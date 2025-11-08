package com.askbit.ai.service;

import com.askbit.ai.dto.Citation;
import com.askbit.ai.model.Document;
import com.askbit.ai.model.DocumentChunk;
import com.askbit.ai.repository.DocumentChunkRepository;
import com.askbit.ai.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetrievalService {

    private final DocumentChunkRepository documentChunkRepository;
    private final DocumentRepository documentRepository;
    private final EmbeddingService embeddingService;

    @Value("${askbit.ai.max-retrieval-results:5}")
    private int maxRetrievalResults;

    @Value("${askbit.ai.confidence-threshold:0.7}")
    private double confidenceThreshold;

    public List<Citation> retrieveRelevantChunks(String question) {
        log.debug("Retrieving relevant chunks for question: {}", question);

        // Generate embedding for the question
        List<Double> questionEmbedding = embeddingService.generateEmbedding(question);

        if (questionEmbedding.isEmpty()) {
            log.warn("Failed to generate embedding for question");
            return List.of();
        }

        // Get all document chunks
        List<DocumentChunk> allChunks = documentChunkRepository.findAll();

        if (allChunks.isEmpty()) {
            log.info("No document chunks found in database");
            return List.of();
        }

        // Calculate similarity scores
        List<ChunkScore> scoredChunks = new ArrayList<>();

        for (DocumentChunk chunk : allChunks) {
            List<Double> chunkEmbedding = embeddingService.parseEmbeddingString(
                    chunk.getEmbeddingVector());

            if (!chunkEmbedding.isEmpty()) {
                double similarity = embeddingService.cosineSimilarity(
                        questionEmbedding, chunkEmbedding);

                if (similarity >= confidenceThreshold) {
                    scoredChunks.add(new ChunkScore(chunk, similarity));
                }
            }
        }

        // Sort by similarity score (descending) and take top results
        List<ChunkScore> topChunks = scoredChunks.stream()
                .sorted(Comparator.comparingDouble(ChunkScore::getScore).reversed())
                .limit(maxRetrievalResults)
                .collect(Collectors.toList());

        log.info("Found {} relevant chunks with similarity >= {}",
                topChunks.size(), confidenceThreshold);

        // Convert to Citations
        return topChunks.stream()
                .map(this::convertToCitation)
                .collect(Collectors.toList());
    }

    private Citation convertToCitation(ChunkScore chunkScore) {
        DocumentChunk chunk = chunkScore.getChunk();

        // Get document metadata
        Document document = documentRepository.findByDocumentId(chunk.getDocumentId())
                .orElse(null);

        Citation.CitationBuilder builder = Citation.builder()
                .documentId(chunk.getDocumentId())
                .pageNumber(chunk.getPageNumber())
                .section(chunk.getSection())
                .startLine(chunk.getStartLine())
                .endLine(chunk.getEndLine())
                // .snippet(chunk.getContent())  // Removed: snippet not included in response
                .relevanceScore(chunkScore.getScore());

        if (document != null) {
            builder.fileName(document.getFileName())
                   .version(document.getVersion());
        }

        return builder.build();
    }

    public String buildContextFromCitations(List<Citation> citations) {
        if (citations == null || citations.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();
        context.append("Retrieved information from company documents:\n\n");

        for (int i = 0; i < citations.size(); i++) {
            Citation citation = citations.get(i);
            context.append(String.format("[Source %d] %s (v%s)",
                    i + 1,
                    citation.getFileName(),
                    citation.getVersion()));

            if (citation.getPageNumber() != null) {
                context.append(String.format(", page %d", citation.getPageNumber()));
            }

            context.append(":\n");
            context.append(citation.getSnippet());
            context.append("\n\n");
        }

        return context.toString();
    }

    // Inner class to hold chunk with similarity score
    @RequiredArgsConstructor
    private static class ChunkScore {
        private final DocumentChunk chunk;
        private final double score;

        public DocumentChunk getChunk() {
            return chunk;
        }

        public double getScore() {
            return score;
        }
    }
}

