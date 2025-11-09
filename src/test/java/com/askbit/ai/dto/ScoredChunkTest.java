package com.askbit.ai.dto;

import com.askbit.ai.model.DocumentChunk;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ScoredChunkTest {

    @Test
    void threeArgConstructor_shouldCreateValidObject() {
        // Arrange
        DocumentChunk chunk = new DocumentChunk();
        chunk.setId(1L);
        chunk.setContent("Test content");

        // Act
        ScoredChunk scoredChunk = new ScoredChunk(chunk, 0.85, 0.65);

        // Assert
        assertThat(scoredChunk.getChunk()).isEqualTo(chunk);
        assertThat(scoredChunk.getVectorScore()).isEqualTo(0.85);
        assertThat(scoredChunk.getKeywordScore()).isEqualTo(0.65);
        assertThat(scoredChunk.getHybridScore()).isEqualTo(0.0);
    }

    @Test
    void fourArgConstructor_shouldCreateValidObject() {
        // Arrange
        DocumentChunk chunk = new DocumentChunk();
        chunk.setId(2L);
        chunk.setContent("Another chunk");

        // Act
        ScoredChunk scoredChunk = new ScoredChunk(chunk, 0.90, 0.70, 0.83);

        // Assert
        assertThat(scoredChunk.getChunk()).isEqualTo(chunk);
        assertThat(scoredChunk.getVectorScore()).isEqualTo(0.90);
        assertThat(scoredChunk.getKeywordScore()).isEqualTo(0.70);
        assertThat(scoredChunk.getHybridScore()).isEqualTo(0.83);
    }

    @Test
    void noArgsConstructor_shouldWork() {
        // Act
        ScoredChunk scoredChunk = new ScoredChunk();

        // Assert
        assertThat(scoredChunk).isNotNull();
    }

    @Test
    void settersAndGetters_shouldWork() {
        // Arrange
        DocumentChunk chunk = new DocumentChunk();
        chunk.setId(3L);
        chunk.setContent("Test");
        ScoredChunk scoredChunk = new ScoredChunk();

        // Act
        scoredChunk.setChunk(chunk);
        scoredChunk.setVectorScore(0.95);
        scoredChunk.setKeywordScore(0.80);
        scoredChunk.setHybridScore(0.90);

        // Assert
        assertThat(scoredChunk.getChunk()).isEqualTo(chunk);
        assertThat(scoredChunk.getVectorScore()).isEqualTo(0.95);
        assertThat(scoredChunk.getKeywordScore()).isEqualTo(0.80);
        assertThat(scoredChunk.getHybridScore()).isEqualTo(0.90);
    }

    @Test
    void shouldCalculateHybridScore() {
        // Arrange
        DocumentChunk chunk = new DocumentChunk();
        chunk.setContent("Test content");
        ScoredChunk scoredChunk = new ScoredChunk(chunk, 0.8, 0.6);
        double vectorWeight = 0.7;
        double keywordWeight = 0.3;

        // Act - Calculate hybrid score (70% vector + 30% keyword)
        double calculatedHybridScore = (scoredChunk.getVectorScore() * vectorWeight) +
                                       (scoredChunk.getKeywordScore() * keywordWeight);
        scoredChunk.setHybridScore(calculatedHybridScore);

        // Assert
        assertThat(scoredChunk.getHybridScore()).isCloseTo(0.74, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void equals_shouldWorkCorrectly() {
        // Arrange
        DocumentChunk chunk = new DocumentChunk();
        chunk.setId(1L);
        chunk.setContent("Same content");

        ScoredChunk scored1 = new ScoredChunk(chunk, 0.9, 0.8, 0.87);
        ScoredChunk scored2 = new ScoredChunk(chunk, 0.9, 0.8, 0.87);

        // Assert
        assertThat(scored1).isEqualTo(scored2);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        // Arrange
        DocumentChunk chunk = new DocumentChunk();
        chunk.setId(1L);
        ScoredChunk scoredChunk = new ScoredChunk(chunk, 0.85, 0.75, 0.82);

        // Act
        int hash1 = scoredChunk.hashCode();
        int hash2 = scoredChunk.hashCode();

        // Assert
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void toString_shouldWork() {
        // Arrange
        DocumentChunk chunk = new DocumentChunk();
        chunk.setId(1L);
        chunk.setContent("Test");
        ScoredChunk scoredChunk = new ScoredChunk(chunk, 0.92, 0.78, 0.88);

        // Act
        String result = scoredChunk.toString();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).contains("0.92");
        assertThat(result).contains("0.78");
        assertThat(result).contains("0.88");
    }

    @Test
    void shouldHandlePerfectVectorScore() {
        // Arrange
        DocumentChunk chunk = new DocumentChunk();
        chunk.setContent("Perfect match");

        // Act
        ScoredChunk scoredChunk = new ScoredChunk(chunk, 1.0, 0.5);

        // Assert
        assertThat(scoredChunk.getVectorScore()).isEqualTo(1.0);
    }

    @Test
    void shouldHandleZeroScores() {
        // Arrange
        DocumentChunk chunk = new DocumentChunk();
        chunk.setContent("No match");

        // Act
        ScoredChunk scoredChunk = new ScoredChunk(chunk, 0.0, 0.0);

        // Assert
        assertThat(scoredChunk.getVectorScore()).isZero();
        assertThat(scoredChunk.getKeywordScore()).isZero();
        assertThat(scoredChunk.getHybridScore()).isZero();
    }

    @Test
    void shouldStoreCompleteChunkData() {
        // Arrange
        DocumentChunk chunk = new DocumentChunk();
        chunk.setId(100L);
        chunk.setDocumentId("doc-abc-123");
        chunk.setChunkIndex(10);
        chunk.setContent("Complete chunk content for testing");
        chunk.setPageNumber(5);
        chunk.setSection("Introduction");

        // Act
        ScoredChunk scoredChunk = new ScoredChunk(chunk, 0.88, 0.72, 0.83);

        // Assert
        assertThat(scoredChunk.getChunk().getId()).isEqualTo(100L);
        assertThat(scoredChunk.getChunk().getDocumentId()).isEqualTo("doc-abc-123");
        assertThat(scoredChunk.getChunk().getChunkIndex()).isEqualTo(10);
        assertThat(scoredChunk.getChunk().getContent()).isEqualTo("Complete chunk content for testing");
        assertThat(scoredChunk.getChunk().getPageNumber()).isEqualTo(5);
        assertThat(scoredChunk.getChunk().getSection()).isEqualTo("Introduction");
        assertThat(scoredChunk.getVectorScore()).isEqualTo(0.88);
        assertThat(scoredChunk.getKeywordScore()).isEqualTo(0.72);
        assertThat(scoredChunk.getHybridScore()).isEqualTo(0.83);
    }

    @Test
    void shouldAllowUpdatingHybridScore() {
        // Arrange
        DocumentChunk chunk = new DocumentChunk();
        chunk.setContent("Test");
        ScoredChunk scoredChunk = new ScoredChunk(chunk, 0.9, 0.7);

        // Act - Initially hybrid score is 0
        assertThat(scoredChunk.getHybridScore()).isZero();

        // Update hybrid score after calculation
        scoredChunk.setHybridScore(0.84);

        // Assert
        assertThat(scoredChunk.getHybridScore()).isEqualTo(0.84);
    }

    @Test
    void shouldAllowUpdatingKeywordScore() {
        // Arrange
        DocumentChunk chunk = new DocumentChunk();
        chunk.setContent("Test");
        ScoredChunk scoredChunk = new ScoredChunk(chunk, 0.9, 0.5);

        // Act - Update keyword score (e.g., when merging results)
        scoredChunk.setKeywordScore(0.8);

        // Assert
        assertThat(scoredChunk.getKeywordScore()).isEqualTo(0.8);
    }
}

