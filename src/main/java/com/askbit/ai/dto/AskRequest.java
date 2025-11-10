package com.askbit.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AskRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private String question;
    private String context;

    /**
     * Optional: Full conversation history for multi-turn clarifications
     * Format: "leave > vacation > unpaid" or just append all previous terms
     */
    private String conversationHistory;
}