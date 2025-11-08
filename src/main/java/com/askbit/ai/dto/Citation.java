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
public class Citation implements Serializable {

    private static final long serialVersionUID = 1L;

    private String documentId;
    private String fileName;
    private String version;
    private Integer pageNumber;
    private String section;
    private Integer startLine;
    private Integer endLine;
    private String snippet;
    private Double relevanceScore;
}

