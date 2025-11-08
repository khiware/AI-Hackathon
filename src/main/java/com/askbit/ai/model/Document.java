package com.askbit.ai.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false, unique = true)
    private String documentId;

    @Column(nullable = false)
    private String version;

    @Column(nullable = false)
    private String fileType;

    @Column(nullable = false)
    private String filePath;

    @Column(length = 5000)
    private String description;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Column(nullable = false)
    private LocalDateTime lastModifiedAt;

    @Column(nullable = false)
    private Boolean active;

    private Integer pageCount;

    private Long fileSize;

    @Column(nullable = false)
    private Boolean indexed;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        lastModifiedAt = LocalDateTime.now();
        if (active == null) {
            active = true;
        }
        if (indexed == null) {
            indexed = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastModifiedAt = LocalDateTime.now();
    }
}

