package com.askbit.ai.service;

import com.askbit.ai.dto.CacheStatsResponse;
import com.askbit.ai.dto.TopQuestionResponse;
import com.askbit.ai.repository.QueryHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final QueryHistoryRepository queryHistoryRepository;
    private final CacheManager cacheManager;

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
        log.info("Invalidating all caches");

        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(cacheName -> {
                Cache cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    log.info("Cleared cache: {}", cacheName);
                }
            });
        }
    }

    public CacheStatsResponse getCacheStats() {
        Long totalHits = queryHistoryRepository.countCacheHits();
        Long totalMisses = queryHistoryRepository.countCacheMisses();

        long hits = totalHits != null ? totalHits : 0L;
        long misses = totalMisses != null ? totalMisses : 0L;
        long totalRequests = hits + misses;
        double hitRate = totalRequests > 0 ? hits * 100.0 / totalRequests : 0.0;

        // Get cache size from cache manager
        long cacheSize = 0L;
        if (cacheManager != null) {
            Cache cache = cacheManager.getCache("queryCache");
            if (cache != null && cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
                cacheSize = ((com.github.benmanes.caffeine.cache.Cache<?, ?>) cache.getNativeCache()).estimatedSize();
            }
        }

        return CacheStatsResponse.builder()
                .size(cacheSize)
                .hitRate(hitRate / 100.0) // Convert to decimal
                .totalHits(hits)
                .totalMisses(misses)
                .build();
    }
}

