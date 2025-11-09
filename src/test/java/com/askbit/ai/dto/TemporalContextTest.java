package com.askbit.ai.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TemporalContextTest {

    @Test
    void latest_shouldCreateLatestVersionContext() {
        // Act
        TemporalContext context = TemporalContext.latest();

        // Assert
        assertThat(context.isUseLatestVersion()).isTrue();
        assertThat(context.getTargetYear()).isNull();
        assertThat(context.isNeedsClarification()).isFalse();
        assertThat(context.getClarificationReason()).isNull();
        assertThat(context.isHistoricalQuery()).isFalse();
    }

    @Test
    void forYear_shouldCreateYearSpecificContext() {
        // Act
        TemporalContext context = TemporalContext.forYear(2023);

        // Assert
        assertThat(context.isUseLatestVersion()).isFalse();
        assertThat(context.getTargetYear()).isEqualTo(2023);
        assertThat(context.isNeedsClarification()).isFalse();
        assertThat(context.getClarificationReason()).isNull();
        assertThat(context.isHistoricalQuery()).isTrue();
    }

    @Test
    void needsClarification_shouldCreateClarificationContext() {
        // Act
        TemporalContext context = TemporalContext.needsClarification();

        // Assert
        assertThat(context.isUseLatestVersion()).isFalse();
        assertThat(context.getTargetYear()).isNull();
        assertThat(context.isNeedsClarification()).isTrue();
        assertThat(context.getClarificationReason()).isEqualTo(
                "Your question refers to a previous policy. Which year or version are you asking about?");
        assertThat(context.isHistoricalQuery()).isFalse();
    }

    @Test
    void isHistoricalQuery_shouldReturnTrueForYearBasedQuery() {
        // Arrange
        TemporalContext context = new TemporalContext(false, 2022, false, null);

        // Act & Assert
        assertThat(context.isHistoricalQuery()).isTrue();
    }

    @Test
    void isHistoricalQuery_shouldReturnFalseForLatestVersion() {
        // Arrange
        TemporalContext context = new TemporalContext(true, null, false, null);

        // Act & Assert
        assertThat(context.isHistoricalQuery()).isFalse();
    }

    @Test
    void isHistoricalQuery_shouldReturnFalseWhenNoTargetYear() {
        // Arrange
        TemporalContext context = new TemporalContext(false, null, false, null);

        // Act & Assert
        assertThat(context.isHistoricalQuery()).isFalse();
    }

    @Test
    void constructor_shouldCreateValidObject() {
        // Act
        TemporalContext context = new TemporalContext(true, 2024, false, "Test reason");

        // Assert
        assertThat(context.isUseLatestVersion()).isTrue();
        assertThat(context.getTargetYear()).isEqualTo(2024);
        assertThat(context.isNeedsClarification()).isFalse();
        assertThat(context.getClarificationReason()).isEqualTo("Test reason");
    }

    @Test
    void settersAndGetters_shouldWork() {
        // Arrange
        TemporalContext context = new TemporalContext(false, null, false, null);

        // Act
        context.setUseLatestVersion(true);
        context.setTargetYear(2025);
        context.setNeedsClarification(true);
        context.setClarificationReason("Updated reason");

        // Assert
        assertThat(context.isUseLatestVersion()).isTrue();
        assertThat(context.getTargetYear()).isEqualTo(2025);
        assertThat(context.isNeedsClarification()).isTrue();
        assertThat(context.getClarificationReason()).isEqualTo("Updated reason");
    }

    @Test
    void equals_shouldWorkCorrectly() {
        // Arrange
        TemporalContext context1 = TemporalContext.latest();
        TemporalContext context2 = TemporalContext.latest();

        // Assert
        assertThat(context1).isEqualTo(context2);
    }

    @Test
    void hashCode_shouldBeConsistent() {
        // Arrange
        TemporalContext context = TemporalContext.forYear(2023);

        // Act
        int hash1 = context.hashCode();
        int hash2 = context.hashCode();

        // Assert
        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    void toString_shouldWork() {
        // Arrange
        TemporalContext context = TemporalContext.forYear(2023);

        // Act
        String result = context.toString();

        // Assert
        assertThat(result).isNotEmpty();
        assertThat(result).contains("2023");
    }

    @Test
    void forYear_shouldHandleDifferentYears() {
        // Act
        TemporalContext context2020 = TemporalContext.forYear(2020);
        TemporalContext context2024 = TemporalContext.forYear(2024);

        // Assert
        assertThat(context2020.getTargetYear()).isEqualTo(2020);
        assertThat(context2024.getTargetYear()).isEqualTo(2024);
        assertThat(context2020.isHistoricalQuery()).isTrue();
        assertThat(context2024.isHistoricalQuery()).isTrue();
    }

    @Test
    void shouldSupportClarificationWithCustomMessage() {
        // Arrange
        TemporalContext context = new TemporalContext(
                false,
                null,
                true,
                "Which version of the policy are you asking about?"
        );

        // Assert
        assertThat(context.isNeedsClarification()).isTrue();
        assertThat(context.getClarificationReason()).isEqualTo("Which version of the policy are you asking about?");
    }

    @Test
    void shouldDifferentiateBetweenLatestAndHistorical() {
        // Arrange
        TemporalContext latest = TemporalContext.latest();
        TemporalContext historical = TemporalContext.forYear(2021);

        // Assert
        assertThat(latest.isUseLatestVersion()).isTrue();
        assertThat(latest.isHistoricalQuery()).isFalse();

        assertThat(historical.isUseLatestVersion()).isFalse();
        assertThat(historical.isHistoricalQuery()).isTrue();
    }
}

