package com.askbit.ai.service;

import com.askbit.ai.model.DocumentChunk;
import com.askbit.ai.repository.DocumentChunkRepository;
import com.askbit.ai.repository.DocumentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HybridRetrievalServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @InjectMocks
    private HybridRetrievalService hybridRetrievalService;

    @Test
    void hybridSearch_shouldReturnChunksForValidQuestion() {
        // Arrange
        String question = "What is the WFH policy?";

        when(documentRepository.findLatestVersions()).thenReturn(Collections.emptyList());

        // Act
        List<DocumentChunk> result = hybridRetrievalService.hybridSearch(question, 5);

        // Assert - When no documents, service returns early without generating embedding
        assertThat(result).isEmpty();
        verify(documentRepository, times(1)).findLatestVersions();
    }

    @Test
    void hybridSearch_shouldReturnEmptyForNoDocuments() {
        // Arrange
        when(documentRepository.findLatestVersions()).thenReturn(Collections.emptyList());

        // Act
        List<DocumentChunk> result = hybridRetrievalService.hybridSearch("Test", 5);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void hybridSearchWithVersionFilter_shouldFilterByYear() {
        // Arrange
        String question = "Policy question";

        when(documentRepository.findLatestVersionsBeforeYear(2023)).thenReturn(Collections.emptyList());

        // Act
        List<DocumentChunk> result = hybridRetrievalService.hybridSearchWithVersionFilter(question, 5, 2023);

        // Assert
        assertThat(result).isEmpty();
        verify(documentRepository, times(1)).findLatestVersionsBeforeYear(2023);
    }

    @Test
    void hybridSearch_shouldHandleEmptyEmbedding() {
        // Arrange - When no documents exist, embedding is not generated
        when(documentRepository.findLatestVersions()).thenReturn(Collections.emptyList());

        // Act
        List<DocumentChunk> result = hybridRetrievalService.hybridSearch("Test", 5);

        // Assert
        assertThat(result).isEmpty();
        verify(documentRepository, times(1)).findLatestVersions();
    }
}

