package com.askbit.ai.dto;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class CitationTest {

    @Test
    void builder_shouldCreateValidObject() {
        Citation citation = Citation.builder()
                .documentId("doc123")
                .fileName("policy.pdf")
                .pageNumber(5)
                .snippet("Test snippet")
                .relevanceScore(0.92)
                .build();

        assertThat(citation.getDocumentId()).isEqualTo("doc123");
        assertThat(citation.getFileName()).isEqualTo("policy.pdf");
        assertThat(citation.getPageNumber()).isEqualTo(5);
        assertThat(citation.getRelevanceScore()).isEqualTo(0.92);
    }

    @Test
    void settersAndGetters_shouldWork() {
        Citation citation = new Citation();
        citation.setDocumentId("doc1");
        citation.setFileName("test.pdf");

        assertThat(citation.getDocumentId()).isEqualTo("doc1");
        assertThat(citation.getFileName()).isEqualTo("test.pdf");
    }
}

