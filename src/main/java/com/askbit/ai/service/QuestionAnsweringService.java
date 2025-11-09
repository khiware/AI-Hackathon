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

import java.util.List;
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
    private final CacheService cacheService;
    private final ObjectMapper objectMapper;
    private long cacheHits = 0L;

    @Value("${askbit.ai.confidence-threshold:0.7}")
    private double confidenceThreshold;

    @Value("${askbit.ai.use-hybrid-search:true}")
    private boolean useHybridSearch;

    @Value("${askbit.ai.max-retrieval-results:1}")
    private int maxRetrievalResults;

    /**
     * Answer a question based on company policy documents.
     */
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

        // Try to get from Redis cache first
        String cacheKey = "queries::" + normalizedQuestion;
        AskResponse cachedResponse = (AskResponse) cacheService.getFromCache(cacheKey);
        if (cachedResponse != null) {
            cacheService.saveToCache("queriesCacheHits", String.valueOf(cacheHits++));
            return buildCachedResponse(cachedResponse,
                    System.currentTimeMillis() - startTime);
        }

        // Retrieve relevant document chunks using Hybrid Search
        List<DocumentChunk> chunks;
        List<Citation> citations;

        if (useHybridSearch) {
            log.info("Using hybrid search (vector + keyword) for retrieval");
            chunks = hybridRetrievalService.hybridSearch(question, maxRetrievalResults);
            citations = convertChunksToCitations(chunks);
        } else {
            log.info("Using traditional vector-only search for retrieval");
            citations = retrievalService.retrieveRelevantChunks(question);
            chunks = null; // Not available in traditional search
        }

        // If no relevant documents found, return appropriate message
        if (citations.isEmpty()) {
            return buildNoDocumentsFoundResponse(
                    System.currentTimeMillis() - startTime);
        }

        // Build context from retrieved chunks (use chunks if available, else citations)
        String context = (useHybridSearch && chunks != null)
            ? buildContextFromChunks(chunks)
            : buildContextFromCitations(citations);

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

        // Check if the answer indicates no clear information was found
        boolean isUnclearAnswer = isAnswerUnclear(answer);

        // If answer is unclear, don't include citations
        List<Citation> finalCitations = isUnclearAnswer ? List.of() : citations;
        double confidence = isUnclearAnswer ? 0.0 : calculateConfidence(citations);

        // Build response
        AskResponse response = AskResponse.builder()
                .answer(answer)
                .citations(finalCitations)
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

        // Store in Redis cache with 1-day TTL
        cacheService.saveToCache(cacheKey, response);

        return response;
    }

    private String buildPrompt(String question, String context) {
        String promptTemplate = """
                You are AskBit.AI, an internal policy assistant for employees.

                Your task is to answer questions based ONLY on the provided company documents.

                CRITICAL RULES:
                1. Answer ONLY using information from the retrieved documents below
                2. If the retrieved documents DO NOT contain specific information to answer the question, you MUST respond EXACTLY with:
                   "I couldn't find a clear policy on this. Please check with HR or submit a ticket."
                3. DO NOT make up information or provide general answers not found in the documents
                4. If the documents contain partial or related information but not the specific answer, still say you couldn't find it
                
                FORMATTING RULES (only if you CAN answer from the documents):
                   - DO NOT include any citation references like [Document X, Page Y] in your answer
                   - Citations will be provided separately, so focus only on the content
                   
                   For Simple Questions:
                   - Provide a direct, concise answer in 1-3 sentences
                   - Example: "Please read the Progressive Variable Pay policy available on myPOD under Corporate Programme and Policies for more information."
                
                   For Comparative Data or Multiple Items:
                   - Use Markdown tables to present structured information clearly
                   - Example format:
                     | Category | Details | Requirements |
                     |----------|---------|--------------|
                     | Item 1   | Info    | Criteria     |
                     | Item 2   | Info    | Criteria     |
                
                   For Processes or Steps:
                   - Use numbered lists with clear step-by-step instructions
                   - Example:
                     Application Process:
                     1. Step One: Description
                     2. Step Two: Description
                     3. Step Three: Description
                
                   For Eligibility Criteria or Options:
                   - Use bullet points with bold labels
                   - Example:
                     Eligibility Criteria:
                     - Experience: 2+ years required
                     - Department: All departments eligible
                     - Performance: Meets expectations or above
                
                   For Numerical Data or Statistics:
                   - Present data in a clean table format
                   - If percentages or amounts are involved, format them clearly
                
                   For Hierarchical Information:
                   - Use nested lists or structured headings
                
                   For Timeline or Date-based Information:
                   - Use tables or chronological lists
                
                4. FORMATTING ENHANCEMENTS:
                   - Use **bold** for important terms, headings, and key points
                   - Use horizontal separators (---) to break up long content
                   - Use > blockquotes for important notices or warnings
                   - Keep paragraphs short (2-3 sentences max)
                   - Add blank lines between sections for readability
                
                5. Be concise, professional, and accurate
                6. Do not make up information or use external knowledge
                7. Present information in the most visually clear and easily understandable format

                Retrieved Documents:
                """;

        return promptTemplate + "\n" + context + "\n\nEmployee Question: " + question +
               "\n\nAnswer (use the most appropriate format - tables, lists, or structured text):";
    }

    /**
     * Convert chunks to citations without snippet (for response)
     */
    private List<Citation> convertChunksToCitations(List<DocumentChunk> chunks) {
        return chunks.stream()
                .map(this::convertChunkToCitation)
                .collect(Collectors.toList());
    }

    /**
     * Convert DocumentChunk to Citation with document metadata
     * Note: Snippet is excluded from response, only metadata is included
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
                // .snippet(chunk.getContent())  // Removed: snippet not included in response
                .relevanceScore(0.85); // Hybrid search provides internal scoring

        if (document != null) {
            builder.fileName(document.getFileName())
                    .version(document.getVersion());
        }

        return builder.build();
    }

    /**
     * Build context string from chunks (used internally for LLM prompt)
     */
    private String buildContextFromChunks(List<DocumentChunk> chunks) {
        StringBuilder context = new StringBuilder();

        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk chunk = chunks.get(i);
            Document document = documentRepository
                    .findByDocumentId(chunk.getDocumentId())
                    .orElse(null);

            String fileName = document != null ? document.getFileName() : "Unknown";
            Integer pageNum = chunk.getPageNumber() != null ? chunk.getPageNumber() : 0;

            context.append(String.format("[Document %d: %s, Page %d]\n%s\n\n",
                    i + 1,
                    fileName,
                    pageNum,
                    chunk.getContent()));
        }

        return context.toString();
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

    private String normalizeQuestion(String question) {
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

    private AskResponse buildCachedResponse(AskResponse response, long totalTime) {
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

