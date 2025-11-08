package com.askbit.ai.service;

import com.askbit.ai.dto.MetricsResponse;
import com.askbit.ai.repository.DocumentChunkRepository;
import com.askbit.ai.repository.DocumentRepository;
import com.askbit.ai.repository.QueryHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {

    private final QueryHistoryRepository queryHistoryRepository;
    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;

    public MetricsResponse getMetrics() {
        Long totalQueries = queryHistoryRepository.count();
        Double avgResponseTime = queryHistoryRepository.findAverageResponseTime();
        Double p95Latency = calculateP95Latency();
        Double cacheHitRate = queryHistoryRepository.findCacheHitRate();
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
                .p95LatencyMs(p95Latency != null ? p95Latency : 0.0)
                .cacheHitRate(cacheHitRate != null ? cacheHitRate : 0.0)
                .totalDocuments(totalDocuments)
                .totalChunks(totalChunks)
                .averageConfidence(avgConfidence != null ? avgConfidence : 0.0)
                .piiRedactionCount(piiRedactionCount != null ? piiRedactionCount : 0L)
                .clarificationCount(clarificationCount != null ? clarificationCount : 0L)
                .mostUsedModel(mostUsedModel)
                .estimatedCost(estimatedCost)
                .build();
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

