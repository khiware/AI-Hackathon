package com.askbit.ai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.embedding.Embedding;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmbeddingServiceTest {

    @Mock
    private EmbeddingModel embeddingModel;

    private EmbeddingService embeddingService;

    @BeforeEach
    void setUp() {
        embeddingService = new EmbeddingService(embeddingModel);
    }

    @Test
    void generateEmbedding_shouldReturnEmbeddingForValidText() {
        // Arrange
        String text = "Test text";
        float[] embeddingArray = {0.1f, 0.2f, 0.3f};

        Embedding embedding = mock(Embedding.class);
        when(embedding.getOutput()).thenReturn(embeddingArray);

        EmbeddingResponse response = mock(EmbeddingResponse.class);
        when(response.getResults()).thenReturn(List.of(embedding));
        when(embeddingModel.embedForResponse(any(List.class))).thenReturn(response);

        // Act
        List<Double> result = embeddingService.generateEmbedding(text);

        // Assert
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).isCloseTo(0.1, org.assertj.core.data.Offset.offset(0.001));
        assertThat(result.get(1)).isCloseTo(0.2, org.assertj.core.data.Offset.offset(0.001));
        assertThat(result.get(2)).isCloseTo(0.3, org.assertj.core.data.Offset.offset(0.001));
        verify(embeddingModel, times(1)).embedForResponse(any(List.class));
    }

    @Test
    void generateEmbedding_shouldReturnEmptyForNullText() {
        // Act
        List<Double> result = embeddingService.generateEmbedding(null);

        // Assert
        assertThat(result).isEmpty();
        verify(embeddingModel, never()).embedForResponse(any());
    }

    @Test
    void generateEmbedding_shouldReturnEmptyForEmptyText() {
        // Act
        List<Double> result = embeddingService.generateEmbedding("   ");

        // Assert
        assertThat(result).isEmpty();
        verify(embeddingModel, never()).embedForResponse(any());
    }

    @Test
    void generateEmbedding_shouldHandleException() {
        // Arrange
        when(embeddingModel.embedForResponse(any())).thenThrow(new RuntimeException("API error"));

        // Act
        List<Double> result = embeddingService.generateEmbedding("Test");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void cosineSimilarity_shouldCalculateCorrectly() {
        // Arrange
        List<Double> vec1 = Arrays.asList(1.0, 0.0, 0.0);
        List<Double> vec2 = Arrays.asList(1.0, 0.0, 0.0);

        // Act
        double result = embeddingService.cosineSimilarity(vec1, vec2);

        // Assert
        assertThat(result).isEqualTo(1.0);
    }

    @Test
    void cosineSimilarity_shouldReturnZeroForNullVectors() {
        // Act
        double result = embeddingService.cosineSimilarity(null, Arrays.asList(1.0, 2.0));

        // Assert
        assertThat(result).isEqualTo(0.0);
    }

    @Test
    void cosineSimilarity_shouldReturnZeroForDifferentSizes() {
        // Arrange
        List<Double> vec1 = Arrays.asList(1.0, 2.0);
        List<Double> vec2 = Arrays.asList(1.0, 2.0, 3.0);

        // Act
        double result = embeddingService.cosineSimilarity(vec1, vec2);

        // Assert
        assertThat(result).isEqualTo(0.0);
    }
}

