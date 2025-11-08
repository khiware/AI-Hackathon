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
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                    log.debug("Cleared cache: {}", cacheName);
                }
            });
        }
    }

    public CacheStatsResponse getCacheStats() {
        long cacheSize = 0L;
        long hits = 0L;
        long misses = 0L;
        double hitRate = 0.0;

        if (cacheManager != null) {
            Cache cache = cacheManager.getCache("queries");
            if (cache != null && cache.getNativeCache() instanceof com.github.benmanes.caffeine.cache.Cache) {
                com.github.benmanes.caffeine.cache.Cache<?, ?> caffeineCache =
                        (com.github.benmanes.caffeine.cache.Cache<?, ?>) cache.getNativeCache();

                // Get cache size
                cacheSize = caffeineCache.estimatedSize();

                // Get cache statistics
                com.github.benmanes.caffeine.cache.stats.CacheStats stats = caffeineCache.stats();
                hits = stats.hitCount();
                misses = stats.missCount();
                long totalRequests = hits + misses;

                if (totalRequests > 0) {
                    hitRate = (double) hits / totalRequests;
                }

                log.debug("Cache stats - Size: {}, Hits: {}, Misses: {}, Hit Rate: {}",
                        cacheSize, hits, misses, hitRate);
            }
        }

        return CacheStatsResponse.builder()
                .size(cacheSize)
                .hitRate(hitRate)
                .totalHits(hits)
                .totalMisses(misses)
                .build();
    }
}

