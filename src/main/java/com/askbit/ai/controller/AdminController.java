package com.askbit.ai.controller;

import com.askbit.ai.dto.CacheStatsResponse;
import com.askbit.ai.dto.MetricsResponse;
import com.askbit.ai.dto.TopQuestionResponse;
import com.askbit.ai.service.AdminService;
import com.askbit.ai.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdminController {

    private final MetricsService metricsService;
    private final AdminService adminService;

    @GetMapping("/metrics")
    public ResponseEntity<MetricsResponse> getMetrics() {
        log.info("Fetching system metrics");
        MetricsResponse metrics = metricsService.getMetrics();
        return ResponseEntity.ok(metrics);
    }

    @GetMapping("/top-questions")
    public ResponseEntity<List<TopQuestionResponse>> getTopQuestions(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Fetching top {} questions", limit);
        List<TopQuestionResponse> topQuestions = adminService.getTopQuestions(limit);
        return ResponseEntity.ok(topQuestions);
    }

    @PostMapping("/cache/invalidate")
    public ResponseEntity<Map<String, String>> invalidateCache() {
        log.info("Invalidating all caches");
        adminService.invalidateAllCaches();
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "All caches have been cleared"
        ));
    }

    @GetMapping("/cache/stats")
    public ResponseEntity<CacheStatsResponse> getCacheStats() {
        log.info("Fetching cache statistics");
        CacheStatsResponse stats = adminService.getCacheStats();
        return ResponseEntity.ok(stats);
    }
}


