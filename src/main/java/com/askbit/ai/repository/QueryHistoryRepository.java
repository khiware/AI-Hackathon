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
    @Query("SELECT AVG(qh.responseTimeMs) FROM QueryHistory qh WHERE qh.modelUsed != 'cached'")
    Double findAverageResponseTime();

    @Query("SELECT AVG(qh.confidence) FROM QueryHistory qh WHERE qh.modelUsed != 'cached'")
    Double findAverageConfidence();

    @Query("SELECT COUNT(qh) FROM QueryHistory qh WHERE qh.piiRedacted = true")
    Long countPiiRedactions();

    @Query("SELECT COUNT(qh) FROM QueryHistory qh WHERE qh.clarificationAsked = true AND qh.modelUsed != 'cached'")
    Long countClarifications();

    @Query("SELECT qh.modelUsed, COUNT(qh) as count FROM QueryHistory qh WHERE qh.modelUsed != 'cached' GROUP BY qh.modelUsed ORDER BY count DESC")
    List<Object[]> findModelUsageStats();

    @Query("SELECT qh.normalizedQuestion, COUNT(qh) as count, AVG(qh.confidence) as avgConfidence, MAX(qh.queryTime) as lastAsked " +
           "FROM QueryHistory qh " +
           "GROUP BY qh.normalizedQuestion " +
           "ORDER BY count DESC")
    List<Object[]> findTopQuestions();

    // Get all response times sorted for p95 calculation
    @Query("SELECT qh.responseTimeMs FROM QueryHistory qh WHERE qh.responseTimeMs IS NOT NULL AND qh.modelUsed != 'cached' ORDER BY qh.responseTimeMs DESC")
    List<Long> findAllResponseTimesSorted();

    @Query("SELECT COUNT(qh) FROM QueryHistory qh WHERE qh.modelUsed != 'cached'")
    Long countModelHitQueries();
}


