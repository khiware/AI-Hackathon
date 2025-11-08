package com.askbit.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopQuestionResponse {
    private String question;
    private Long count;
    private Double avgConfidence;
    private LocalDateTime lastAsked;
}

