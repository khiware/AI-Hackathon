package com.askbit.ai.dto;

import com.askbit.ai.model.DocumentChunk;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChunkScoreTest {

    @Test
    void constructor_shouldCreateValidObject() {
        // Arrange
        DocumentChunk chunk = new DocumentChunk();
        chunk.setId(1L);
        chunk.setDocumentId("doc123");
        chunk.setContent("Test content");
        double score = 0.95;

        // Act
        ChunkScore chunkScore = new ChunkScore(chunk, score);

        // Assert
        assertThat(chunkScore.getChunk()).isEqualTo(chunk);
        assertThat(chunkScore.getScore()).isEqualTo(0.95);
    }

    @Test
    void noArgsConstructor_shouldWork() {
        // Act
        ChunkScore chunkScore = new ChunkScore();

        // Assert
        assertThat(chunkScore).isNotNull();
    }

    @Test
    void settersAndGetters_shouldWork() {
        // Arrange
        DocumentChunk chunk = new DocumentChunk();
        chunk.setId(2L);
        chunk.setContent("Another chunk");
        ChunkScore chunkScore = new ChunkScore();

        // Act
        chunkScore.setChunk(chunk);
        chunkScore.setScore(0.88);

        // Assert
        assertThat(chunkScore.getChunk()).isEqualTo(chunk);
        assertThat(chunkScore.getChunk().getId()).isEqualTo(2L);
        assertThat(chunkScore.getScore()).isEqualTo(0.88);
    }

    @Test
    void equals_shouldWorkCorrectly() {
        // Arrange
        DocumentChunk chunk = new DocumentChunk();
        chunk.setId(1L);
        chunk.setContent("Test");

        ChunkScore score1 = new ChunkScore(chunk, 0.9);
        ChunkScore score2 = new ChunkScore(chunk, 0.9);

        // Assert
        assertThat(score1).isEqualTo(score2);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        // Arrange
        DocumentChunk chunk = new DocumentChunk();
        chunk.setId(1L);
        ChunkScore chunkScore = new ChunkScore(chunk, 0.75);

        // Act
        int hash1 = chunkScore.hashCode();
        int hash2 = chunkScore.hashCode();

        // Assert
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void toString_shouldWork() {
        // Arrange
        DocumentChunk chunk = new DocumentChunk();
        chunk.setId(1L);
        chunk.setContent("Test content");
        ChunkScore chunkScore = new ChunkScore(chunk, 0.92);

        // Act
        String result = chunkScore.toString();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).contains("0.92");
    }

    @Test
    void shouldHandleHighScore() {
        // Arrange
        DocumentChunk chunk = new DocumentChunk();
        chunk.setContent("Perfect match");

        // Act
        ChunkScore chunkScore = new ChunkScore(chunk, 1.0);

        // Assert
        assertThat(chunkScore.getScore()).isEqualTo(1.0);
    }

    @Test
    void shouldHandleLowScore() {
        // Arrange
        DocumentChunk chunk = new DocumentChunk();
        chunk.setContent("Weak match");

        // Act
        ChunkScore chunkScore = new ChunkScore(chunk, 0.01);

        // Assert
        assertThat(chunkScore.getScore()).isEqualTo(0.01);
    }

    @Test
    void shouldHandleZeroScore() {
        // Arrange
        DocumentChunk chunk = new DocumentChunk();
        chunk.setContent("No match");

        // Act
        ChunkScore chunkScore = new ChunkScore(chunk, 0.0);

        // Assert
        assertThat(chunkScore.getScore()).isZero();
    }

    @Test
    void shouldStoreCompleteChunkData() {
        // Arrange
        DocumentChunk chunk = new DocumentChunk();
        chunk.setId(10L);
        chunk.setDocumentId("doc-xyz");
        chunk.setChunkIndex(5);
        chunk.setContent("Complete chunk content");
        chunk.setPageNumber(3);
        chunk.setSection("Section A");

        // Act
        ChunkScore chunkScore = new ChunkScore(chunk, 0.85);

        // Assert
        assertThat(chunkScore.getChunk().getId()).isEqualTo(10L);
        assertThat(chunkScore.getChunk().getDocumentId()).isEqualTo("doc-xyz");
        assertThat(chunkScore.getChunk().getChunkIndex()).isEqualTo(5);
        assertThat(chunkScore.getChunk().getContent()).isEqualTo("Complete chunk content");
        assertThat(chunkScore.getChunk().getPageNumber()).isEqualTo(3);
        assertThat(chunkScore.getChunk().getSection()).isEqualTo("Section A");
        assertThat(chunkScore.getScore()).isEqualTo(0.85);
    }
}

