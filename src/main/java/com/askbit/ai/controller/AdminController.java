package com.askbit.ai.controller;

import com.askbit.ai.dto.MetricsResponse;
import com.askbit.ai.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdminController {

    private final MetricsService metricsService;

    @GetMapping("/metrics")
    public ResponseEntity<MetricsResponse> getMetrics() {
        log.info("Fetching system metrics");
        MetricsResponse metrics = metricsService.getMetrics();
        return ResponseEntity.ok(metrics);
    }
}


