package com.askbit.ai.repository;

import com.askbit.ai.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    Optional<Document> findByDocumentId(String documentId);
    List<Document> findByActive(Boolean active);
    Long countByActive(Boolean active);
    List<Document> findByFileNameContainingIgnoreCase(String fileName);
    Optional<Document> findByDocumentIdAndVersion(String documentId, String version);
    List<Document> findByIndexed(Boolean indexed);
    Optional<Document> findByFileNameAndVersion(String fileName, String version);
    Optional<Document> findByFileNameAndVersionAndActive(String fileName, String version, Boolean active);

    /**
     * Find all versions of a document by filename
     */
    List<Document> findByFileNameAndActiveOrderByUploadedAtDesc(String fileName, Boolean active);

    /**
     * Find latest version of each unique document
     */
    @Query("SELECT d FROM Document d WHERE d.active = true AND d.uploadedAt = " +
           "(SELECT MAX(d2.uploadedAt) FROM Document d2 WHERE d2.fileName = d.fileName AND d2.active = true)")
    List<Document> findLatestVersions();

    /**
     * Find documents uploaded before a specific year
     */
    @Query("SELECT d FROM Document d WHERE d.active = true AND YEAR(d.uploadedAt) <= :year " +
           "AND d.uploadedAt = (SELECT MAX(d2.uploadedAt) FROM Document d2 " +
           "WHERE d2.fileName = d.fileName AND d2.active = true AND YEAR(d2.uploadedAt) <= :year)")
    List<Document> findLatestVersionsBeforeYear(@Param("year") int year);
}