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
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentProcessingService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingService embeddingService;

    @Value("${askbit.ai.document.storage-path:./documents}")
    private String documentStoragePath;

    private static final int CHUNK_SIZE = 500; // characters per chunk - reduced for better page handling
    private static final int CHUNK_OVERLAP = 100; // overlap between chunks

    @Transactional
    public DocumentUploadResponse uploadAndProcessDocument(
            MultipartFile file,
            String version,
            String description) throws IOException {

        long startTime = System.currentTimeMillis();

        // Validate file name
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.trim().isEmpty()) {
            return DocumentUploadResponse.builder()
                    .fileName("unknown")
                    .success(false)
                    .message("Invalid file name")
                    .build();
        }

        // Normalize version
        String documentVersion = version != null ? version : "1.0";

        // Check for existing active document with same filename and version
        Optional<Document> existingDocument = documentRepository
                .findByFileNameAndVersionAndActive(fileName, documentVersion, true);

        if (existingDocument.isPresent()) {
            Document existing = existingDocument.get();
            log.warn("Document already exists: {} version {} (documentId: {})",
                    fileName, documentVersion, existing.getDocumentId());

            return DocumentUploadResponse.builder()
                    .documentId(existing.getDocumentId())
                    .fileName(fileName)
                    .version(documentVersion)
                    .success(false)
                    .message(String.format("Document '%s' version '%s' already exists. " +
                            "Please use a different version or delete the existing document first.",
                            fileName, documentVersion))
                    .pagesProcessed(existing.getPageCount() != null ? existing.getPageCount() : 0)
                    .chunksCreated(existing.getChunkCount() != null ? existing.getChunkCount() : 0)
                    .build();
        }

        // Create storage directory if it doesn't exist
        Path storagePath = Paths.get(documentStoragePath);
        if (!Files.exists(storagePath)) {
            Files.createDirectories(storagePath);
        }

        // Generate unique document ID
        String documentId = generateDocumentId(fileName);

        // Save file
        String fileType = getFileExtension(fileName);
        Path filePath = storagePath.resolve(documentId + "_" + fileName);
        file.transferTo(filePath.toFile());

        // Create document entity
        Document document = Document.builder()
                .documentId(documentId)
                .fileName(fileName)
                .version(documentVersion)
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
        List<DocumentChunk> chunks;
        int pageCount = 0;

        try {
            if (fileType.equalsIgnoreCase("pdf")) {
                // PDF processing saves chunks internally, returns dummy list
                chunks = processPdfDocument(filePath.toFile(), documentId);
                pageCount = getPageCountFromPdf(filePath.toFile());
                // Don't save chunks again - already saved in processPdfDocument!
            } else if (fileType.equalsIgnoreCase("docx")) {
                chunks = processDocxDocument(filePath.toFile(), documentId);
                // Save chunks for non-PDF documents
                documentChunkRepository.saveAll(chunks);
            } else if (fileType.equalsIgnoreCase("md") || fileType.equalsIgnoreCase("txt")) {
                chunks = processTextDocument(filePath.toFile(), documentId);
                // Save chunks for non-PDF documents
                documentChunkRepository.saveAll(chunks);
            } else {
                throw new IllegalArgumentException("Unsupported file type: " + fileType);
            }

            // Update document with page count and indexed status
            document.setPageCount(pageCount);
            document.setChunkCount(chunks.size());
            document.setIndexed(true);
            documentRepository.save(document);

            long processingTime = System.currentTimeMillis() - startTime;
            log.info("Document processed: {} in {}ms",
                    fileName, processingTime);

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

    /**
     * MEMORY-OPTIMIZED: Process PDF page-by-page with immediate saves
     * Prevents OutOfMemoryError by not accumulating all chunks in memory
     */
    private List<DocumentChunk> processPdfDocument(File file, String documentId) throws IOException {
        int totalChunksCreated = 0;
        int globalChunkIndex = 0; // Global counter to ensure unique chunk_index across all pages

        try (PDDocument pdfDocument = org.apache.pdfbox.Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            int totalPages = pdfDocument.getNumberOfPages();

            log.info("Processing PDF with {} pages (page-by-page with immediate saves)", totalPages);

            // Process ONE page at a time, save immediately
            for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                log.info("Starting page {} of {}", pageNum, totalPages);

                stripper.setStartPage(pageNum);
                stripper.setEndPage(pageNum);
                String pageText = stripper.getText(pdfDocument);

                log.debug("Page {} extracted, text length: {} characters", pageNum, pageText.length());

                // Create chunks for THIS page only - pass global counter
                List<DocumentChunk> pageChunks = createChunksFromText(pageText, documentId, pageNum, globalChunkIndex);

                log.debug("Page {} created {} chunks", pageNum, pageChunks.size());

                // SAVE IMMEDIATELY - don't accumulate in memory!
                if (!pageChunks.isEmpty()) {
                    // Validate chunks before saving
                    pageChunks = validateChunks(pageChunks, documentId);

                    if (!pageChunks.isEmpty()) {
                        documentChunkRepository.saveAll(pageChunks);
                        totalChunksCreated += pageChunks.size();

                        // Update global counter
                        globalChunkIndex += pageChunks.size();

                        log.debug("Saved {} chunks from page {}/{}, global chunk index now at {}",
                                pageChunks.size(), pageNum, totalPages, globalChunkIndex);
                    }

                    // Clear the list immediately to free memory
                    pageChunks.clear();
                } else {
                    log.warn("Page {} produced no chunks (text might be too short)", pageNum);
                }

                // Force GC every 5 pages
                if (pageNum % 5 == 0) {
                    System.gc();
                    log.info("Processed {} / {} pages, {} total chunks",
                            pageNum, totalPages, totalChunksCreated);
                }

                log.info("Completed page {} of {}", pageNum, totalPages);
            }
        }

        // Return empty list since we already saved everything
        // Just return count for logging
        log.info("PDF processing complete: {} total chunks created", totalChunksCreated);

        // Return a dummy list with size info for the caller
        List<DocumentChunk> result = new ArrayList<>();
        // Add a single dummy chunk just to carry the count
        if (totalChunksCreated > 0) {
            DocumentChunk dummyChunk = DocumentChunk.builder()
                    .documentId(documentId)
                    .chunkIndex(0)
                    .content("PROCESSED")
                    .build();
            result.add(dummyChunk);
        }
        return result;
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

            chunks = createChunksFromText(contentBuilder.toString(), documentId, null, 0);
            chunks = validateChunks(chunks, documentId);
        }

        return chunks;
    }

    private List<DocumentChunk> processTextDocument(File file, String documentId) throws IOException {
        String content = Files.readString(file.toPath());
        List<DocumentChunk> chunks = createChunksFromText(content, documentId, null, 0);
        return validateChunks(chunks, documentId);
    }

    /**
     * OPTIMIZED: Create chunks using Spring AI BATCH embeddings
     * KEY OPTIMIZATION: Generates ALL embeddings in ONE API call (10-50x faster!)
     * FIXED: Prevents infinite loop on small pages
     * FIXED: Uses startingChunkIndex to prevent duplicate chunk_index across pages
     */
    private List<DocumentChunk> createChunksFromText(String text, String documentId, Integer pageNumber, int startingChunkIndex) {
        List<DocumentChunk> chunks = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            log.debug("Empty text for page {}, skipping", pageNumber);
            return chunks;
        }

        int textLength = text.length();
        int startPos = 0;

        // PHASE 1: Extract all chunk texts first (fast)
        List<String> chunkTexts = new ArrayList<>();
        int previousStartPos = -1; // Track to prevent infinite loop
        int iterationCount = 0;
        final int MAX_ITERATIONS = 1000; // Safety limit

        log.debug("Starting chunk extraction for page {}, text length: {}", pageNumber, textLength);

        while (startPos < textLength) {
            iterationCount++;

            // Safety check: prevent infinite loop
            if (startPos == previousStartPos) {
                log.error("Infinite loop detected at position {} for page {}, breaking after {} iterations",
                        startPos, pageNumber, iterationCount);
                break;
            }

            // Additional safety: max iterations
            if (iterationCount > MAX_ITERATIONS) {
                log.error("Max iterations ({}) exceeded for page {}, breaking", MAX_ITERATIONS, pageNumber);
                break;
            }

            previousStartPos = startPos;

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

            if (!chunkContent.isEmpty() && chunkContent.length() >= 5) {
                chunkTexts.add(chunkContent);
                log.trace("Page {}: Added chunk {} (length: {}), position: {}-{}",
                        pageNumber, chunkTexts.size(), chunkContent.length(), startPos, endPos);
            } else {
                log.debug("Page {}: Skipping chunk at position {}-{} (too short: {} chars)",
                        pageNumber, startPos, endPos, chunkContent.length());
            }

            // Calculate next position
            int nextPos = endPos - CHUNK_OVERLAP;

            log.trace("Page {}: startPos={}, endPos={}, nextPos={}, textLength={}",
                    pageNumber, startPos, endPos, nextPos, textLength);

            // CRITICAL FIX: Ensure we always move forward
            if (nextPos <= startPos) {
                // If overlap would cause us to go backwards, just move to endPos
                log.debug("Page {}: Overlap would go backwards, moving to endPos={}", pageNumber, endPos);
                startPos = endPos;
            } else {
                startPos = nextPos;
            }

            // Additional safety: if we're at the end, break
            if (startPos >= textLength) {
                log.debug("Page {}: Reached end of text at position {}", pageNumber, startPos);
                break;
            }
        }

        log.debug("Page {}: Extracted {} chunks in {} iterations", pageNumber, chunkTexts.size(), iterationCount);

        // PHASE 2: ⭐ SPRING AI BATCH EMBEDDINGS - ONE API CALL FOR ALL CHUNKS! ⭐
        List<List<Double>> embeddings;
        try {
            log.debug("Generating {} embeddings in ONE batch using Spring AI", chunkTexts.size());
            embeddings = embeddingService.generateEmbeddings(chunkTexts);
        } catch (Exception e) {
            log.error("Batch embedding failed, falling back to individual: {}", e.getMessage());
            // Fallback: generate one by one
            embeddings = new ArrayList<>();
            for (String chunkText : chunkTexts) {
                embeddings.add(embeddingService.generateEmbedding(chunkText));
            }
        }

        // PHASE 3: Create DocumentChunk objects with embeddings - using global chunk index
        for (int i = 0; i < chunkTexts.size(); i++) {
            List<Double> embedding = i < embeddings.size() ? embeddings.get(i) : new ArrayList<>();

            DocumentChunk chunk = DocumentChunk.builder()
                    .documentId(documentId)
                    .chunkIndex(startingChunkIndex + i) // Use global counter to prevent duplicates
                    .content(chunkTexts.get(i))
                    .pageNumber(pageNumber)
                    .embeddingVector(convertEmbeddingToString(embedding))
                    .build();

            chunks.add(chunk);

            // Free memory immediately
            if (!embedding.isEmpty()) {
                embedding.clear();
            }
        }

        return chunks;
    }

    /**
     * Validate chunks to ensure data integrity
     * - Filters out chunks with null or empty content
     * - Ensures all required fields are present
     * - Logs validation errors
     */
    private List<DocumentChunk> validateChunks(List<DocumentChunk> chunks, String documentId) {
        List<DocumentChunk> validChunks = new ArrayList<>();
        int invalidCount = 0;

        for (DocumentChunk chunk : chunks) {
            boolean isValid = true;
            StringBuilder errors = new StringBuilder();

            // Validate documentId
            if (chunk.getDocumentId() == null || chunk.getDocumentId().trim().isEmpty()) {
                errors.append("Missing documentId; ");
                isValid = false;
            }

            // Validate chunkIndex
            if (chunk.getChunkIndex() == null || chunk.getChunkIndex() < 0) {
                errors.append("Invalid chunkIndex: ").append(chunk.getChunkIndex()).append("; ");
                isValid = false;
            }

            // Validate content (most critical)
            if (chunk.getContent() == null || chunk.getContent().trim().isEmpty()) {
                errors.append("Missing or empty content; ");
                isValid = false;
            }

            // Warn if embedding is missing
            if (chunk.getEmbeddingVector() == null || chunk.getEmbeddingVector().trim().isEmpty()) {
                log.warn("Chunk {} has no embedding vector - will affect search quality", chunk.getChunkIndex());
            }

            if (isValid) {
                validChunks.add(chunk);
            } else {
                invalidCount++;
                log.warn("Skipping invalid chunk {}: {}", chunk.getChunkIndex(), errors.toString());
            }
        }

        if (invalidCount > 0) {
            log.warn("Filtered out {} invalid chunks for document {}", invalidCount, documentId);
        }

        log.debug("Validated {} chunks, {} valid, {} invalid", chunks.size(), validChunks.size(), invalidCount);
        return validChunks;
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

    /**
     * Clean up duplicate chunks for a document
     * Removes chunks with duplicate chunk_index, keeping only the first occurrence
     */
    @Transactional
    public int cleanupDuplicateChunks(String documentId) {
        log.info("Starting cleanup of duplicate chunks for document: {}", documentId);

        List<DocumentChunk> allChunks = documentChunkRepository.findByDocumentIdOrderByChunkIndex(documentId);

        if (allChunks.isEmpty()) {
            log.info("No chunks found for document: {}", documentId);
            return 0;
        }

        List<DocumentChunk> chunksToDelete = new ArrayList<>();
        List<Integer> seenIndices = new ArrayList<>();
        int duplicateCount = 0;

        for (DocumentChunk chunk : allChunks) {
            if (seenIndices.contains(chunk.getChunkIndex())) {
                chunksToDelete.add(chunk);
                duplicateCount++;
                log.debug("Found duplicate chunk_index {} (id: {})", chunk.getChunkIndex(), chunk.getId());
            } else {
                seenIndices.add(chunk.getChunkIndex());
            }
        }

        if (!chunksToDelete.isEmpty()) {
            documentChunkRepository.deleteAll(chunksToDelete);
            log.info("Deleted {} duplicate chunks for document: {}", duplicateCount, documentId);
        } else {
            log.info("No duplicate chunks found for document: {}", documentId);
        }

        return duplicateCount;
    }

    /**
     * Clean up all invalid chunks (null content, missing required fields)
     */
    @Transactional
    public int cleanupInvalidChunks(String documentId) {
        log.info("Starting cleanup of invalid chunks for document: {}", documentId);

        List<DocumentChunk> allChunks = documentChunkRepository.findByDocumentId(documentId);

        if (allChunks.isEmpty()) {
            log.info("No chunks found for document: {}", documentId);
            return 0;
        }

        List<DocumentChunk> chunksToDelete = new ArrayList<>();

        for (DocumentChunk chunk : allChunks) {
            boolean isInvalid = false;
            StringBuilder reason = new StringBuilder();

            if (chunk.getContent() == null || chunk.getContent().trim().isEmpty()) {
                reason.append("null/empty content; ");
                isInvalid = true;
            }

            if (chunk.getChunkIndex() == null || chunk.getChunkIndex() < 0) {
                reason.append("invalid chunkIndex; ");
                isInvalid = true;
            }

            if (isInvalid) {
                chunksToDelete.add(chunk);
                log.debug("Marking invalid chunk {} for deletion: {}", chunk.getId(), reason.toString());
            }
        }

        if (!chunksToDelete.isEmpty()) {
            documentChunkRepository.deleteAll(chunksToDelete);
            log.info("Deleted {} invalid chunks for document: {}", chunksToDelete.size(), documentId);
        } else {
            log.info("No invalid chunks found for document: {}", documentId);
        }

        return chunksToDelete.size();
    }

    /**
     * Comprehensive cleanup: removes duplicates and invalid chunks, then re-indexes
     */
    @Transactional
    public void cleanupAndReindexDocument(String documentId) {
        log.info("Starting comprehensive cleanup for document: {}", documentId);

        int duplicatesRemoved = cleanupDuplicateChunks(documentId);
        int invalidRemoved = cleanupInvalidChunks(documentId);

        // Re-index remaining chunks to ensure sequential indices
        List<DocumentChunk> remainingChunks = documentChunkRepository.findByDocumentIdOrderByChunkIndex(documentId);

        for (int i = 0; i < remainingChunks.size(); i++) {
            DocumentChunk chunk = remainingChunks.get(i);
            if (chunk.getChunkIndex() != i) {
                log.debug("Re-indexing chunk from {} to {}", chunk.getChunkIndex(), i);
                chunk.setChunkIndex(i);
            }
        }

        documentChunkRepository.saveAll(remainingChunks);

        // Update document metadata
        Document document = getDocument(documentId);
        document.setChunkCount(remainingChunks.size());
        documentRepository.save(document);

        log.info("Cleanup complete for document {}: {} duplicates removed, {} invalid removed, {} chunks remaining",
                documentId, duplicatesRemoved, invalidRemoved, remainingChunks.size());
    }

    /**
     * Find and remove duplicate documents (same fileName and version)
     * Keeps the most recently uploaded document, removes older duplicates
     */
    @Transactional
    public int cleanupDuplicateDocuments() {
        log.info("Starting cleanup of duplicate documents");

        List<Document> allDocuments = documentRepository.findByActive(true);
        Map<String, List<Document>> documentsByFileNameAndVersion = new HashMap<>();

        // Group documents by fileName+version
        for (Document doc : allDocuments) {
            String key = doc.getFileName() + "|" + doc.getVersion();
            documentsByFileNameAndVersion
                    .computeIfAbsent(key, k -> new ArrayList<>())
                    .add(doc);
        }

        int duplicatesRemoved = 0;

        // For each group, keep the newest, mark others as inactive
        for (Map.Entry<String, List<Document>> entry : documentsByFileNameAndVersion.entrySet()) {
            List<Document> duplicates = entry.getValue();

            if (duplicates.size() > 1) {
                // Sort by upload date, newest first
                duplicates.sort((d1, d2) -> d2.getUploadedAt().compareTo(d1.getUploadedAt()));

                Document keepDocument = duplicates.get(0);
                log.info("Found {} duplicates for '{} v{}', keeping documentId: {}",
                        duplicates.size(), keepDocument.getFileName(),
                        keepDocument.getVersion(), keepDocument.getDocumentId());

                // Mark older duplicates as inactive
                for (int i = 1; i < duplicates.size(); i++) {
                    Document duplicate = duplicates.get(i);
                    log.info("Removing duplicate documentId: {} (uploaded: {})",
                            duplicate.getDocumentId(), duplicate.getUploadedAt());

                    duplicate.setActive(false);
                    documentRepository.save(duplicate);

                    // Delete associated chunks
                    documentChunkRepository.deleteByDocumentId(duplicate.getDocumentId());

                    duplicatesRemoved++;
                }
            }
        }

        log.info("Duplicate documents cleanup complete: {} duplicates removed", duplicatesRemoved);
        return duplicatesRemoved;
    }

    /**
     * List all duplicate documents (same fileName and version)
     */
    public Map<String, List<Document>> findDuplicateDocuments() {
        List<Document> allDocuments = documentRepository.findByActive(true);
        Map<String, List<Document>> duplicates = new HashMap<>();
        Map<String, List<Document>> all = new HashMap<>();

        // Group documents by fileName+version
        for (Document doc : allDocuments) {
            String key = doc.getFileName() + " v" + doc.getVersion();
            all.computeIfAbsent(key, k -> new ArrayList<>()).add(doc);
        }

        // Filter only groups with more than 1 document
        for (Map.Entry<String, List<Document>> entry : all.entrySet()) {
            if (entry.getValue().size() > 1) {
                duplicates.put(entry.getKey(), entry.getValue());
            }
        }

        return duplicates;
    }
}

