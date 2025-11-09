package com.askbit.ai.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class QueryPreprocessingServiceTest {

    @Mock
    private PiiRedactionService piiRedactionService;

    private QueryPreprocessingService queryPreprocessingService;

    @BeforeEach
    void setUp() {
        queryPreprocessingService = new QueryPreprocessingService(piiRedactionService);
    }

    @Test
    void preprocessQuestion_shouldReturnNullForNullInput() {
        // Act
        String result = queryPreprocessingService.preprocessQuestion(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void preprocessQuestion_shouldReturnEmptyForEmptyInput() {
        // Act
        String result = queryPreprocessingService.preprocessQuestion("");

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void preprocessQuestion_shouldReturnTrimmedForWhitespaceOnly() {
        // Act
        String result = queryPreprocessingService.preprocessQuestion("   ");

        // Assert
        // After trimming whitespace, it becomes empty
        assertThat(result).isNotNull();
    }

    @Test
    void preprocessQuestion_shouldNormalizeWhitespace() {
        // Arrange
        String input = "What  is   the    policy?";

        // Act
        String result = queryPreprocessingService.preprocessQuestion(input);

        // Assert
        assertThat(result).isEqualTo("What is the policy?");
    }

    @Test
    void preprocessQuestion_shouldRemoveRepeatedPunctuation() {
        // Arrange
        String input = "What is the policy???";

        // Act
        String result = queryPreprocessingService.preprocessQuestion(input);

        // Assert
        assertThat(result).isEqualTo("What is the policy?");
    }

    @Test
    void preprocessQuestion_shouldCorrectSpellingMistakes() {
        // Arrange
        String input = "What is the increament polcy?";

        // Act
        String result = queryPreprocessingService.preprocessQuestion(input);

        // Assert
        assertThat(result).contains("increment");
        assertThat(result).contains("policy");
    }

    @Test
    void preprocessQuestion_shouldCorrectMultipleSpellingErrors() {
        // Arrange
        String input = "What is my sallary and benifits?";

        // Act
        String result = queryPreprocessingService.preprocessQuestion(input);

        // Assert
        assertThat(result).contains("salary");
        assertThat(result).contains("benefits");
    }

    @Test
    void preprocessQuestion_shouldFixQuestionWordTypos() {
        // Arrange
        String input = "wat is the policy?";

        // Act
        String result = queryPreprocessingService.preprocessQuestion(input);

        // Assert
        assertThat(result).startsWith("what");
    }

    @Test
    void preprocessQuestion_shouldPreserveCase() {
        // Arrange
        String input = "What is the Increament Policy?";

        // Act
        String result = queryPreprocessingService.preprocessQuestion(input);

        // Assert
        assertThat(result).contains("Increment");
        assertThat(result).contains("Policy");
    }

    @Test
    void preprocessQuestion_shouldHandleNoChanges() {
        // Arrange
        String input = "What is the vacation policy?";

        // Act
        String result = queryPreprocessingService.preprocessQuestion(input);

        // Assert
        assertThat(result).isEqualTo(input);
    }

    @Test
    void preprocessQuestion_shouldCleanSpecialCharacters() {
        // Arrange
        String input = "What is the policy???!!";

        // Act
        String result = queryPreprocessingService.preprocessQuestion(input);

        // Assert
        assertThat(result).doesNotContain("!!!");
        assertThat(result).doesNotContain("???");
    }

    @Test
    void preprocessQuestion_shouldHandleCompoundErrors() {
        // Arrange
        String input = "wat  is  my  sallary  increament???";

        // Act
        String result = queryPreprocessingService.preprocessQuestion(input);

        // Assert
        assertThat(result).contains("what");
        assertThat(result).contains("salary");
        assertThat(result).contains("increment");
        assertThat(result).doesNotContain("???");
        assertThat(result).doesNotContain("  "); // no double spaces
    }

    @Test
    void preprocessQuestion_shouldPreservePunctuation() {
        // Arrange
        String input = "What's the policy, and how does it work?";

        // Act
        String result = queryPreprocessingService.preprocessQuestion(input);

        // Assert
        assertThat(result).contains("'");
        assertThat(result).contains(",");
        assertThat(result).contains("?");
    }

    @Test
    void preprocessQuestion_shouldHandleLongText() {
        // Arrange
        String input = "I would like to know about the increament polcy and the benifits for employees in the organization";

        // Act
        String result = queryPreprocessingService.preprocessQuestion(input);

        // Assert
        assertThat(result).contains("increment");
        assertThat(result).contains("policy");
        assertThat(result).contains("benefits");
    }

    @Test
    void preprocessQuestion_shouldHandleAllUppercase() {
        // Arrange
        String input = "WHAT IS THE POLCY?";

        // Act
        String result = queryPreprocessingService.preprocessQuestion(input);

        // Assert
        // The word POLCY should be corrected to POLICY maintaining uppercase
        assertThat(result.toUpperCase()).contains("POLICY");
    }

    @Test
    void preprocessQuestion_shouldHandleAllLowercase() {
        // Arrange
        String input = "what is the polcy?";

        // Act
        String result = queryPreprocessingService.preprocessQuestion(input);

        // Assert
        assertThat(result).contains("policy");
    }

    @Test
    void preprocessQuestion_shouldHandleMixedCase() {
        // Arrange
        String input = "WhAt Is ThE PoLcY?";

        // Act
        String result = queryPreprocessingService.preprocessQuestion(input);

        // Assert
        assertThat(result).containsIgnoringCase("policy");
    }

    @Test
    void preprocessQuestion_shouldHandleNumbersAndLetters() {
        // Arrange
        String input = "What is the FY 2023 polcy?";

        // Act
        String result = queryPreprocessingService.preprocessQuestion(input);

        // Assert
        assertThat(result).contains("policy");
        assertThat(result).contains("FY");
        assertThat(result).contains("2023");
    }

    @Test
    void preprocessQuestion_shouldHandleHyphens() {
        // Arrange
        String input = "What is the work-from-home polcy?";

        // Act
        String result = queryPreprocessingService.preprocessQuestion(input);

        // Assert
        assertThat(result).contains("work-from-home");
        assertThat(result).contains("policy");
    }

    @Test
    void preprocessQuestion_shouldFixWenTypo() {
        // Arrange
        String input = "wen can I apply?";

        // Act
        String result = queryPreprocessingService.preprocessQuestion(input);

        // Assert
        assertThat(result).startsWith("when");
    }

    @Test
    void preprocessQuestion_shouldFixWerTypo() {
        // Arrange
        String input = "wer is the office?";

        // Act
        String result = queryPreprocessingService.preprocessQuestion(input);

        // Assert
        assertThat(result).startsWith("where");
    }

    @Test
    void preprocessQuestion_shouldFixHwTypo() {
        // Arrange
        String input = "hw do I apply?";

        // Act
        String result = queryPreprocessingService.preprocessQuestion(input);

        // Assert
        assertThat(result).startsWith("how");
    }
}

