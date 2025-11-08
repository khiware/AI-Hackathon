package com.askbit.ai.service;

import com.askbit.ai.dto.CacheStatsResponse;
import com.askbit.ai.dto.TopQuestionResponse;
import com.askbit.ai.repository.QueryHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final QueryHistoryRepository queryHistoryRepository;
    private final CacheService cacheService;

    public List<TopQuestionResponse> getTopQuestions(int limit) {
        List<Object[]> results = queryHistoryRepository.findTopQuestions();
        List<TopQuestionResponse> topQuestions = new ArrayList<>();

        int count = 0;
        for (Object[] row : results) {
            if (count >= limit) break;

            // Parse the results based on query return types
            String question = (String) row[0];
            Long questionCount = ((Number) row[1]).longValue();
            Double avgConfidence = row[2] != null ? ((Number) row[2]).doubleValue() : null;
            LocalDateTime lastAsked = row[3] instanceof LocalDateTime ? (LocalDateTime) row[3] : null;

            TopQuestionResponse topQuestion = TopQuestionResponse.builder()
                    .question(question)
                    .count(questionCount)
                    .avgConfidence(avgConfidence)
                    .lastAsked(lastAsked)
                    .build();

            topQuestions.add(topQuestion);
            count++;
        }

        return topQuestions;
    }

    public void invalidateAllCaches() {
        try {
            cacheService.evictCache();
        } catch (Exception e) {
            log.error("Error clearing Redis cache", e);
        }
    }

    public CacheStatsResponse getCacheStats() {
        long cacheSize = 0L;
        long hits = 0L;
        long misses = 0L;
        double hitRate = 0.0;

        try {
            // Get cacheHits from Redis
            hits = cacheService.getFromCache("queriesCacheHits") != null
                    ? Long.parseLong((String) cacheService.getFromCache("queriesCacheHits"))
                    : 0;

            // Get actual Redis cache size by counting keys matching the pattern
            Set<String> keys = cacheService.getKeysByPattern("queries::*");
            cacheSize = (keys != null) ? keys.size() : 0L;
            // Because misses would be stored in the cache
            misses = cacheSize;

            log.debug("Redis cache contains {} entries", cacheSize);
        } catch (Exception e) {
            log.error("Error getting Redis cache size", e);
        }

        // Calculate hit rate based on total requests
        if (cacheSize > 0) {
            hitRate = (double) hits / cacheSize;
        }

        log.debug("Cache stats - Size: {}, Hits: {}, Misses: {}, Hit Rate: {}",
                cacheSize, hits, misses, hitRate);

        return CacheStatsResponse.builder()
                .size(cacheSize)
                .hitRate(hitRate)
                .totalHits(hits)
                .totalMisses(misses)
                .build();
    }
}

