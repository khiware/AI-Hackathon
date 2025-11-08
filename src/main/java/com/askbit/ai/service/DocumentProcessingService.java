package com.askbit.ai.service;

import com.askbit.ai.dto.DocumentUploadResponse;
import com.askbit.ai.model.Document;
import com.askbit.ai.model.DocumentChunk;
import com.askbit.ai.repository.DocumentChunkRepository;
import com.askbit.ai.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentProcessingService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingService embeddingService;
    private final AdminService adminService;

    @Value("${askbit.ai.document.storage-path:./documents}")
    private String documentStoragePath;

    private static final int CHUNK_SIZE = 1000; // characters per chunk
    private static final int CHUNK_OVERLAP = 200; // overlap between chunks

    @Transactional
    public DocumentUploadResponse uploadAndProcessDocument(
            MultipartFile file,
            String version,
            String description) throws IOException {

        long startTime = System.currentTimeMillis();

        // Create storage directory if it doesn't exist
        Path storagePath = Paths.get(documentStoragePath);
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }

        // Generate unique document ID
        String documentId = generateDocumentId(file.getOriginalFilename());

        // Save file
        String fileName = file.getOriginalFilename();
        String fileType = getFileExtension(fileName);
        Path filePath = storagePath.resolve(documentId + "_" + fileName);
        file.transferTo(filePath.toFile());

        // Create document entity
        Document document = Document.builder()
                .documentId(documentId)
                .fileName(fileName)
                .version(version != null ? version : "1.0")
                .fileType(fileType)
                .filePath(filePath.toString())
                .description(description)
                .uploadedAt(LocalDateTime.now())
                .lastModifiedAt(LocalDateTime.now())
                .active(true)
                .indexed(false)
                .fileSize(file.getSize())
                .build();

        documentRepository.save(document);

        // Process document based on type
        List<DocumentChunk> chunks = new ArrayList<>();
        int pageCount = 0;

        try {
            if (fileType.equalsIgnoreCase("pdf")) {
                chunks = processPdfDocument(filePath.toFile(), documentId);
                pageCount = getPageCountFromPdf(filePath.toFile());
            } else if (fileType.equalsIgnoreCase("docx") || fileType.equalsIgnoreCase("doc")) {
                chunks = processDocxDocument(filePath.toFile(), documentId);
            } else if (fileType.equalsIgnoreCase("md") || fileType.equalsIgnoreCase("txt")) {
                chunks = processTextDocument(filePath.toFile(), documentId);
            } else {
                throw new IllegalArgumentException("Unsupported file type: " + fileType);
            }

            // Save chunks
            documentChunkRepository.saveAll(chunks);

            // Update document with page count and indexed status
            document.setPageCount(pageCount);
            document.setIndexed(true);
            documentRepository.save(document);

            // Invalidate cache since document content has changed
            log.info("Invalidating all caches");
            adminService.invalidateAllCaches();

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("Document processed: {} in {}ms, {} chunks created",
                    fileName, processingTime, chunks.size());

            return DocumentUploadResponse.builder()
                    .documentId(documentId)
                    .fileName(fileName)
                    .version(document.getVersion())
                    .success(true)
                    .message("Document uploaded and processed successfully")
                    .pagesProcessed(pageCount)
                    .chunksCreated(chunks.size())
                    .build();

        } catch (Exception e) {
            log.error("Error processing document: {}", fileName, e);
            document.setIndexed(false);
            documentRepository.save(document);

            return DocumentUploadResponse.builder()
                    .documentId(documentId)
                    .fileName(fileName)
                    .success(false)
                    .message("Error processing document: " + e.getMessage())
                    .build();
        }
    }

    private List<DocumentChunk> processPdfDocument(File file, String documentId) throws IOException {
        List<DocumentChunk> chunks = new ArrayList<>();

        try (PDDocument pdfDocument = org.apache.pdfbox.Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = pdfDocument.getNumberOfPages();

            for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                stripper.setStartPage(pageNum);
                stripper.setEndPage(pageNum);
                String pageText = stripper.getText(pdfDocument);

                // Split page into chunks
                List<DocumentChunk> pageChunks = createChunksFromText(
                        pageText, documentId, pageNum);
                chunks.addAll(pageChunks);
            }
        }

        return chunks;
    }

    private List<DocumentChunk> processDocxDocument(File file, String documentId) throws IOException {
        List<DocumentChunk> chunks = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file);
             XWPFDocument document = new XWPFDocument(fis)) {

            StringBuilder contentBuilder = new StringBuilder();
            List<XWPFParagraph> paragraphs = document.getParagraphs();

            for (XWPFParagraph paragraph : paragraphs) {
                contentBuilder.append(paragraph.getText()).append("\n");
            }

            chunks = createChunksFromText(contentBuilder.toString(), documentId, null);
        }

        return chunks;
    }

    private List<DocumentChunk> processTextDocument(File file, String documentId) throws IOException {
        String content = Files.readString(file.toPath());
        return createChunksFromText(content, documentId, null);
    }

    private List<DocumentChunk> createChunksFromText(String text, String documentId, Integer pageNumber) {
        List<DocumentChunk> chunks = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return chunks;
        }

        int textLength = text.length();
        int chunkIndex = 0;
        int startPos = 0;

        while (startPos < textLength) {
            int endPos = Math.min(startPos + CHUNK_SIZE, textLength);

            // Try to break at sentence or word boundary
            if (endPos < textLength) {
                int lastPeriod = text.lastIndexOf('.', endPos);
                int lastSpace = text.lastIndexOf(' ', endPos);

                if (lastPeriod > startPos && (endPos - lastPeriod) < 100) {
                    endPos = lastPeriod + 1;
                } else if (lastSpace > startPos) {
                    endPos = lastSpace + 1;
                }
            }

            String chunkContent = text.substring(startPos, endPos).trim();

            if (!chunkContent.isEmpty()) {
                // Generate embedding for the chunk
                List<Double> embedding = embeddingService.generateEmbedding(chunkContent);

                DocumentChunk chunk = DocumentChunk.builder()
                        .documentId(documentId)
                        .chunkIndex(chunkIndex++)
                        .content(chunkContent)
                        .pageNumber(pageNumber)
                        .embeddingVector(convertEmbeddingToString(embedding))
                        .build();

                chunks.add(chunk);
            }

            startPos = endPos - CHUNK_OVERLAP;
            if (startPos < 0) startPos = endPos;
        }

        return chunks;
    }

    private String generateDocumentId(String fileName) {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1) : "";
    }

    private int getPageCountFromPdf(File file) throws IOException {
        try (PDDocument pdfDocument = org.apache.pdfbox.Loader.loadPDF(file)) {
            return pdfDocument.getNumberOfPages();
        }
    }

    private String convertEmbeddingToString(List<Double> embedding) {
        if (embedding == null || embedding.isEmpty()) {
            return "";
        }
        return embedding.stream()
                .map(String::valueOf)
                .reduce((a, b) -> a + "," + b)
                .orElse("");
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findByActive(true);
    }

    public Document getDocument(String documentId) {
        return documentRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found: " + documentId));
    }

    @Transactional
    public void deleteDocument(String documentId) {
        Document document = getDocument(documentId);
        document.setActive(false);
        documentRepository.save(document);

        // Delete chunks
        documentChunkRepository.deleteByDocumentId(documentId);

        log.info("Document deleted: {}", documentId);
    }
}

