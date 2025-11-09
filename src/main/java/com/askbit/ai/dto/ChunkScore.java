package com.askbit.ai.dto;

import com.askbit.ai.model.DocumentChunk;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Holds a document chunk with its similarity score for ranking purposes
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChunkScore implements Serializable {

    private static final long serialVersionUID = 1L;

    private DocumentChunk chunk;
    private double score;
}

