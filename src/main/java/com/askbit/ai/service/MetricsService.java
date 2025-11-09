package com.askbit.ai.service;

import com.askbit.ai.dto.MetricsResponse;
import com.askbit.ai.repository.DocumentChunkRepository;
import com.askbit.ai.repository.DocumentRepository;
import com.askbit.ai.repository.QueryHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {

    private final QueryHistoryRepository queryHistoryRepository;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final CacheService cacheService;

    public MetricsResponse getMetrics() {
        // Count only model-hit queries (exclude cached queries)
        Long totalQueriesLong = queryHistoryRepository.countModelHitQueries();
        long totalQueries = totalQueriesLong != null ? totalQueriesLong : 0L;

        Double avgResponseTime = queryHistoryRepository.findAverageResponseTime();
        Double p95Latency = calculateP95Latency();
        Double cacheHitRate = calculateCacheHitRate();
        Long totalDocuments = documentRepository.count();
        Long totalChunks = documentChunkRepository.count();
        Double avgConfidence = queryHistoryRepository.findAverageConfidence();
        Long piiRedactionCount = queryHistoryRepository.countPiiRedactions();
        Long clarificationCount = queryHistoryRepository.countClarifications();

        String mostUsedModel = getMostUsedModel();

        // Estimate cost: $0.002 per query (approximate GPT-3.5 cost)
        Double estimatedCost = totalQueries * 0.002;

        return MetricsResponse.builder()
                .totalQueries(totalQueries)
                .averageResponseTimeMs(avgResponseTime != null ? avgResponseTime : 0.0)
                .p95LatencyMs(p95Latency)
                .cacheHitRate(cacheHitRate)
                .totalDocuments(totalDocuments)
                .totalChunks(totalChunks)
                .averageConfidence(avgConfidence != null ? avgConfidence : 0.0)
                .piiRedactionCount(piiRedactionCount != null ? piiRedactionCount : 0L)
                .clarificationCount(clarificationCount != null ? clarificationCount : 0L)
                .mostUsedModel(mostUsedModel)
                .estimatedCost(estimatedCost)
                .build();
    }

    /**
     * Get cache hit rate from QueryHistory.
     * Redis doesn't provide built-in stats like Caffeine, so we track via database.
     * Returns value between 0.0 and 1.0 representing the hit rate.
     */
    private Double calculateCacheHitRate() {
        try {
            // Get cacheHits from Redis
            long hits = cacheService.getFromCache("queriesCacheHits") != null
                    ? Long.parseLong((String) cacheService.getFromCache("queriesCacheHits"))
                    : 0L;

            Set<String> keys = cacheService.getKeysByPattern("queries::*");
            long cacheSize = (keys != null) ? keys.size() : 0L;

            log.debug("Redis cache contains {} entries", cacheSize);

            if (cacheSize > 0) {
                return (double) hits / (hits + cacheSize);
            }
        } catch (Exception e) {
            log.error("Error fetching cache hit rate from redisTemplate queries cache", e);
        }

        return 0.0;
    }

    private Double calculateP95Latency() {
        List<Long> responseTimes = queryHistoryRepository.findAllResponseTimesSorted();

        if (responseTimes == null || responseTimes.isEmpty()) {
            return 0.0;
        }

        // Calculate 95th percentile index
        int p95Index = (int) Math.ceil(responseTimes.size() * 0.05) - 1;
        if (p95Index < 0) p95Index = 0;

        return responseTimes.get(p95Index).doubleValue();
    }

    private String getMostUsedModel() {
        List<Object[]> modelStats = queryHistoryRepository.findModelUsageStats();

        if (modelStats != null && !modelStats.isEmpty()) {
            Object[] topModel = modelStats.get(0);
            return topModel[0] != null ? topModel[0].toString() : "N/A";
        }

        return "N/A";
    }
}

