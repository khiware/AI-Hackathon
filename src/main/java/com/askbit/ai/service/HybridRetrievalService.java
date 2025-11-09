package com.askbit.ai.service;

import com.askbit.ai.model.Document;
import com.askbit.ai.model.DocumentChunk;
import com.askbit.ai.repository.DocumentChunkRepository;
import com.askbit.ai.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Enhanced Retrieval Service with Hybrid Search (Vector + Keyword)
 * Supports PostgreSQL pgvector for fast similarity search
 * NOW WITH VERSION-AWARE RETRIEVAL
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HybridRetrievalService {

    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentRepository documentRepository;
    private final EmbeddingService embeddingService;

    private static final double VECTOR_WEIGHT = 0.7;    // 70% weight for vector similarity
    private static final double KEYWORD_WEIGHT = 0.3;   // 30% weight for keyword matching

    /**
     * Hybrid search: Combines vector similarity and keyword matching
     * Results are cached for faster subsequent queries
     */
    public List<DocumentChunk> hybridSearch(String question, int topK) {
        return hybridSearchWithVersionFilter(question, topK, null);
    }

    /**
     * Version-aware hybrid search
     * @param question The search query
     * @param topK Number of results to return
     * @param targetYear Optional year filter (null = latest versions only)
     */
    public List<DocumentChunk> hybridSearchWithVersionFilter(String question, int topK, Integer targetYear) {
        long startTime = System.currentTimeMillis();

        // Get relevant document IDs based on version filter
        List<String> allowedDocumentIds = getVersionFilteredDocumentIds(targetYear);

        if (allowedDocumentIds.isEmpty()) {
            log.warn("No documents found for version filter (year: {})", targetYear);
            return Collections.emptyList();
        }

        log.info("Version filter: {} documents allowed (year: {})",
                allowedDocumentIds.size(), targetYear != null ? targetYear : "latest");

        // Step 1: Generate embedding for the question
        List<Double> questionEmbedding = embeddingService.generateEmbedding(question);

        // Step 2: Vector similarity search (filtered by version)
        List<ScoredChunk> vectorResults = vectorSimilaritySearch(questionEmbedding, topK * 2, allowedDocumentIds);

        // Step 3: Keyword-based search (filtered by version)
        List<ScoredChunk> keywordResults = keywordSearch(question, topK * 2, allowedDocumentIds);

        // Step 4: Combine and re-rank results
        List<DocumentChunk> hybridResults = combineAndRerankResults(
                vectorResults, keywordResults, topK);

        long duration = System.currentTimeMillis() - startTime;
        log.info("Version-aware hybrid search completed in {}ms: found {} results", duration, hybridResults.size());

        return hybridResults;
    }

    /**
     * Get document IDs filtered by version (latest or specific year)
     */
    private List<String> getVersionFilteredDocumentIds(Integer targetYear) {
        List<Document> documents;

        if (targetYear == null) {
            // Get latest versions only
            documents = documentRepository.findLatestVersions();
            log.info("Using latest versions: {} documents", documents.size());
        } else {
            // Get latest versions before/at target year
            documents = documentRepository.findLatestVersionsBeforeYear(targetYear);
            log.info("Using versions before/at year {}: {} documents", targetYear, documents.size());
        }

        return documents.stream()
                .map(Document::getDocumentId)
                .collect(Collectors.toList());
    }

    /**
     * Pure vector similarity search using cosine similarity (with version filter)
     */
    private List<ScoredChunk> vectorSimilaritySearch(List<Double> queryEmbedding, int limit,
                                                      List<String> allowedDocumentIds) {
        // Get only chunks from allowed documents
        List<DocumentChunk> allChunks = allowedDocumentIds.isEmpty()
                ? Collections.emptyList()
                : documentChunkRepository.findByDocumentIdIn(allowedDocumentIds);

        return allChunks.stream()
                .filter(chunk -> chunk.getEmbeddingVector() != null)
                .map(chunk -> {
                    double similarity = calculateCosineSimilarity(
                            queryEmbedding,
                            parseEmbedding(chunk.getEmbeddingVector())
                    );
                    return new ScoredChunk(chunk, similarity, 0.0);
                })
                .sorted(Comparator.comparingDouble(sc -> -sc.vectorScore))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Keyword-based search using content matching (with version filter)
     */
    private List<ScoredChunk> keywordSearch(String query, int limit, List<String> allowedDocumentIds) {
        String[] keywords = extractKeywords(query);

        // Get only chunks from allowed documents
        List<DocumentChunk> allChunks = allowedDocumentIds.isEmpty()
                ? Collections.emptyList()
                : documentChunkRepository.findByDocumentIdIn(allowedDocumentIds);

        return allChunks.stream()
                .map(chunk -> {
                    double keywordScore = calculateKeywordScore(chunk.getContent(), keywords);
                    return new ScoredChunk(chunk, 0.0, keywordScore);
                })
                .filter(sc -> sc.keywordScore > 0)
                .sorted(Comparator.comparingDouble(sc -> -sc.keywordScore))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Combine vector and keyword results with weighted scoring
     */
    private List<DocumentChunk> combineAndRerankResults(
            List<ScoredChunk> vectorResults,
            List<ScoredChunk> keywordResults,
            int topK) {

        Map<Long, ScoredChunk> combinedResults = new HashMap<>();

        // Add vector results
        for (ScoredChunk sc : vectorResults) {
            combinedResults.put(sc.chunk.getId(), sc);
        }

        // Merge with keyword results
        for (ScoredChunk sc : keywordResults) {
            Long id = sc.chunk.getId();
            if (combinedResults.containsKey(id)) {
                ScoredChunk existing = combinedResults.get(id);
                existing.keywordScore = Math.max(existing.keywordScore, sc.keywordScore);
            } else {
                combinedResults.put(id, sc);
            }
        }

        // Calculate hybrid scores and sort
        return combinedResults.values().stream()
                .peek(sc -> sc.hybridScore =
                        (sc.vectorScore * VECTOR_WEIGHT) + (sc.keywordScore * KEYWORD_WEIGHT))
                .sorted(Comparator.comparingDouble(sc -> -sc.hybridScore))
                .limit(topK)
                .map(sc -> sc.chunk)
                .collect(Collectors.toList());
    }

    /**
     * Calculate cosine similarity between two vectors
     */
    private double calculateCosineSimilarity(List<Double> vec1, List<Double> vec2) {
        if (vec1.size() != vec2.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += vec1.get(i) * vec1.get(i);
            norm2 += vec2.get(i) * vec2.get(i);
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Parse embedding string to list of doubles
     */
    private List<Double> parseEmbedding(String embeddingStr) {
        if (embeddingStr == null || embeddingStr.isEmpty()) {
            return Collections.emptyList();
        }

        String[] parts = embeddingStr.replace("[", "").replace("]", "").split(",");
        List<Double> embedding = new ArrayList<>(parts.length);

        for (String part : parts) {
            try {
                embedding.add(Double.parseDouble(part.trim()));
            } catch (NumberFormatException e) {
                log.warn("Failed to parse embedding value: {}", part);
            }
        }

        return embedding;
    }

    /**
     * Extract keywords from query
     */
    private String[] extractKeywords(String query) {
        return query.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .split("\\s+");
    }

    /**
     * Calculate keyword matching score
     */
    private double calculateKeywordScore(String content, String[] keywords) {
        if (content == null || keywords.length == 0) {
            return 0.0;
        }

        String lowerContent = content.toLowerCase();
        int matches = 0;

        for (String keyword : keywords) {
            if (keyword.length() > 2 && lowerContent.contains(keyword)) {
                matches++;
            }
        }

        return (double) matches / keywords.length;
    }

    /**
     * Helper class to store chunk with multiple scores
     */
    private static class ScoredChunk {
        DocumentChunk chunk;
        double vectorScore;
        double keywordScore;
        double hybridScore;

        ScoredChunk(DocumentChunk chunk, double vectorScore, double keywordScore) {
            this.chunk = chunk;
            this.vectorScore = vectorScore;
            this.keywordScore = keywordScore;
        }
    }
}

