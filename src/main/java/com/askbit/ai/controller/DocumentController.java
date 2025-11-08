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

    @DeleteMapping("/{documentId}")
    public ResponseEntity<Void> deleteDocument(@PathVariable String documentId) {
        try {
            documentProcessingService.deleteDocument(documentId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

