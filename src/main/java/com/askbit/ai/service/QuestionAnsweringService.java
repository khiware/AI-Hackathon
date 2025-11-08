package com.askbit.ai.service;

import com.askbit.ai.dto.AskRequest;
import com.askbit.ai.dto.AskResponse;
import com.askbit.ai.dto.Citation;
import com.askbit.ai.model.Document;
import com.askbit.ai.model.DocumentChunk;
import com.askbit.ai.model.QueryHistory;
import com.askbit.ai.repository.DocumentRepository;
import com.askbit.ai.repository.QueryHistoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionAnsweringService {

    private final RetrievalService retrievalService;
    private final HybridRetrievalService hybridRetrievalService;
    private final DocumentRepository documentRepository;
    private final ModelRouterService modelRouterService;
    private final PiiRedactionService piiRedactionService;
    private final QueryHistoryRepository queryHistoryRepository;
    private final ClarificationService clarificationService;
    private final ObjectMapper objectMapper;

    @Value("${askbit.ai.confidence-threshold:0.7}")
    private double confidenceThreshold;

    @Value("${askbit.ai.use-hybrid-search:true}")
    private boolean useHybridSearch;

    @Value("${askbit.ai.max-retrieval-results:5}")
    private int maxRetrievalResults;

    /**
     * Answer a question based on company policy documents.
     */
    @Transactional
    @Cacheable(value = "queries", key = "#request.question.toLowerCase().replaceAll('[^a-z0-9\\\\s]', '').trim()")
    public AskResponse answerQuestion(AskRequest request) {
        long startTime = System.currentTimeMillis();

        String question = request.getQuestion();

        // Validate input
        if (question == null || question.trim().isEmpty()) {
            return buildErrorResponse("Question cannot be empty");
        }

        // Redact PII from question
        String originalQuestion = question;
        question = piiRedactionService.redactPii(question);
        boolean piiRedacted = !originalQuestion.equals(question);

        if (piiRedacted) {
            log.info("PII detected and redacted in question");
        }

        // Normalize question for caching
        String normalizedQuestion = normalizeQuestion(question);

        // Check if question needs clarification
        if (clarificationService.needsClarification(question)) {
            String clarificationQuestion = clarificationService
                    .generateClarificationQuestion(question);

            return AskResponse.builder()
                    .clarificationQuestion(clarificationQuestion)
                    .needsClarification(true)
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }

        // Retrieve relevant document chunks using Hybrid Search
        List<Citation> citations;

        if (useHybridSearch) {
            log.info("Using hybrid search (vector + keyword) for retrieval");
            citations = performHybridRetrieval(question);
        } else {
            log.info("Using traditional vector-only search for retrieval");
            citations = retrievalService.retrieveRelevantChunks(question);
        }

        // If no relevant documents found, return appropriate message
        if (citations.isEmpty()) {
            return buildNoDocumentsFoundResponse(
                    System.currentTimeMillis() - startTime);
        }

        // Build context from retrieved chunks
        String context = buildContextFromCitations(citations);

        // Build prompt for LLM
        String prompt = buildPrompt(question, context);

        // Get response from model
        ModelRouterService.ModelResponse modelResponse =
                modelRouterService.generateResponse(prompt);

        if (!modelResponse.getSuccess()) {
            return buildErrorResponse("Failed to generate answer. Please try again.");
        }

        // Redact PII from answer
        String answer = piiRedactionService.redactPii(modelResponse.getContent());

        // Calculate confidence based on citation scores
        double confidence = calculateConfidence(citations);

        // Build response
        AskResponse response = AskResponse.builder()
                .answer(answer)
                .citations(citations)
                .confidence(confidence)
                .cached(false)
                .needsClarification(false)
                .responseTimeMs(System.currentTimeMillis() - startTime)
                .modelUsed(modelResponse.getModelUsed())
                .piiRedacted(piiRedacted)
                .build();

        // Save to query history for analytics
        saveQueryHistory(normalizedQuestion, originalQuestion, response,
                modelResponse.getModelUsed());

        return response;
    }

    private String buildPrompt(String question, String context) {
        return String.format("""
                You are AskBit.AI, an internal policy assistant for employees.

                Your task is to answer questions based ONLY on the provided company documents.

                IMPORTANT RULES:
                1. Answer ONLY using information from the retrieved documents below
                2. If the documents don't contain the answer, say: "I couldn't find a clear policy on this. Please check with HR or submit a ticket."
                3. Include specific references to document sections when answering
                4. Be concise and professional
                5. Do not make up information or use external knowledge
                6. If information is ambiguous, acknowledge it

                Retrieved Documents:
                %s

                Employee Question: %s

                Answer:
                """, context, question);
    }

    /**
     * Perform hybrid retrieval (vector + keyword search) and convert to citations
     */
    private List<Citation> performHybridRetrieval(String question) {
        // Use hybrid search to get document chunks
        List<DocumentChunk> chunks = hybridRetrievalService.hybridSearch(
                question,
                maxRetrievalResults
        );

        // Convert chunks to citations with document metadata
        return chunks.stream()
                .map(this::convertChunkToCitation)
                .collect(Collectors.toList());
    }

    /**
     * Convert DocumentChunk to Citation with document metadata
     */
    private Citation convertChunkToCitation(DocumentChunk chunk) {
        // Get document metadata
        Document document = documentRepository
                .findByDocumentId(chunk.getDocumentId())
                .orElse(null);

        Citation.CitationBuilder builder = Citation.builder()
                .documentId(chunk.getDocumentId())
                .pageNumber(chunk.getPageNumber())
                .section(chunk.getSection())
                .startLine(chunk.getStartLine())
                .endLine(chunk.getEndLine())
                .snippet(chunk.getContent())
                .relevanceScore(0.85); // Hybrid search provides internal scoring

        if (document != null) {
            builder.fileName(document.getFileName())
                    .version(document.getVersion());
        }

        return builder.build();
    }

    /**
     * Build context string from citations (used by both retrieval methods)
     */
    private String buildContextFromCitations(List<Citation> citations) {
        StringBuilder context = new StringBuilder();

        for (int i = 0; i < citations.size(); i++) {
            Citation citation = citations.get(i);
            context.append(String.format("[Document %d: %s, Page %d]\n%s\n\n",
                    i + 1,
                    citation.getFileName() != null ? citation.getFileName() : "Unknown",
                    citation.getPageNumber() != null ? citation.getPageNumber() : 0,
                    citation.getSnippet()));
        }

        return context.toString();
    }

    public String normalizeQuestion(String question) {
        return question.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .trim();
    }

    private double calculateConfidence(List<Citation> citations) {
        if (citations == null || citations.isEmpty()) {
            return 0.0;
        }

        double avgRelevance = citations.stream()
                .mapToDouble(Citation::getRelevanceScore)
                .average()
                .orElse(0.0);

        // Adjust confidence based on number of citations
        double citationBonus = Math.min(citations.size() * 0.05, 0.15);

        return Math.min(avgRelevance + citationBonus, 1.0);
    }

    public AskResponse buildCachedResponse(AskResponse response, long totalTime) {
        return AskResponse.builder()
                .answer(response.getAnswer())
                .citations(response.getCitations())
                .confidence(response.getConfidence())
                .cached(true)  // Mark as cached
                .needsClarification(response.getNeedsClarification())
                .clarificationQuestion(response.getClarificationQuestion())
                .responseTimeMs(totalTime)  // Use actual retrieval time
                .modelUsed("cached")  // Indicate it was served from cache
                .piiRedacted(response.getPiiRedacted())
                .build();
    }

    private AskResponse buildNoDocumentsFoundResponse(long responseTime) {
        return AskResponse.builder()
                .answer("I couldn't find a clear policy on this. Please check with HR or submit a ticket.")
                .citations(List.of())
                .confidence(0.0)
                .cached(false)
                .needsClarification(false)
                .responseTimeMs(responseTime)
                .build();
    }

    private AskResponse buildErrorResponse(String message) {
        return AskResponse.builder()
                .answer(message)
                .citations(List.of())
                .confidence(0.0)
                .cached(false)
                .needsClarification(false)
                .build();
    }

    private void saveQueryHistory(String normalizedQuestion, String originalQuestion,
                                   AskResponse response, String modelUsed) {
        try {
            String citationsJson = objectMapper.writeValueAsString(response.getCitations());

            QueryHistory queryHistory = QueryHistory.builder()
                    .question(originalQuestion)
                    .normalizedQuestion(normalizedQuestion)
                    .answer(response.getAnswer())
                    .confidence(response.getConfidence())
                    .fromCache(false)
                    .responseTimeMs(response.getResponseTimeMs())
                    .modelUsed(modelUsed)
                    .citationsJson(citationsJson)
                    .piiRedacted(response.getPiiRedacted())
                    .clarificationAsked(false)
                    .build();

            queryHistoryRepository.save(queryHistory);
        } catch (JsonProcessingException e) {
            log.error("Error saving query history", e);
        }
    }
}

