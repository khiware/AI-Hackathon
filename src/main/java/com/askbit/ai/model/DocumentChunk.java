package com.askbit.ai.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_chunks",
       indexes = {
           @Index(name = "idx_document_chunk", columnList = "documentId,chunkIndex", unique = true),
           @Index(name = "idx_document_page", columnList = "documentId,pageNumber")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String documentId;

    @Column(nullable = false)
    private Integer chunkIndex;

    @Column(nullable = false, length = 10000)
    private String content;

    private Integer pageNumber;

    private Integer paragraphNumber;

    private Integer startLine;

    private Integer endLine;

    @Column(length = 500)
    private String section;

    // Embedding vector stored as comma-separated string for H2 compatibility
    @Column(length = 50000)
    private String embeddingVector;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_fk", referencedColumnName = "id")
    private Document document;
}

