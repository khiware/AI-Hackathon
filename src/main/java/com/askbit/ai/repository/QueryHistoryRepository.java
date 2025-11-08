package com.askbit.ai.repository;

import com.askbit.ai.model.QueryHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface QueryHistoryRepository extends JpaRepository<QueryHistory, Long> {
    Optional<QueryHistory> findByNormalizedQuestion(String normalizedQuestion);

    List<QueryHistory> findByQueryTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT AVG(qh.responseTimeMs) FROM QueryHistory qh")
    Double findAverageResponseTime();

    @Query("SELECT COUNT(qh) * 100.0 / (SELECT COUNT(qh2) FROM QueryHistory qh2) FROM QueryHistory qh WHERE qh.fromCache = true")
    Double findCacheHitRate();

    @Query("SELECT AVG(qh.confidence) FROM QueryHistory qh")
    Double findAverageConfidence();

    @Query("SELECT COUNT(qh) FROM QueryHistory qh WHERE qh.piiRedacted = true")
    Long countPiiRedactions();

    @Query("SELECT COUNT(qh) FROM QueryHistory qh WHERE qh.clarificationAsked = true")
    Long countClarifications();

    @Query("SELECT qh.modelUsed, COUNT(qh) as count FROM QueryHistory qh GROUP BY qh.modelUsed ORDER BY count DESC")
    List<Object[]> findModelUsageStats();
}


