package com.askbit.ai.dto;

import com.askbit.ai.model.DocumentChunk;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Helper class to store a document chunk with multiple scores for hybrid search ranking.
 * Used in hybrid retrieval to combine vector similarity and keyword matching scores.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScoredChunk implements Serializable {

    private static final long serialVersionUID = 1L;

    private DocumentChunk chunk;
    private double vectorScore;
    private double keywordScore;
    private double hybridScore;

    /**
     * Constructor for initial scoring (before hybrid score calculation)
     */
    public ScoredChunk(DocumentChunk chunk, double vectorScore, double keywordScore) {
        this.chunk = chunk;
        this.vectorScore = vectorScore;
        this.keywordScore = keywordScore;
        this.hybridScore = 0.0;
    }
}

