package com.askbit.ai.service;

import com.askbit.ai.dto.Citation;
import com.askbit.ai.repository.DocumentChunkRepository;
import com.askbit.ai.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RetrievalServiceTest {

    @Mock
    private DocumentChunkRepository documentChunkRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private EmbeddingService embeddingService;

    @InjectMocks
    private RetrievalService retrievalService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(retrievalService, "maxRetrievalResults", 5);
        ReflectionTestUtils.setField(retrievalService, "confidenceThreshold", 0.7);
    }

    @Test
    void retrieveRelevantChunks_shouldReturnEmptyForNoDocuments() {
        // Arrange
        when(documentRepository.findLatestVersions()).thenReturn(Collections.emptyList());

        // Act
        List<Citation> result = retrievalService.retrieveRelevantChunks("Test question");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void retrieveRelevantChunks_shouldReturnEmptyForNoEmbedding() {
        // Arrange
        when(documentRepository.findLatestVersions()).thenReturn(Collections.emptyList());

        // Act
        List<Citation> result = retrievalService.retrieveRelevantChunks("Test");

        // Assert
        assertThat(result).isEmpty();
        verify(documentRepository, times(1)).findLatestVersions();
    }

    @Test
    void retrieveRelevantChunksWithVersionFilter_shouldFilterByYear() {
        // Arrange
        when(documentRepository.findLatestVersionsBeforeYear(2023)).thenReturn(Collections.emptyList());

        // Act
        List<Citation> result = retrievalService.retrieveRelevantChunksWithVersionFilter("Question", 2023);

        // Assert
        assertThat(result).isEmpty();
        verify(documentRepository, times(1)).findLatestVersionsBeforeYear(2023);
    }

    @Test
    void retrieveRelevantChunks_shouldHandleException() {
        // Arrange
        when(documentRepository.findLatestVersions()).thenThrow(new RuntimeException("DB error"));

        // Act & Assert - Exception should be thrown
        org.junit.jupiter.api.Assertions.assertThrows(
                RuntimeException.class,
                () -> retrievalService.retrieveRelevantChunks("Test")
        );
    }
}

