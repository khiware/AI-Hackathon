package com.askbit.ai.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "query_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String question;

    @Column(length = 2000)
    private String normalizedQuestion;

    @Column(nullable = false, length = 10000)
    private String answer;

    @Column(nullable = false)
    private Double confidence;

    @Column(nullable = false)
    private Boolean fromCache;

    @Column(nullable = false)
    private LocalDateTime queryTime;

    private Long responseTimeMs;

    private String modelUsed;

    @Column(length = 5000)
    private String citationsJson;

    private Boolean piiRedacted;

    private Boolean clarificationAsked;

    @PrePersist
    protected void onCreate() {
        queryTime = LocalDateTime.now();
        if (fromCache == null) {
            fromCache = false;
        }
        if (piiRedacted == null) {
            piiRedacted = false;
        }
        if (clarificationAsked == null) {
            clarificationAsked = false;
        }
    }
}
