package com.askbit.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * Data class to hold temporal context analysis results from query analysis.
 * Used to determine which version of documents to retrieve based on temporal indicators in the query.
 */
@Data
@AllArgsConstructor
public class TemporalContext implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean useLatestVersion;
    private Integer targetYear;
    private boolean needsClarification;
    private String clarificationReason;

    public static TemporalContext latest() {
        return new TemporalContext(true, null, false, null);
    }

    public static TemporalContext forYear(int year) {
        return new TemporalContext(false, year, false, null);
    }

    public static TemporalContext needsClarification() {
        return new TemporalContext(false, null, true,
                "Your question refers to a previous policy. Which year or version are you asking about?");
    }

    public boolean isHistoricalQuery() {
        return !useLatestVersion && targetYear != null;
    }
}

