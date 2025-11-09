package com.askbit.ai.service;

import com.askbit.ai.service.TemporalQueryAnalyzer.TemporalContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TemporalQueryAnalyzerTest {

    @InjectMocks
    private TemporalQueryAnalyzer temporalQueryAnalyzer;

    @Test
    void analyzeQuestion_shouldReturnLatestForNullQuestion() {
        // Act
        TemporalContext result = temporalQueryAnalyzer.analyzeQuestion(null);

        // Assert
        assertThat(result.isUseLatestVersion()).isTrue();
        assertThat(result.getTargetYear()).isNull();
        assertThat(result.isNeedsClarification()).isFalse();
    }

    @Test
    void analyzeQuestion_shouldReturnLatestForEmptyQuestion() {
        // Act
        TemporalContext result = temporalQueryAnalyzer.analyzeQuestion("");

        // Assert
        assertThat(result.isUseLatestVersion()).isTrue();
        assertThat(result.getTargetYear()).isNull();
    }

    @Test
    void analyzeQuestion_shouldDetectExplicitYear() {
        // Arrange
        String question = "What was the policy in 2022?";

        // Act
        TemporalContext result = temporalQueryAnalyzer.analyzeQuestion(question);

        // Assert
        assertThat(result.isUseLatestVersion()).isFalse();
        assertThat(result.getTargetYear()).isEqualTo(2022);
    }

    @Test
    void analyzeQuestion_shouldDetectFiscalYear() {
        // Arrange
        String question = "What is the policy for FY 2023-24?";

        // Act
        TemporalContext result = temporalQueryAnalyzer.analyzeQuestion(question);

        // Assert
        assertThat(result.getTargetYear()).isEqualTo(2023);
    }

    @Test
    void analyzeQuestion_shouldDetectCurrentYear() {
        // Arrange
        int currentYear = Year.now().getValue();
        String question = "What is the policy in " + currentYear + "?";

        // Act
        TemporalContext result = temporalQueryAnalyzer.analyzeQuestion(question);

        // Assert
        assertThat(result.isUseLatestVersion()).isTrue();
    }

    @Test
    void analyzeQuestion_shouldDetectTemporalKeywordPrevious() {
        // Arrange
        String question = "What was the previous policy?";

        // Act
        TemporalContext result = temporalQueryAnalyzer.analyzeQuestion(question);

        // Assert
        assertThat(result.isNeedsClarification()).isTrue();
    }

    @Test
    void analyzeQuestion_shouldDetectTemporalKeywordOld() {
        // Arrange
        String question = "Show me the old policy";

        // Act
        TemporalContext result = temporalQueryAnalyzer.analyzeQuestion(question);

        // Assert
        assertThat(result.isNeedsClarification()).isTrue();
    }

    @Test
    void analyzeQuestion_shouldDetectCurrentKeyword() {
        // Arrange
        String question = "What is the current policy?";

        // Act
        TemporalContext result = temporalQueryAnalyzer.analyzeQuestion(question);

        // Assert
        assertThat(result.isUseLatestVersion()).isTrue();
    }

    @Test
    void analyzeQuestion_shouldDetectLatestKeyword() {
        // Arrange
        String question = "Show me the latest policy";

        // Act
        TemporalContext result = temporalQueryAnalyzer.analyzeQuestion(question);

        // Assert
        assertThat(result.isUseLatestVersion()).isTrue();
    }

    @Test
    void analyzeQuestion_shouldDefaultToLatest() {
        // Arrange
        String question = "What is the WFH policy?";

        // Act
        TemporalContext result = temporalQueryAnalyzer.analyzeQuestion(question);

        // Assert
        assertThat(result.isUseLatestVersion()).isTrue();
        assertThat(result.isNeedsClarification()).isFalse();
    }

    @Test
    void analyzeQuestion_shouldHandleWhitespace() {
        // Arrange
        String question = "   What is the policy?   ";

        // Act
        TemporalContext result = temporalQueryAnalyzer.analyzeQuestion(question);

        // Assert
        assertThat(result.isUseLatestVersion()).isTrue();
    }

    @Test
    void temporalContext_latest_shouldCreateCorrectly() {
        // Act
        TemporalContext result = TemporalContext.latest();

        // Assert
        assertThat(result.isUseLatestVersion()).isTrue();
        assertThat(result.getTargetYear()).isNull();
        assertThat(result.isNeedsClarification()).isFalse();
    }

    @Test
    void temporalContext_forYear_shouldCreateCorrectly() {
        // Act
        TemporalContext result = TemporalContext.forYear(2022);

        // Assert
        assertThat(result.isUseLatestVersion()).isFalse();
        assertThat(result.getTargetYear()).isEqualTo(2022);
        assertThat(result.isNeedsClarification()).isFalse();
    }

    @Test
    void temporalContext_needsClarification_shouldCreateCorrectly() {
        // Act
        TemporalContext result = TemporalContext.needsClarification();

        // Assert
        assertThat(result.isNeedsClarification()).isTrue();
    }

    @Test
    void analyzeQuestion_shouldDetectHistoricalKeyword() {
        // Arrange
        String question = "What was the historical policy?";

        // Act
        TemporalContext result = temporalQueryAnalyzer.analyzeQuestion(question);

        // Assert
        assertThat(result.isNeedsClarification()).isTrue();
    }

    @Test
    void analyzeQuestion_shouldDetectLastYearKeyword() {
        // Arrange
        String question = "What was the policy last year?";

        // Act
        TemporalContext result = temporalQueryAnalyzer.analyzeQuestion(question);

        // Assert
        assertThat(result.isNeedsClarification()).isTrue();
    }
}

