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
public class ModelResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String content;
    private String modelUsed;
    private Long responseTimeMs;
    private Boolean fromCache;
    private Boolean success;
    private String error;
    private Boolean failoverUsed;
    private String failoverReason;
}

