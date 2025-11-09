package com.askbit.ai.service;

import com.askbit.ai.dto.DocumentUploadResponse;
import com.askbit.ai.model.Document;
import com.askbit.ai.repository.DocumentChunkRepository;
import com.askbit.ai.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentProcessingServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private DocumentChunkRepository documentChunkRepository;

    @Mock
    private EmbeddingService embeddingService;

    @Mock
    private AdminService adminService;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private DocumentProcessingService documentProcessingService;

    @BeforeEach
    void setUp() {
        // Use test resources folder for document storage during tests
        String testResourcePath = "src/test/resources/test-documents";
        ReflectionTestUtils.setField(documentProcessingService, "documentStoragePath", testResourcePath);
    }

    @Test
    void uploadAndProcessDocument_shouldReturnErrorForNullFileName() throws IOException {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn(null);

        // Act
        DocumentUploadResponse response = documentProcessingService
                .uploadAndProcessDocument(multipartFile, "1.0", "Test");

        // Assert
        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getMessage()).contains("Invalid file name");
    }

    @Test
    void uploadAndProcessDocument_shouldReturnErrorForEmptyFileName() throws IOException {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("   ");

        // Act
        DocumentUploadResponse response = documentProcessingService
                .uploadAndProcessDocument(multipartFile, "1.0", "Test");

        // Assert
        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getMessage()).contains("Invalid file name");
    }

    @Test
    void uploadAndProcessDocument_shouldReturnErrorForDuplicateDocument() throws IOException {
        // Arrange
        String fileName = "existing.pdf";
        when(multipartFile.getOriginalFilename()).thenReturn(fileName);

        Document existingDoc = new Document();
        existingDoc.setDocumentId("doc123");
        existingDoc.setFileName(fileName);
        existingDoc.setVersion("1.0");

        when(documentRepository.findByFileNameAndVersionAndActive(fileName, "1.0", true))
                .thenReturn(Optional.of(existingDoc));

        // Act
        DocumentUploadResponse response = documentProcessingService
                .uploadAndProcessDocument(multipartFile, "1.0", "Test");

        // Assert
        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getMessage()).contains("already exists");
        assertThat(response.getDocumentId()).isEqualTo("doc123");
    }

    @Test
    void uploadAndProcessDocument_shouldReturnErrorForUnsupportedFileType() throws IOException {
        // Arrange
        when(multipartFile.getOriginalFilename()).thenReturn("test.exe");
        when(documentRepository.findByFileNameAndVersionAndActive(anyString(), anyString(), anyBoolean()))
                .thenReturn(Optional.empty());

        // Act & Assert - Should throw IllegalArgumentException
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> documentProcessingService.uploadAndProcessDocument(multipartFile, "1.0", "Test")
        );
    }

    @Test
    void getAllDocuments_shouldReturnAllDocuments() {
        // Arrange
        Document doc1 = new Document();
        doc1.setFileName("doc1.pdf");

        Document doc2 = new Document();
        doc2.setFileName("doc2.pdf");

        when(documentRepository.findByActive(true)).thenReturn(List.of(doc1, doc2));

        // Act
        List<Document> result = documentProcessingService.getAllDocuments();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFileName()).isEqualTo("doc1.pdf");
        verify(documentRepository, times(1)).findByActive(true);
    }

    @Test
    void getAllDocuments_shouldReturnEmptyList() {
        // Arrange
        when(documentRepository.findByActive(true)).thenReturn(Collections.emptyList());

        // Act
        List<Document> result = documentProcessingService.getAllDocuments();

        // Assert
        assertThat(result).isEmpty();
        verify(documentRepository, times(1)).findByActive(true);
    }

    @Test
    void deleteDocument_shouldDeleteDocumentAndChunks() {
        // Arrange
        Long docId = 123L;
        Document document = new Document();
        document.setId(docId);
        document.setDocumentId("doc123");
        document.setActive(true);

        when(documentRepository.findById(docId)).thenReturn(Optional.of(document));
        doNothing().when(documentChunkRepository).deleteByDocumentId(anyString());
        doNothing().when(documentRepository).deleteById(docId);

        // Act
        documentProcessingService.deleteDocument(docId);

        // Assert
        verify(documentChunkRepository, times(1)).deleteByDocumentId("doc123");
        verify(documentRepository, times(1)).deleteById(docId);
    }

    @Test
    void deleteDocument_shouldThrowExceptionForInvalidId() {
        // Arrange
        when(documentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> documentProcessingService.deleteDocument(999L)
        );
    }
}

