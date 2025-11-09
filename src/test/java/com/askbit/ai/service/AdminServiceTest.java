package com.askbit.ai.service;

import com.askbit.ai.dto.CacheStatsResponse;
import com.askbit.ai.dto.TopQuestionResponse;
import com.askbit.ai.repository.QueryHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private QueryHistoryRepository queryHistoryRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test
        reset(queryHistoryRepository, cacheService);
    }

    @Test
    void getTopQuestions_shouldReturnTopQuestionsSuccessfully() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{"What is WFH policy?", 100L, 0.95, now});
        mockResults.add(new Object[]{"How to apply leave?", 50L, 0.88, now.minusDays(1)});
        mockResults.add(new Object[]{"Salary increment policy?", 30L, 0.92, now.minusDays(2)});

        when(queryHistoryRepository.findTopQuestions()).thenReturn(mockResults);

        // Act
        List<TopQuestionResponse> result = adminService.getTopQuestions(2);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getQuestion()).isEqualTo("What is WFH policy?");
        assertThat(result.get(0).getCount()).isEqualTo(100L);
        assertThat(result.get(0).getAvgConfidence()).isEqualTo(0.95);
        assertThat(result.get(1).getQuestion()).isEqualTo("How to apply leave?");
        verify(queryHistoryRepository, times(1)).findTopQuestions();
    }

    @Test
    void getTopQuestions_shouldHandleNullConfidence() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{"Test question", 10L, null, now});

        when(queryHistoryRepository.findTopQuestions()).thenReturn(mockResults);

        // Act
        List<TopQuestionResponse> result = adminService.getTopQuestions(1);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAvgConfidence()).isNull();
    }

    @Test
    void getTopQuestions_shouldHandleEmptyResults() {
        // Arrange
        when(queryHistoryRepository.findTopQuestions()).thenReturn(Collections.emptyList());

        // Act
        List<TopQuestionResponse> result = adminService.getTopQuestions(10);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getTopQuestions_shouldRespectLimit() {
        // Arrange
        List<Object[]> mockResults = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            mockResults.add(new Object[]{"Question " + i, (long) i, 0.9, LocalDateTime.now()});
        }

        when(queryHistoryRepository.findTopQuestions()).thenReturn(mockResults);

        // Act
        List<TopQuestionResponse> result = adminService.getTopQuestions(5);

        // Assert
        assertThat(result).hasSize(5);
    }

    @Test
    void invalidateAllCaches_shouldCallCacheServiceEvict() {
        // Arrange
        doNothing().when(cacheService).evictCache();

        // Act
        adminService.invalidateAllCaches();

        // Assert
        verify(cacheService, times(1)).evictCache();
    }

    @Test
    void invalidateAllCaches_shouldHandleExceptionGracefully() {
        // Arrange
        doThrow(new RuntimeException("Redis error")).when(cacheService).evictCache();

        // Act & Assert - should not throw exception
        assertDoesNotThrow(() -> adminService.invalidateAllCaches());
        verify(cacheService, times(1)).evictCache();
    }

    @Test
    void getCacheStats_shouldReturnCorrectStats() {
        // Arrange
        Set<String> mockKeys = new HashSet<>();
        mockKeys.add("queries::key1");
        mockKeys.add("queries::key2");
        mockKeys.add("queries::key3");

        when(cacheService.getFromCache("queriesCacheHits")).thenReturn(10);
        when(cacheService.getKeysByPattern("queries::*")).thenReturn(mockKeys);

        // Act
        CacheStatsResponse result = adminService.getCacheStats();

        // Assert
        assertThat(result.getSize()).isEqualTo(3L);
        assertThat(result.getTotalHits()).isEqualTo(10L);
        assertThat(result.getTotalMisses()).isEqualTo(3L);
        assertThat(result.getHitRate()).isGreaterThan(0.0);
    }

    @Test
    void getCacheStats_shouldHandleNullCacheHits() {
        // Arrange
        when(cacheService.getFromCache("queriesCacheHits")).thenReturn(null);
        when(cacheService.getKeysByPattern("queries::*")).thenReturn(Collections.emptySet());

        // Act
        CacheStatsResponse result = adminService.getCacheStats();

        // Assert
        assertThat(result.getSize()).isEqualTo(0L);
        assertThat(result.getTotalHits()).isEqualTo(0L);
        assertThat(result.getHitRate()).isEqualTo(0.0);
    }

    @Test
    void getCacheStats_shouldHandleNullKeys() {
        // Arrange
        when(cacheService.getFromCache("queriesCacheHits")).thenReturn(5);
        when(cacheService.getKeysByPattern("queries::*")).thenReturn(null);

        // Act
        CacheStatsResponse result = adminService.getCacheStats();

        // Assert
        assertThat(result.getSize()).isEqualTo(0L);
    }

    @Test
    void getCacheStats_shouldHandleException() {
        // Arrange
        when(cacheService.getFromCache(anyString())).thenThrow(new RuntimeException("Redis error"));

        // Act
        CacheStatsResponse result = adminService.getCacheStats();

        // Assert
        assertThat(result.getSize()).isEqualTo(0L);
        assertThat(result.getTotalHits()).isEqualTo(0L);
        assertThat(result.getHitRate()).isEqualTo(0.0);
    }

    @Test
    void getCacheStats_shouldCalculateHitRateCorrectly() {
        // Arrange
        Set<String> mockKeys = new HashSet<>();
        mockKeys.add("queries::key1");

        when(cacheService.getFromCache("queriesCacheHits")).thenReturn(9);
        when(cacheService.getKeysByPattern("queries::*")).thenReturn(mockKeys);

        // Act
        CacheStatsResponse result = adminService.getCacheStats();

        // Assert
        // hitRate = hits / (hits + cacheSize) = 9 / (9 + 1) = 0.9
        assertThat(result.getHitRate()).isEqualTo(0.9);
    }
}

