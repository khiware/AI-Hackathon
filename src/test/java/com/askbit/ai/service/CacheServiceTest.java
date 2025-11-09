package com.askbit.ai.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private CacheService cacheService;

    @Test
    void incrementCacheHit_shouldCallRedisIncrement() {
        // Arrange
        String key = "testKey";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(key)).thenReturn(1L);

        // Act
        cacheService.incrementCacheHit(key);

        // Assert
        verify(valueOperations, times(1)).increment(key);
    }

    @Test
    void saveToCache_shouldSaveWithCorrectTTL() {
        // Arrange
        String key = "cacheKey";
        Object value = "testValue";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act
        cacheService.saveToCache(key, value);

        // Assert
        verify(valueOperations, times(1)).set(key, value, 1, TimeUnit.DAYS);
    }

    @Test
    void getFromCache_shouldReturnCachedValue() {
        // Arrange
        String key = "testKey";
        Object expectedValue = "cachedValue";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(expectedValue);

        // Act
        Object result = cacheService.getFromCache(key);

        // Assert
        assertThat(result).isEqualTo(expectedValue);
        verify(valueOperations, times(1)).get(key);
    }

    @Test
    void getFromCache_shouldReturnNullWhenNotFound() {
        // Arrange
        String key = "nonExistentKey";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(key)).thenReturn(null);

        // Act
        Object result = cacheService.getFromCache(key);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void getKeysByPattern_shouldReturnMatchingKeys() {
        // Arrange
        String pattern = "queries::*";
        Set<String> expectedKeys = new HashSet<>();
        expectedKeys.add("queries::key1");
        expectedKeys.add("queries::key2");

        when(redisTemplate.keys(pattern)).thenReturn(expectedKeys);

        // Act
        Set<String> result = cacheService.getKeysByPattern(pattern);

        // Assert
        assertThat(result).isEqualTo(expectedKeys);
        assertThat(result).hasSize(2);
    }

    @Test
    void getKeysByPattern_shouldReturnEmptySetWhenNoMatches() {
        // Arrange
        String pattern = "nonExistent::*";
        when(redisTemplate.keys(pattern)).thenReturn(new HashSet<>());

        // Act
        Set<String> result = cacheService.getKeysByPattern(pattern);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void evictCache_shouldDeleteQueriesKeys() {
        // Arrange
        Set<String> queriesKeys = new HashSet<>();
        queriesKeys.add("queries::key1");
        queriesKeys.add("queries::key2");

        Set<String> cacheHitsKeys = new HashSet<>();
        cacheHitsKeys.add("queriesCacheHits");

        when(redisTemplate.keys("queries::*")).thenReturn(queriesKeys);
        when(redisTemplate.keys("queriesCacheHits*")).thenReturn(cacheHitsKeys);
        when(redisTemplate.delete(queriesKeys)).thenReturn(2L);
        when(redisTemplate.delete(cacheHitsKeys)).thenReturn(1L);

        // Act
        cacheService.evictCache();

        // Assert
        verify(redisTemplate, times(1)).delete(queriesKeys);
        verify(redisTemplate, times(1)).delete(cacheHitsKeys);
    }

    @Test
    void evictCache_shouldHandleEmptyKeys() {
        // Arrange
        when(redisTemplate.keys("queries::*")).thenReturn(new HashSet<>());
        when(redisTemplate.keys("queriesCacheHits*")).thenReturn(new HashSet<>());

        // Act
        cacheService.evictCache();

        // Assert
        verify(redisTemplate, never()).delete(any(Set.class));
    }

    @Test
    void evictCache_shouldHandleNullKeys() {
        // Arrange
        when(redisTemplate.keys("queries::*")).thenReturn(null);
        when(redisTemplate.keys("queriesCacheHits*")).thenReturn(null);

        // Act
        cacheService.evictCache();

        // Assert
        verify(redisTemplate, never()).delete(any(Set.class));
    }

    @Test
    void saveToCache_shouldHandleComplexObjects() {
        // Arrange
        String key = "complexKey";
        Object complexValue = new TestObject("test", 123);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act
        cacheService.saveToCache(key, complexValue);

        // Assert
        verify(valueOperations, times(1)).set(key, complexValue, 1, TimeUnit.DAYS);
    }

    @Test
    void incrementCacheHit_shouldHandleMultipleIncrements() {
        // Arrange
        String key = "hitCounter";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.increment(key)).thenReturn(1L, 2L, 3L);

        // Act
        cacheService.incrementCacheHit(key);
        cacheService.incrementCacheHit(key);
        cacheService.incrementCacheHit(key);

        // Assert
        verify(valueOperations, times(3)).increment(key);
    }

    // Helper class for testing complex objects
    private static class TestObject {
        String name;
        int value;

        TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}

