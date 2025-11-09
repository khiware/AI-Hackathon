package com.askbit.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AskResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String answer;
    private List<Citation> citations;
    private Double confidence;
    private Boolean cached;
    private String clarificationQuestion;
    private Boolean needsClarification;
    private Long responseTimeMs;
    private String modelUsed;
    private Boolean piiRedacted;
    private String preprocessedQuestion;  // Shows what was understood after preprocessing
}

