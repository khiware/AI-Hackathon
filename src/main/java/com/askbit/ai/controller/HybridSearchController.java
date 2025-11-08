package com.askbit.ai.controller;

import com.askbit.ai.dto.Citation;
import com.askbit.ai.model.Document;
import com.askbit.ai.model.DocumentChunk;
import com.askbit.ai.repository.DocumentRepository;
import com.askbit.ai.service.HybridRetrievalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Example Controller demonstrating HybridRetrievalService usage
 *
 * This controller provides endpoints to test and compare hybrid search
 * with traditional retrieval methods.
 */
@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
public class HybridSearchController {

    private final HybridRetrievalService hybridRetrievalService;
    private final DocumentRepository documentRepository;

    /**
     * Basic hybrid search endpoint
     *
     * Example: GET /api/search/hybrid?query=leave%20policy&topK=5
     */
    @GetMapping("/hybrid")
    public ResponseEntity<Map<String, Object>> hybridSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK) {

        log.info("Hybrid search request: query={}, topK={}", query, topK);

        long startTime = System.currentTimeMillis();

        // Perform hybrid search
        List<DocumentChunk> chunks = hybridRetrievalService.hybridSearch(query, topK);

        long duration = System.currentTimeMillis() - startTime;

        // Convert to citations for better readability
        List<Citation> citations = chunks.stream()
                .map(this::convertToCitation)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("query", query);
        response.put("resultsCount", citations.size());
        response.put("searchTimeMs", duration);
        response.put("searchType", "hybrid");
        response.put("results", citations);

        return ResponseEntity.ok(response);
    }

    /**
     * Detailed hybrid search with scoring information
     *
     * Example: POST /api/search/hybrid/detailed
     * Body: { "query": "leave policy", "topK": 5 }
     */
    @PostMapping("/hybrid/detailed")
    public ResponseEntity<Map<String, Object>> detailedHybridSearch(
            @RequestBody SearchRequest request) {

        log.info("Detailed hybrid search: {}", request.getQuery());

        long startTime = System.currentTimeMillis();

        // Perform hybrid search
        List<DocumentChunk> chunks = hybridRetrievalService.hybridSearch(
                request.getQuery(),
                request.getTopK() != null ? request.getTopK() : 5
        );

        long duration = System.currentTimeMillis() - startTime;

        // Build detailed response
        List<Map<String, Object>> detailedResults = chunks.stream()
                .map(chunk -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("documentId", chunk.getDocumentId());
                    result.put("chunkIndex", chunk.getChunkIndex());
                    result.put("pageNumber", chunk.getPageNumber());
                    result.put("section", chunk.getSection());
                    result.put("content", chunk.getContent());
                    result.put("contentLength", chunk.getContent().length());

                    // Get document metadata
                    documentRepository.findByDocumentId(chunk.getDocumentId())
                            .ifPresent(doc -> {
                                result.put("fileName", doc.getFileName());
                                result.put("fileType", doc.getFileType());
                                result.put("version", doc.getVersion());
                            });

                    return result;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("query", request.getQuery());
        response.put("resultsCount", detailedResults.size());
        response.put("searchTimeMs", duration);
        response.put("searchType", "hybrid");
        response.put("hybridWeights", Map.of(
                "vectorWeight", 0.7,
                "keywordWeight", 0.3
        ));
        response.put("results", detailedResults);

        return ResponseEntity.ok(response);
    }

    /**
     * Search with custom parameters
     *
     * Example: POST /api/search/hybrid/custom
     */
    @PostMapping("/hybrid/custom")
    public ResponseEntity<Map<String, Object>> customHybridSearch(
            @RequestBody CustomSearchRequest request) {

        log.info("Custom hybrid search: query={}, topK={}",
                request.getQuery(), request.getTopK());

        // Perform hybrid search
        List<DocumentChunk> chunks = hybridRetrievalService.hybridSearch(
                request.getQuery(),
                request.getTopK() != null ? request.getTopK() : 5
        );

        // Filter by document ID if specified
        if (request.getDocumentId() != null && !request.getDocumentId().isEmpty()) {
            chunks = chunks.stream()
                    .filter(chunk -> chunk.getDocumentId().equals(request.getDocumentId()))
                    .collect(Collectors.toList());
        }

        // Filter by minimum content length if specified
        if (request.getMinContentLength() != null) {
            chunks = chunks.stream()
                    .filter(chunk -> chunk.getContent().length() >= request.getMinContentLength())
                    .collect(Collectors.toList());
        }

        List<Citation> citations = chunks.stream()
                .map(this::convertToCitation)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("query", request.getQuery());
        response.put("resultsCount", citations.size());
        response.put("filters", Map.of(
                "documentId", request.getDocumentId() != null ? request.getDocumentId() : "none",
                "minContentLength", request.getMinContentLength() != null ? request.getMinContentLength() : 0
        ));
        response.put("results", citations);

        return ResponseEntity.ok(response);
    }

    /**
     * Get statistics about hybrid search performance
     */
    @GetMapping("/hybrid/stats")
    public ResponseEntity<Map<String, Object>> getHybridSearchStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("searchType", "hybrid");
        stats.put("weights", Map.of(
                "vectorSimilarity", 0.7,
                "keywordMatching", 0.3
        ));
        stats.put("features", List.of(
                "Vector similarity search using embeddings",
                "Keyword-based content matching",
                "Weighted score combination",
                "Result caching for performance"
        ));
        stats.put("cacheEnabled", true);
        stats.put("cacheName", "vectorSearchCache");

        return ResponseEntity.ok(stats);
    }

    /**
     * Helper method to convert DocumentChunk to Citation
     */
    private Citation convertToCitation(DocumentChunk chunk) {
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
                .relevanceScore(0.85);

        if (document != null) {
            builder.fileName(document.getFileName())
                    .version(document.getVersion());
        }

        return builder.build();
    }

    // DTO Classes

    @lombok.Data
    public static class SearchRequest {
        private String query;
        private Integer topK;
    }

    @lombok.Data
    public static class CustomSearchRequest {
        private String query;
        private Integer topK;
        private String documentId;
        private Integer minContentLength;
    }
}

