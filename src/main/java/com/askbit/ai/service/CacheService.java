package com.askbit.ai.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void saveToCache(String key, Object value) {
        redisTemplate.opsForValue().set(key, value, 1, TimeUnit.DAYS);
    }

    public Object getFromCache(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Set<String> getKeysByPattern(String keyPattern) {
        return redisTemplate.keys(keyPattern);
    }

    @PostConstruct
    public void evictCache() {
        deleteKeyByPattern("queries::*");
        deleteKeyByPattern("queriesCacheHits*");
        log.info("Evicted all relevant caches from Redis");
    }

    // Clear all keys matching the queries cache pattern
    private void deleteKeyByPattern(String keyPattern) {
        Set<String> keys = getKeysByPattern(keyPattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Cleared {} cached {} from Redis", keys.size(), keyPattern);
        } else {
            log.info("No cached {} found in Redis", keyPattern);
        }
    }
}

