package com.askbit.ai.controller;

import com.askbit.ai.dto.CacheStatsResponse;
import com.askbit.ai.dto.MetricsResponse;
import com.askbit.ai.dto.TopQuestionResponse;
import com.askbit.ai.service.AdminService;
import com.askbit.ai.service.MetricsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MetricsService metricsService;

    @MockBean
    private AdminService adminService;

    @Test
    void getMetrics_shouldReturnMetrics() throws Exception {
        // Arrange
        MetricsResponse metrics = MetricsResponse.builder()
                .totalQueries(1000L)
                .averageResponseTimeMs(1500.0)
                .p95LatencyMs(2500.0)
                .cacheHitRate(0.75)
                .totalDocuments(50L)
                .totalChunks(500L)
                .averageConfidence(0.85)
                .piiRedactionCount(10L)
                .clarificationCount(5L)
                .mostUsedModel("gpt-4")
                .estimatedCost(2.0)
                .build();

        when(metricsService.getMetrics()).thenReturn(metrics);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/metrics")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQueries").value(1000))
                .andExpect(jsonPath("$.averageResponseTimeMs").value(1500.0))
                .andExpect(jsonPath("$.cacheHitRate").value(0.75))
                .andExpect(jsonPath("$.mostUsedModel").value("gpt-4"));

        verify(metricsService, times(1)).getMetrics();
    }

    @Test
    void getTopQuestions_shouldReturnTopQuestions() throws Exception {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        List<TopQuestionResponse> topQuestions = Arrays.asList(
                TopQuestionResponse.builder()
                        .question("What is the WFH policy?")
                        .count(50L)
                        .avgConfidence(0.92)
                        .lastAsked(now)
                        .build(),
                TopQuestionResponse.builder()
                        .question("How much PTO do I get?")
                        .count(35L)
                        .avgConfidence(0.88)
                        .lastAsked(now.minusDays(1))
                        .build()
        );

        when(adminService.getTopQuestions(10)).thenReturn(topQuestions);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/top-questions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].question").value("What is the WFH policy?"))
                .andExpect(jsonPath("$[0].count").value(50))
                .andExpect(jsonPath("$[1].count").value(35));

        verify(adminService, times(1)).getTopQuestions(10);
    }

    @Test
    void getTopQuestions_shouldHandleCustomLimit() throws Exception {
        // Arrange
        when(adminService.getTopQuestions(5)).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/top-questions")
                        .param("limit", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        verify(adminService, times(1)).getTopQuestions(5);
    }

    @Test
    void invalidateCache_shouldReturnSuccess() throws Exception {
        // Arrange
        doNothing().when(adminService).invalidateAllCaches();

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/cache/invalidate")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.message").value("All caches have been cleared"));

        verify(adminService, times(1)).invalidateAllCaches();
    }

    @Test
    void getCacheStats_shouldReturnStats() throws Exception {
        // Arrange
        CacheStatsResponse stats = CacheStatsResponse.builder()
                .size(100L)
                .totalHits(75L)
                .totalMisses(25L)
                .hitRate(0.75)
                .build();

        when(adminService.getCacheStats()).thenReturn(stats);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/cache/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100))
                .andExpect(jsonPath("$.totalHits").value(75))
                .andExpect(jsonPath("$.totalMisses").value(25))
                .andExpect(jsonPath("$.hitRate").value(0.75));

        verify(adminService, times(1)).getCacheStats();
    }

    @Test
    void getMetrics_shouldHandleZeroValues() throws Exception {
        // Arrange
        MetricsResponse metrics = MetricsResponse.builder()
                .totalQueries(0L)
                .averageResponseTimeMs(0.0)
                .cacheHitRate(0.0)
                .totalDocuments(0L)
                .totalChunks(0L)
                .mostUsedModel("N/A")
                .build();

        when(metricsService.getMetrics()).thenReturn(metrics);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalQueries").value(0))
                .andExpect(jsonPath("$.mostUsedModel").value("N/A"));
    }

    @Test
    void getTopQuestions_shouldHandleEmptyList() throws Exception {
        // Arrange
        when(adminService.getTopQuestions(anyInt())).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/top-questions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}

