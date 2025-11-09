package com.askbit.ai.controller;

import com.askbit.ai.dto.DocumentUploadResponse;
import com.askbit.ai.model.Document;
import com.askbit.ai.service.DocumentProcessingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentProcessingService documentProcessingService;

    @Test
    void uploadDocument_shouldReturnSuccessResponse() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-document.pdf",
                "application/pdf",
                "test content".getBytes()
        );

        DocumentUploadResponse response = DocumentUploadResponse.builder()
                .documentId("123")
                .fileName("test-document.pdf")
                .version("1.0")
                .success(true)
                .message("Document uploaded successfully")
                .pagesProcessed(10)
                .chunksCreated(50)
                .build();

        when(documentProcessingService.uploadAndProcessDocument(any(), anyString(), anyString()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/documents/upload")
                        .file(file)
                        .param("version", "1.0")
                        .param("description", "Test document"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentId").value("123"))
                .andExpect(jsonPath("$.fileName").value("test-document.pdf"))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.pagesProcessed").value(10))
                .andExpect(jsonPath("$.chunksCreated").value(50));

        verify(documentProcessingService, times(1))
                .uploadAndProcessDocument(any(), eq("1.0"), eq("Test document"));
    }

    @Test
    void uploadDocument_shouldUseDefaultVersion() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.pdf",
                "application/pdf",
                "content".getBytes()
        );

        DocumentUploadResponse response = DocumentUploadResponse.builder()
                .documentId("456")
                .fileName("document.pdf")
                .version("1.0")
                .success(true)
                .message("Success")
                .build();

        when(documentProcessingService.uploadAndProcessDocument(any(), eq("1.0"), isNull()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/documents/upload")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.version").value("1.0"));
    }

    @Test
    void uploadDocument_shouldHandleIOException() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "error-document.pdf",
                "application/pdf",
                "content".getBytes()
        );

        when(documentProcessingService.uploadAndProcessDocument(any(), eq("2.0"), isNull()))
                .thenThrow(new IOException("Failed to process file"));

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/documents/upload")
                        .file(file)
                        .param("version", "2.0"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.fileName").value("error-document.pdf"))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed to upload document: Failed to process file"));
    }

    @Test
    void uploadDocument_shouldHandleServiceFailure() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "fail-document.pdf",
                "application/pdf",
                "content".getBytes()
        );

        DocumentUploadResponse response = DocumentUploadResponse.builder()
                .fileName("fail-document.pdf")
                .success(false)
                .message("Invalid file format")
                .build();

        when(documentProcessingService.uploadAndProcessDocument(any(), eq("1.0"), isNull()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/documents/upload")
                        .file(file))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid file format"));
    }

    @Test
    void uploadDocument_shouldHandleDifferentFileTypes() throws Exception {
        // Arrange - DOCX file
        MockMultipartFile docxFile = new MockMultipartFile(
                "file",
                "document.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "docx content".getBytes()
        );

        DocumentUploadResponse response = DocumentUploadResponse.builder()
                .documentId("789")
                .fileName("document.docx")
                .success(true)
                .message("DOCX uploaded")
                .build();

        when(documentProcessingService.uploadAndProcessDocument(any(), eq("1.5"), isNull()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/documents/upload")
                        .file(docxFile)
                        .param("version", "1.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("document.docx"));
    }

    @Test
    void getAllDocuments_shouldReturnListOfDocuments() throws Exception {
        // Arrange
        Document doc1 = new Document();
        doc1.setId(1L);
        doc1.setFileName("doc1.pdf");
        doc1.setVersion("1.0");
        doc1.setUploadedAt(LocalDateTime.now());

        Document doc2 = new Document();
        doc2.setId(2L);
        doc2.setFileName("doc2.pdf");
        doc2.setVersion("2.0");
        doc2.setUploadedAt(LocalDateTime.now());

        List<Document> documents = Arrays.asList(doc1, doc2);

        when(documentProcessingService.getAllDocuments()).thenReturn(documents);

        // Act & Assert
        mockMvc.perform(get("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fileName").value("doc1.pdf"))
                .andExpect(jsonPath("$[1].fileName").value("doc2.pdf"));

        verify(documentProcessingService, times(1)).getAllDocuments();
    }

    @Test
    void getAllDocuments_shouldReturnEmptyList() throws Exception {
        // Arrange
        when(documentProcessingService.getAllDocuments()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void deleteDocument_shouldReturnOk() throws Exception {
        // Arrange
        doNothing().when(documentProcessingService).deleteDocument(123L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/documents/123"))
                .andExpect(status().isOk());

        verify(documentProcessingService, times(1)).deleteDocument(123L);
    }

    @Test
    void deleteDocument_shouldReturnNotFoundForInvalidId() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Document not found"))
                .when(documentProcessingService).deleteDocument(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/documents/999"))
                .andExpect(status().isNotFound());

        verify(documentProcessingService, times(1)).deleteDocument(999L);
    }

    @Test
    void deleteDocument_shouldHandleInvalidIdFormat() throws Exception {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid ID"))
                .when(documentProcessingService).deleteDocument(anyLong());

        // Act & Assert
        mockMvc.perform(delete("/api/v1/documents/abc"))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadDocument_shouldHandleNullDescription() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "doc.pdf",
                "application/pdf",
                "content".getBytes()
        );

        DocumentUploadResponse response = DocumentUploadResponse.builder()
                .documentId("111")
                .fileName("doc.pdf")
                .success(true)
                .build();

        when(documentProcessingService.uploadAndProcessDocument(any(), anyString(), isNull()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/documents/upload")
                        .file(file)
                        .param("version", "1.0"))
                .andExpect(status().isOk());

        verify(documentProcessingService, times(1))
                .uploadAndProcessDocument(any(), eq("1.0"), isNull());
    }

    @Test
    void uploadDocument_shouldHandleLargeFile() throws Exception {
        // Arrange
        byte[] largeContent = new byte[1024 * 1024]; // 1MB
        MockMultipartFile largeFile = new MockMultipartFile(
                "file",
                "large-doc.pdf",
                "application/pdf",
                largeContent
        );

        DocumentUploadResponse response = DocumentUploadResponse.builder()
                .documentId("large-1")
                .fileName("large-doc.pdf")
                .success(true)
                .pagesProcessed(100)
                .chunksCreated(500)
                .build();

        when(documentProcessingService.uploadAndProcessDocument(any(), anyString(), anyString()))
                .thenReturn(response);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/documents/upload")
                        .file(largeFile)
                        .param("version", "1.0")
                        .param("description", "Large file"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pagesProcessed").value(100))
                .andExpect(jsonPath("$.chunksCreated").value(500));
    }
}

