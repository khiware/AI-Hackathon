package com.askbit.ai.service;

import com.askbit.ai.analyzer.TemporalQueryAnalyzer;
import com.askbit.ai.dto.AskRequest;
import com.askbit.ai.dto.AskResponse;
import com.askbit.ai.dto.Citation;
import com.askbit.ai.dto.ModelResponse;
import com.askbit.ai.dto.TemporalContext;
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
    private final TemporalQueryAnalyzer temporalQueryAnalyzer;
    private final QueryPreprocessingService queryPreprocessingService;
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
    public AskResponse answerQuestion(AskRequest request) {
        long startTime = System.currentTimeMillis();

        String question = request.getQuestion();

        // Validate input
        if (question == null || question.trim().isEmpty()) {
            return buildErrorResponse("Question cannot be empty");
        }

        // Check for greetings and conversational inputs
        if (isGreeting(question)) {
            return buildGreetingResponse(question, System.currentTimeMillis() - startTime);
        }

        // STEP 1: Preprocess question - handle spelling mistakes, special characters, etc.
        String originalQuestion = question;
        question = queryPreprocessingService.preprocessQuestion(question);

        if (!originalQuestion.equals(question)) {
            // Mask PII before logging to prevent exposure of names, phone numbers, emails etc.
            String maskedOriginal = piiRedactionService.redactPii(originalQuestion);
            String maskedProcessed = piiRedactionService.redactPii(question);
            log.info("Question preprocessed (PII masked): '{}' -> '{}'", maskedOriginal, maskedProcessed);
        }

        // STEP 2: Redact PII from question
        String questionBeforePiiRedaction = question;
        question = piiRedactionService.redactPii(question);
        boolean piiRedacted = !questionBeforePiiRedaction.equals(question);

        if (piiRedacted) {
            log.info("PII detected and redacted in question");
        }

        // Normalize question for caching
        String normalizedQuestion = normalizeQuestion(question);

        // Track if user provided clarification context
        boolean wasClarified = request.getContext() != null && !request.getContext().trim().isEmpty();

        // Try to get from Redis cache first
        String cacheKey = "queries::" + normalizedQuestion;
        AskResponse cachedResponse = (AskResponse) cacheService.getFromCache(cacheKey);

        // Keep cached answer text for potential failover (even if returning cached response now)
        String cachedAnswerText = (cachedResponse != null) ? cachedResponse.getAnswer() : null;

        if (cachedResponse != null) {
            cacheService.incrementCacheHit("queriesCacheHits");
            AskResponse newCachedResponse = buildCachedResponse(cachedResponse,
                    System.currentTimeMillis() - startTime);
            saveQueryHistory(normalizedQuestion, originalQuestion, newCachedResponse,
                    newCachedResponse.getModelUsed(), false, null);
            return newCachedResponse;
        }

        // TEMPORAL ANALYSIS: Analyze question for version/year context
        TemporalContext temporalContext =
                temporalQueryAnalyzer.analyzeQuestion(question);

        // If temporal analysis needs clarification (e.g., "old policy" without year)
        if (temporalContext.isNeedsClarification()) {
            log.info("Temporal analysis requires clarification");
            AskResponse needClarificationResponse = AskResponse.builder()
                    .clarificationQuestion(temporalContext.getClarificationReason())
                    .needsClarification(true)
                    .cached(false)
                    .piiRedacted(piiRedacted)
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();

            saveQueryHistory(question, originalQuestion, needClarificationResponse,
                    null, wasClarified, temporalContext.getClarificationReason());
            return needClarificationResponse;
        }
        // Handle clarification flow:
        // If user provided context (responding to a clarification question), expand the question
        if (wasClarified) {
            log.info("Processing clarification response");
            question = clarificationService.expandQuestionWithContext(question, request.getContext());
            log.info("Question expanded with context");

            // ITERATIVE CLARIFICATION: Check if the expanded question still needs clarification
            if (clarificationService.needsClarification(question)) {
                String clarificationQuestion = clarificationService
                        .generateClarificationQuestion(question);

                log.info("Expanded question still needs clarification");

                AskResponse wasClarifiedResponse = AskResponse.builder()
                        .clarificationQuestion(clarificationQuestion)
                        .needsClarification(true)
                        .cached(false)
                        .piiRedacted(piiRedacted)
                        .responseTimeMs(System.currentTimeMillis() - startTime)
                        .build();
                saveQueryHistory(question, originalQuestion,
                        wasClarifiedResponse, null, true,
                        clarificationQuestion);
                return wasClarifiedResponse;
            }

            // Re-analyze temporal context after clarification
            temporalContext = temporalQueryAnalyzer.analyzeQuestion(question);
        }
        // Otherwise, check if question needs clarification (first pass)
        else if (clarificationService.needsClarification(question)) {
            String clarificationQuestion = clarificationService
                    .generateClarificationQuestion(question);

            log.info("Question needs clarification");

            // Don't save to history yet - wait for user's clarified response
            AskResponse clarificationResponse = AskResponse.builder()
                    .clarificationQuestion(clarificationQuestion)
                    .needsClarification(true)
                    .cached(false)
                    .piiRedacted(piiRedacted)
                    .responseTimeMs(System.currentTimeMillis() - startTime)
                    .build();
            saveQueryHistory(question, originalQuestion,
                    clarificationResponse, null, true,
                    clarificationQuestion);
            return clarificationResponse;
        }

        // Determine target year for version filtering
        Integer targetYear = temporalContext.isHistoricalQuery()
                ? temporalContext.getTargetYear()
                : null;

        log.info("Version filter applied - Latest: {}, Year: {}",
                temporalContext.isUseLatestVersion(), targetYear);

        // Retrieve relevant document chunks using Hybrid Search WITH VERSION FILTER
        List<DocumentChunk> chunks;
        List<Citation> citations;

        if (useHybridSearch) {
            log.info("Using hybrid search (vector + keyword) for retrieval with version filter");
            chunks = hybridRetrievalService.hybridSearchWithVersionFilter(
                    question, maxRetrievalResults, targetYear);
            citations = convertChunksToCitations(chunks);
        } else {
            log.info("Using traditional vector-only search for retrieval with version filter");
            citations = retrievalService.retrieveRelevantChunksWithVersionFilter(question, targetYear);
            chunks = null; // Not available in traditional search
        }

        // If no relevant documents found, return appropriate message
        if (citations.isEmpty()) {
            return buildNoDocumentsFoundResponse(piiRedacted,
                    System.currentTimeMillis() - startTime);
        }

        // Build context from retrieved chunks (use chunks if available, else citations)
        String context = (useHybridSearch && chunks != null)
            ? buildContextFromChunks(chunks)
            : buildContextFromCitations(citations);

        // Build prompt for LLM
        String prompt = buildPrompt(question, context);

        // Get response from model with failover support (pass cached answer if available)
        ModelResponse modelResponse =
                modelRouterService.generateResponseWithFailover(prompt, cachedAnswerText);

        // Note: failover mechanism will return success=true with fallback message if needed
        // So we don't need to check for failure here - just use the content

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
                .preprocessedQuestion(originalQuestion.equals(question) ? null : question)
                .build();

        // Save to query history for analytics
        saveQueryHistory(normalizedQuestion, originalQuestion, response,
                modelResponse.getModelUsed(), false, null);

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
                2. The documents provided are VERSION-SPECIFIC:
                   - If the question asks about current/latest policy, you're seeing the LATEST VERSION only
                   - If the question asks about a specific year (e.g., 2023), you're seeing the version FROM THAT YEAR
                   - Always answer based on the version shown in the document metadata [Document X: filename, Page Y]
                3. If the retrieved documents DO NOT contain specific information to answer the question, you MUST respond EXACTLY with:
                   "I couldn't find a clear policy on this. Please check with HR or submit a ticket."
                4. DO NOT make up information or provide general answers not found in the documents
                5. If the documents contain partial or related information but not the specific answer, still say you couldn't find it
                
                VERSION AWARENESS:
                   - Pay attention to document version numbers and dates in the document headers
                   - If answering about a specific year, acknowledge it: "According to the 2023 policy..."
                   - If answering about current policy, you can say: "According to the current policy..."
                
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
                
                6. FORMATTING ENHANCEMENTS:
                   - Use **bold** for important terms, headings, and key points
                   - Use horizontal separators (---) to break up long content
                   - Use > blockquotes for important notices or warnings
                   - Keep paragraphs short (2-3 sentences max)
                   - Add blank lines between sections for readability
                
                7. Be concise, professional, and accurate
                8. Do not make up information or use external knowledge
                9. Present information in the most visually clear and easily understandable format

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

            // Format: "Document X: filename (version: x.0), Page Y"
            String documentName = citation.getFileName() != null ? citation.getFileName() : "Unknown";
            if (citation.getVersion() != null && !citation.getVersion().isEmpty()) {
                documentName = documentName + " (version: " + citation.getVersion() + ")";
            }

            context.append(String.format("[Document %d: %s, Page %d]\n%s\n\n",
                    i + 1,
                    documentName,
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
        // Check if cached answer is unclear - if so, remove citations
        boolean isUnclear = isAnswerUnclear(response.getAnswer());

        return AskResponse.builder()
                .answer(response.getAnswer())
                .citations(isUnclear ? List.of() : response.getCitations())
                .confidence(isUnclear ? 0.0 : response.getConfidence())
                .cached(true)  // Mark as cached
                .needsClarification(response.getNeedsClarification())
                .clarificationQuestion(response.getClarificationQuestion())
                .responseTimeMs(totalTime)  // Use actual retrieval time
                .modelUsed("cached")  // Indicate it was served from cache
                .piiRedacted(response.getPiiRedacted())
                .build();
    }

    private AskResponse buildNoDocumentsFoundResponse(boolean piiRedacted, long responseTime) {
        return AskResponse.builder()
                .answer("I couldn't find a clear policy on this. Please check with HR or submit a ticket.")
                .citations(List.of())
                .confidence(0.0)
                .cached(false)
                .needsClarification(false)
                .responseTimeMs(responseTime)
                .piiRedacted(piiRedacted)
                .build();
    }

    /**
     * Check if the answer indicates unclear or missing information
     */
    private boolean isAnswerUnclear(String answer) {
        if (answer == null || answer.trim().isEmpty()) {
            return true;
        }

        String lowerAnswer = answer.toLowerCase().trim();

        // Check for common phrases indicating unclear/missing information
        return lowerAnswer.contains("couldn't find") ||
               lowerAnswer.contains("could not find") ||
               lowerAnswer.contains("no clear policy") ||
               lowerAnswer.contains("no information") ||
               lowerAnswer.contains("not available") ||
               lowerAnswer.contains("please check with hr") ||
               lowerAnswer.contains("submit a ticket") ||
               lowerAnswer.contains("i don't have") ||
               lowerAnswer.contains("unable to find") ||
               lowerAnswer.contains("no specific") ||
               lowerAnswer.contains("documents don't contain");
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
                                  AskResponse response, String modelUsed, boolean clarificationAsked,
                                  String clarificationQuestion) {
        try {
            String citationsJson = objectMapper.writeValueAsString(response.getCitations());

            QueryHistory queryHistory = QueryHistory.builder()
                    .question(originalQuestion)
                    .normalizedQuestion(normalizedQuestion)
                    .answer(response.getAnswer())
                    .confidence(response.getConfidence())
                    .fromCache(response.getCached())
                    .responseTimeMs(response.getResponseTimeMs())
                    .modelUsed(modelUsed)
                    .citationsJson(citationsJson)
                    .piiRedacted(response.getPiiRedacted())
                    .clarificationAsked(clarificationAsked)
                    .clarificationQuestion(clarificationQuestion)
                    .build();

            queryHistoryRepository.save(queryHistory);
        } catch (JsonProcessingException e) {
            log.error("Error saving query history", e);
        }
    }

    private boolean isGreeting(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }
        String normalized = input.toLowerCase().trim().replaceAll("[^a-z\\s]", "");
        String[] greetings = {"hello", "hi", "hey", "hola", "greetings", "good morning",
                              "good afternoon", "good evening", "whats up", "how are you",
                              "howdy", "sup", "yo", "hii", "helloo", "heyy"};
        for (String greeting : greetings) {
            if (normalized.equals(greeting) || normalized.startsWith(greeting + " ")) {
                return true;
            }
        }
        return false;
    }

    private AskResponse buildGreetingResponse(String greeting, long responseTime) {
        String answer = "Hello! 👋 I'm AskBit.AI, your AI-powered policy assistant. Ask me anything about company policies, HR, benefits, leave, or expenses!";
        log.info("Responding to greeting with welcome message");
        return AskResponse.builder()
                .answer(answer)
                .citations(List.of())
                .confidence(1.0)
                .cached(false)
                .needsClarification(false)
                .responseTimeMs(responseTime)
                .modelUsed("greeting-handler")
                .piiRedacted(false)
                .build();
    }
}

