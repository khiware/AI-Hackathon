package com.askbit.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheStatsResponse {
    private Long size;
    private Double hitRate;
    private Long totalHits;
    private Long totalMisses;
}

