package com.askbit.ai.repository;

import com.askbit.ai.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByDocumentId(String documentId);
    List<Document> findByActive(Boolean active);
    List<Document> findByFileNameContainingIgnoreCase(String fileName);
    Optional<Document> findByDocumentIdAndVersion(String documentId, String version);
    List<Document> findByIndexed(Boolean indexed);
    Optional<Document> findByFileNameAndVersion(String fileName, String version);
    Optional<Document> findByFileNameAndVersionAndActive(String fileName, String version, Boolean active);
}