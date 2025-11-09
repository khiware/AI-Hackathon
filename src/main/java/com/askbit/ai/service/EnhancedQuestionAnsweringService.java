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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ENHANCED QuestionAnsweringService with HybridRetrievalService Integration
 *
 * This is an example of how to integrate HybridRetrievalService into the existing
 * QuestionAnsweringService. You can replace the existing service or add this as
 * a feature flag option.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EnhancedQuestionAnsweringService {

    // Option 1: Use HybridRetrievalService only
    private final HybridRetrievalService hybridRetrievalService;

    // Option 2: Keep both and use feature flag (recommended)
    private final RetrievalService retrievalService;

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

    @Transactional
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

        // Check cache first
        Optional<QueryHistory> cachedQuery = queryHistoryRepository
                .findByNormalizedQuestion(normalizedQuestion);

        if (cachedQuery.isPresent()) {
            log.info("Cache hit for question: {}", normalizedQuestion);
            return buildCachedResponse(cachedQuery.get(),
                    System.currentTimeMillis() - startTime);
        }

        // Handle clarification flow:
        // If user provided context (responding to a clarification question), expand the question
        if (request.getContext() != null && !request.getContext().trim().isEmpty()) {
            log.info("User provided clarification context: {}", request.getContext());
            question = clarificationService.expandQuestionWithContext(question, request.getContext());
            log.info("Expanded question: {}", question);

            // ITERATIVE CLARIFICATION: Check if the expanded question still needs clarification
            if (clarificationService.needsClarification(question)) {
                String clarificationQuestion = clarificationService
                        .generateClarificationQuestion(question);

                log.info("Expanded question still needs clarification: {}", question);

                return AskResponse.builder()
                        .clarificationQuestion(clarificationQuestion)
                        .needsClarification(true)
                        .responseTimeMs(System.currentTimeMillis() - startTime)
                        .build();
            }
        }
        // Otherwise, check if question needs clarification (first pass)
        else if (clarificationService.needsClarification(question)) {
            String clarificationQuestion = clarificationService
                    .generateClarificationQuestion(question);

            log.info("Question needs clarification: {}", question);

            return AskResponse.builder()
                    .clarificationQuestion(clarificationQuestion)
                    .needsClarification(true)
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();
        }

        // === HYBRID SEARCH INTEGRATION ===
        List<Citation> citations;

        if (useHybridSearch) {
            // Use hybrid search (vector + keyword)
            log.info("Using hybrid search for retrieval");
            citations = performHybridRetrieval(question);
        } else {
            // Use traditional vector-only search
            log.info("Using traditional vector search");
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

        // Save to query history
        saveQueryHistory(normalizedQuestion, originalQuestion, response,
                modelResponse.getModelUsed());

        return response;
    }

    /**
     * Perform hybrid retrieval and convert to citations
     */
    private List<Citation> performHybridRetrieval(String question) {
        // Use hybrid search
        List<DocumentChunk> chunks = hybridRetrievalService.hybridSearch(
                question,
                maxRetrievalResults
        );

        // Convert chunks to citations
        return chunks.stream()
                .map(this::convertChunkToCitation)
                .collect(Collectors.toList());
    }

    /**
     * Convert DocumentChunk to Citation
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
                .relevanceScore(0.85); // Hybrid search uses internal scoring

        if (document != null) {
            builder.fileName(document.getFileName())
                    .version(document.getVersion());
        }

        return builder.build();
    }

    /**
     * Build context string from citations
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

    private String normalizeQuestion(String question) {
        return question.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .trim();
    }

    private double calculateConfidence(List<Citation> citations) {
        if (citations.isEmpty()) {
            return 0.0;
        }

        double avgScore = citations.stream()
                .mapToDouble(c -> c.getRelevanceScore() != null ? c.getRelevanceScore() : 0.85)
                .average()
                .orElse(0.0);

        return Math.min(avgScore, 1.0);
    }

    private AskResponse buildCachedResponse(QueryHistory cached, long responseTime) {
        List<Citation> citations;
        try {
            citations = objectMapper.readValue(
                    cached.getCitationsJson(),
                    objectMapper.getTypeFactory()
                            .constructCollectionType(List.class, Citation.class)
            );
        } catch (JsonProcessingException e) {
            log.error("Failed to parse citations from cached query", e);
            citations = List.of();
        }

        return AskResponse.builder()
                .answer(cached.getAnswer())
                .citations(citations)
                .confidence(cached.getConfidence())
                .cached(true)
                .needsClarification(false)
                .responseTimeMs(responseTime)
                .modelUsed(cached.getModelUsed())
                .piiRedacted(cached.getPiiRedacted())
                .build();
    }

    private AskResponse buildErrorResponse(String message) {
        return AskResponse.builder()
                .answer(message)
                .confidence(0.0)
                .cached(false)
                .needsClarification(false)
                .responseTimeMs(0L)
                .build();
    }

    private AskResponse buildNoDocumentsFoundResponse(long responseTime) {
        return AskResponse.builder()
                .answer("I couldn't find any relevant information in the company documents. " +
                        "Please contact HR or submit a support ticket for assistance.")
                .confidence(0.0)
                .cached(false)
                .needsClarification(false)
                .responseTimeMs(responseTime)
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
                    .clarificationAsked(response.getNeedsClarification())
                    .queryTime(LocalDateTime.now())
                    .build();

            queryHistoryRepository.save(queryHistory);
            log.info("Query history saved for question: {}", normalizedQuestion);

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize citations for query history", e);
        }
    }
}

