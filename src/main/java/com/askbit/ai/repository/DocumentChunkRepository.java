package com.askbit.ai.repository;

import com.askbit.ai.model.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {
    List<DocumentChunk> findByDocumentId(String documentId);
    List<DocumentChunk> findByDocumentIdAndPageNumber(String documentId, Integer pageNumber);

    @Query("SELECT dc FROM DocumentChunk dc WHERE dc.documentId = :documentId ORDER BY dc.chunkIndex")
    List<DocumentChunk> findByDocumentIdOrderByChunkIndex(String documentId);

    void deleteByDocumentId(String documentId);

    /**
     * Find chunks only from specific document IDs (for version filtering)
     */
    List<DocumentChunk> findByDocumentIdIn(List<String> documentIds);
}

