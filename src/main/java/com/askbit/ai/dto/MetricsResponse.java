package com.askbit.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricsResponse {
    private Long totalQueries;
    private Double averageResponseTimeMs;
    private Double cacheHitRate;
    private Long totalDocuments;
    private Long totalChunks;
    private Double averageConfidence;
    private Long piiRedactionCount;
    private Long clarificationCount;
    private String mostUsedModel;
}


