package com.askbit.ai.controller;

import com.askbit.ai.dto.DocumentUploadResponse;
import com.askbit.ai.model.Document;
import com.askbit.ai.service.DocumentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DocumentController {

    private final DocumentProcessingService documentProcessingService;

    @PostMapping("/upload")
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "version", required = false, defaultValue = "1.0") String version,
            @RequestParam(value = "description", required = false) String description) {

        log.info("Uploading document: {}", file.getOriginalFilename());

        try {
            DocumentUploadResponse response = documentProcessingService
                    .uploadAndProcessDocument(file, version, description);

            return response.getSuccess()
                    ? ResponseEntity.ok(response)
                    : ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);

        } catch (IOException e) {
            log.error("Error uploading document", e);

            DocumentUploadResponse errorResponse = DocumentUploadResponse.builder()
                    .fileName(file.getOriginalFilename())
                    .success(false)
                    .message("Failed to upload document: " + e.getMessage())
                    .build();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        List<Document> documents = documentProcessingService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{documentId}")
    public ResponseEntity<Document> getDocument(@PathVariable String documentId) {
        try {
            Document document = documentProcessingService.getDocument(documentId);
            return ResponseEntity.ok(document);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String id) {
        try {
            documentProcessingService.deleteDocument(Long.parseLong(id));
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Clean up duplicate chunks for a specific document
     */
    @PostMapping("/{documentId}/cleanup/duplicates")
    public ResponseEntity<CleanupResponse> cleanupDuplicateChunks(@PathVariable String documentId) {
        try {
            log.info("Cleaning up duplicate chunks for document: {}", documentId);
            int removedCount = documentProcessingService.cleanupDuplicateChunks(documentId);

            return ResponseEntity.ok(CleanupResponse.builder()
                    .documentId(documentId)
                    .success(true)
                    .message("Duplicate chunks cleanup completed")
                    .duplicatesRemoved(removedCount)
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Clean up invalid chunks (null content, etc.) for a specific document
     */
    @PostMapping("/{documentId}/cleanup/invalid")
    public ResponseEntity<CleanupResponse> cleanupInvalidChunks(@PathVariable String documentId) {
        try {
            log.info("Cleaning up invalid chunks for document: {}", documentId);
            int removedCount = documentProcessingService.cleanupInvalidChunks(documentId);

            return ResponseEntity.ok(CleanupResponse.builder()
                    .documentId(documentId)
                    .success(true)
                    .message("Invalid chunks cleanup completed")
                    .invalidRemoved(removedCount)
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Comprehensive cleanup: removes duplicates, invalid chunks, and re-indexes
     */
    @PostMapping("/{documentId}/cleanup/full")
    public ResponseEntity<CleanupResponse> cleanupAndReindex(@PathVariable String documentId) {
        try {
            log.info("Full cleanup and re-index for document: {}", documentId);
            documentProcessingService.cleanupAndReindexDocument(documentId);

            Document document = documentProcessingService.getDocument(documentId);

            return ResponseEntity.ok(CleanupResponse.builder()
                    .documentId(documentId)
                    .success(true)
                    .message("Full cleanup and re-indexing completed")
                    .remainingChunks(document.getChunkCount())
                    .build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error during cleanup and re-index", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CleanupResponse.builder()
                            .documentId(documentId)
                            .success(false)
                            .message("Error during cleanup: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Find all duplicate documents
     */
    @GetMapping("/duplicates")
    public ResponseEntity<Map<String, List<Document>>> findDuplicateDocuments() {
        log.info("Finding duplicate documents");
        Map<String, List<Document>> duplicates = documentProcessingService.findDuplicateDocuments();
        return ResponseEntity.ok(duplicates);
    }

    /**
     * Clean up all duplicate documents (keeps newest, removes older)
     */
    @PostMapping("/cleanup/duplicates")
    public ResponseEntity<DocumentCleanupResponse> cleanupDuplicateDocuments() {
        try {
            log.info("Cleaning up duplicate documents");
            int removedCount = documentProcessingService.cleanupDuplicateDocuments();

            return ResponseEntity.ok(DocumentCleanupResponse.builder()
                    .success(true)
                    .message("Duplicate documents cleanup completed")
                    .duplicateDocumentsRemoved(removedCount)
                    .build());
        } catch (Exception e) {
            log.error("Error during duplicate documents cleanup", e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DocumentCleanupResponse.builder()
                            .success(false)
                            .message("Error during cleanup: " + e.getMessage())
                            .build());
        }
    }

    /**
     * Response class for cleanup operations
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CleanupResponse {
        private String documentId;
        private Boolean success;
        private String message;
        private Integer duplicatesRemoved;
        private Integer invalidRemoved;
        private Integer remainingChunks;
    }

    /**
     * Response class for document-level cleanup operations
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DocumentCleanupResponse {
        private Boolean success;
        private String message;
        private Integer duplicateDocumentsRemoved;
    }
}

