package com.askbit.ai.service;

import com.askbit.ai.dto.MetricsResponse;
import com.askbit.ai.model.Document;
import com.askbit.ai.repository.DocumentRepository;
import com.askbit.ai.repository.QueryHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {

    @Mock
    private QueryHistoryRepository queryHistoryRepository;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private MetricsService metricsService;

    @BeforeEach
    void setUp() {
        // Create mock documents with chunk counts
        Document doc1 = new Document();
        doc1.setChunkCount(200);

        Document doc2 = new Document();
        doc2.setChunkCount(300);

        List<Document> activeDocuments = Arrays.asList(doc1, doc2);

        // Default mocks for common operations
        lenient().when(queryHistoryRepository.countModelHitQueries()).thenReturn(100L);
        lenient().when(queryHistoryRepository.findAverageResponseTime()).thenReturn(1500.0);
        lenient().when(queryHistoryRepository.findAverageConfidence()).thenReturn(0.85);
        lenient().when(queryHistoryRepository.countPiiRedactions()).thenReturn(10L);
        lenient().when(queryHistoryRepository.countClarifications()).thenReturn(5L);
        lenient().when(documentRepository.countByActive(true)).thenReturn(2L);
        lenient().when(documentRepository.findByActive(true)).thenReturn(activeDocuments);
    }

    @Test
    void getMetrics_shouldReturnCompleteMetrics() {
        // Arrange
        List<Long> responseTimes = Arrays.asList(100L, 200L, 300L, 400L, 500L,
                600L, 700L, 800L, 900L, 1000L,
                1100L, 1200L, 1300L, 1400L, 1500L,
                1600L, 1700L, 1800L, 1900L, 2000L);
        when(queryHistoryRepository.findAllResponseTimesSorted()).thenReturn(responseTimes);

        Object[] modelStat = new Object[]{"gpt-4", 75L};
        when(queryHistoryRepository.findModelUsageStats()).thenReturn(Collections.singletonList(modelStat));

        when(cacheService.getFromCache("queriesCacheHits")).thenReturn(30);
        when(cacheService.getKeysByPattern("queries::*")).thenReturn(Set.of("key1", "key2", "key3"));

        // Act
        MetricsResponse result = metricsService.getMetrics();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTotalQueries()).isEqualTo(100L);
        assertThat(result.getAverageResponseTimeMs()).isEqualTo(1500.0);
        assertThat(result.getTotalDocuments()).isEqualTo(2L); // 2 active documents
        assertThat(result.getTotalChunks()).isEqualTo(500L); // 200 + 300 chunks
        assertThat(result.getAverageConfidence()).isEqualTo(0.85);
        assertThat(result.getPiiRedactionCount()).isEqualTo(10L);
        assertThat(result.getClarificationCount()).isEqualTo(5L);
        assertThat(result.getMostUsedModel()).isEqualTo("gpt-4");
        assertThat(result.getEstimatedCost()).isEqualTo(0.2);
    }

    @Test
    void getMetrics_shouldHandleNullValues() {
        // Arrange
        when(queryHistoryRepository.countModelHitQueries()).thenReturn(null);
        when(queryHistoryRepository.findAverageResponseTime()).thenReturn(null);
        when(queryHistoryRepository.findAverageConfidence()).thenReturn(null);
        when(queryHistoryRepository.countPiiRedactions()).thenReturn(null);
        when(queryHistoryRepository.countClarifications()).thenReturn(null);
        when(queryHistoryRepository.findAllResponseTimesSorted()).thenReturn(null);
        when(queryHistoryRepository.findModelUsageStats()).thenReturn(null);
        when(documentRepository.countByActive(true)).thenReturn(0L);
        when(documentRepository.findByActive(true)).thenReturn(Collections.emptyList());

        // Act
        MetricsResponse result = metricsService.getMetrics();

        // Assert
        assertThat(result.getTotalQueries()).isEqualTo(0L);
        assertThat(result.getAverageResponseTimeMs()).isEqualTo(0.0);
        assertThat(result.getAverageConfidence()).isEqualTo(0.0);
        assertThat(result.getPiiRedactionCount()).isEqualTo(0L);
        assertThat(result.getClarificationCount()).isEqualTo(0L);
        assertThat(result.getMostUsedModel()).isEqualTo("N/A");
        assertThat(result.getP95LatencyMs()).isEqualTo(0.0);
    }

    @Test
    void calculateP95Latency_shouldReturnCorrectValue() {
        // Arrange
        List<Long> times = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            times.add((long) (i * 10));
        }
        when(queryHistoryRepository.findAllResponseTimesSorted()).thenReturn(times);
        when(queryHistoryRepository.findModelUsageStats()).thenReturn(Collections.emptyList());

        // Act
        MetricsResponse result = metricsService.getMetrics();

        // Assert
        assertThat(result.getP95LatencyMs()).isGreaterThan(0);
    }

    @Test
    void calculateCacheHitRate_shouldCalculateCorrectly() {
        // Arrange
        when(cacheService.getFromCache("queriesCacheHits")).thenReturn(50);
        when(cacheService.getKeysByPattern("queries::*")).thenReturn(Set.of("k1", "k2", "k3", "k4", "k5"));
        when(queryHistoryRepository.findAllResponseTimesSorted()).thenReturn(Collections.emptyList());
        when(queryHistoryRepository.findModelUsageStats()).thenReturn(Collections.emptyList());

        // Act
        MetricsResponse result = metricsService.getMetrics();

        // Assert
        assertThat(result.getCacheHitRate()).isGreaterThan(0.0);
    }

    @Test
    void calculateCacheHitRate_shouldHandleException() {
        // Arrange
        when(cacheService.getFromCache(anyString())).thenThrow(new RuntimeException("Redis error"));
        when(queryHistoryRepository.findAllResponseTimesSorted()).thenReturn(Collections.emptyList());
        when(queryHistoryRepository.findModelUsageStats()).thenReturn(Collections.emptyList());

        // Act
        MetricsResponse result = metricsService.getMetrics();

        // Assert
        assertThat(result.getCacheHitRate()).isEqualTo(0.0);
    }

    @Test
    void getMostUsedModel_shouldReturnTopModel() {
        // Arrange
        List<Object[]> stats = Arrays.asList(
                new Object[]{"gpt-4", 100L},
                new Object[]{"gpt-3.5", 50L}
        );
        when(queryHistoryRepository.findModelUsageStats()).thenReturn(stats);
        when(queryHistoryRepository.findAllResponseTimesSorted()).thenReturn(Collections.emptyList());

        // Act
        MetricsResponse result = metricsService.getMetrics();

        // Assert
        assertThat(result.getMostUsedModel()).isEqualTo("gpt-4");
    }

    @Test
    void getMostUsedModel_shouldHandleNullModelName() {
        // Arrange
        Object[] stat = new Object[]{null, 100L};
        when(queryHistoryRepository.findModelUsageStats()).thenReturn(Collections.singletonList(stat));
        when(queryHistoryRepository.findAllResponseTimesSorted()).thenReturn(Collections.emptyList());

        // Act
        MetricsResponse result = metricsService.getMetrics();

        // Assert
        assertThat(result.getMostUsedModel()).isEqualTo("N/A");
    }

    @Test
    void getMetrics_shouldCalculateCostCorrectly() {
        // Arrange
        when(queryHistoryRepository.countModelHitQueries()).thenReturn(1000L);
        when(queryHistoryRepository.findAllResponseTimesSorted()).thenReturn(Collections.emptyList());
        when(queryHistoryRepository.findModelUsageStats()).thenReturn(Collections.emptyList());

        // Act
        MetricsResponse result = metricsService.getMetrics();

        // Assert
        assertThat(result.getEstimatedCost()).isEqualTo(2.0);
    }
}

