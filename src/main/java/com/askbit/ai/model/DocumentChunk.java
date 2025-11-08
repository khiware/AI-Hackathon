package com.askbit.ai.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "document_chunks")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_fk", referencedColumnName = "id")
    private Document document;
}

