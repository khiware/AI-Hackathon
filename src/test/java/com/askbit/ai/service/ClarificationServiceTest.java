package com.askbit.ai.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ClarificationServiceTest {

    @InjectMocks
    private ClarificationService clarificationService;

    @Test
    void needsClarification_shouldReturnFalseForNull() {
        // Act
        boolean result = clarificationService.needsClarification(null);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void needsClarification_shouldReturnFalseForEmpty() {
        // Act
        boolean result = clarificationService.needsClarification("");

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void needsClarification_shouldReturnTrueForVagueQuestion() {
        // Arrange
        String question = "what is the policy?";

        // Act
        boolean result = clarificationService.needsClarification(question);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void needsClarification_shouldReturnTrueForAmbiguousPTO() {
        // Arrange
        String question = "What is the PTO policy?";

        // Act
        boolean result = clarificationService.needsClarification(question);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void needsClarification_shouldReturnTrueForAmbiguousLeave() {
        // Arrange
        String question = "How much leave do I get?";

        // Act
        boolean result = clarificationService.needsClarification(question);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void needsClarification_shouldReturnFalseForSpecificQuestion() {
        // Arrange
        String question = "What is the PTO policy for full-time employees in the US?";

        // Act
        boolean result = clarificationService.needsClarification(question);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void needsClarification_shouldReturnTrueForShortQuestion() {
        // Arrange
        String question = "How?";

        // Act
        boolean result = clarificationService.needsClarification(question);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void needsClarification_shouldReturnFalseForDetailedQuestion() {
        // Arrange
        String question = "What is the work from home policy for remote workers?";

        // Act
        boolean result = clarificationService.needsClarification(question);

        // Assert
        assertThat(result).isFalse();
    }

    @Test
    void generateClarificationQuestion_shouldGenerateForPTO() {
        // Arrange
        String question = "Tell me about PTO";

        // Act
        String result = clarificationService.generateClarificationQuestion(question);

        // Assert
        assertThat(result).contains("pto");
        assertThat(result).contains("full-time employees");
        assertThat(result).contains("part-time employees");
    }

    @Test
    void generateClarificationQuestion_shouldGenerateForLeave() {
        // Arrange
        String question = "What about leave?";

        // Act
        String result = clarificationService.generateClarificationQuestion(question);

        // Assert
        assertThat(result).contains("leave");
        assertThat(result).contains("vacation leave");
        assertThat(result).contains("sick leave");
    }

    @Test
    void generateClarificationQuestion_shouldGenerateForVagueQuestion() {
        // Arrange
        String question = "tell me about it";

        // Act
        String result = clarificationService.generateClarificationQuestion(question);

        // Assert
        assertThat(result).contains("Could you please be more specific");
        assertThat(result).contains("Employee policies");
    }

    @Test
    void generateClarificationQuestion_shouldGenerateForExpense() {
        // Arrange
        String question = "What is the expense policy?";

        // Act
        String result = clarificationService.generateClarificationQuestion(question);

        // Assert
        assertThat(result).contains("expense");
        assertThat(result).contains("travel expenses");
    }

    @Test
    void generateClarificationQuestion_shouldGenerateForBenefit() {
        // Arrange
        String question = "Tell me about benefits";

        // Act
        String result = clarificationService.generateClarificationQuestion(question);

        // Assert
        assertThat(result).contains("benefit");
        assertThat(result).contains("health insurance");
    }

    @Test
    void expandQuestionWithContext_shouldCombineQuestionAndContext() {
        // Arrange
        String original = "What is the PTO policy?";
        String clarification = "full-time employees";

        // Act
        String result = clarificationService.expandQuestionWithContext(original, clarification);

        // Assert
        assertThat(result).contains(original);
        assertThat(result).contains(clarification);
        assertThat(result).contains("specifically about");
    }

    @Test
    void needsClarification_shouldDetectPolicy() {
        // Arrange
        String question = "What is the policy?";

        // Act
        boolean result = clarificationService.needsClarification(question);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void needsClarification_shouldDetectRemoteWork() {
        // Arrange
        String question = "Tell me about remote work";

        // Act
        boolean result = clarificationService.needsClarification(question);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void needsClarification_shouldDetectWorkHours() {
        // Arrange
        String question = "What are the work hours?";

        // Act
        boolean result = clarificationService.needsClarification(question);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void needsClarification_shouldDetectCompensation() {
        // Arrange
        String question = "Tell me about compensation";

        // Act
        boolean result = clarificationService.needsClarification(question);

        // Assert
        assertThat(result).isTrue();
    }

    @Test
    void generateClarificationQuestion_shouldHandleUnknownTerm() {
        // Arrange
        String question = "What is xyz?";

        // Act
        String result = clarificationService.generateClarificationQuestion(question);

        // Assert
        assertThat(result).contains("Could you please be more specific");
    }
}

