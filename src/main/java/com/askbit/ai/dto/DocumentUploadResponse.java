package com.askbit.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadResponse {
    private String documentId;
    private String fileName;
    private String version;
    private Boolean success;
    private String message;
    private Integer pagesProcessed;
    private Integer chunksCreated;
}

