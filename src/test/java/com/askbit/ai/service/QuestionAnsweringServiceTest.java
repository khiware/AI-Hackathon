package com.askbit.ai.service;

import com.askbit.ai.dto.AskRequest;
import com.askbit.ai.dto.AskResponse;
import com.askbit.ai.repository.DocumentRepository;
import com.askbit.ai.repository.QueryHistoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuestionAnsweringServiceTest {

    @Mock
    private RetrievalService retrievalService;

    @Mock
    private HybridRetrievalService hybridRetrievalService;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private ModelRouterService modelRouterService;

    @Mock
    private PiiRedactionService piiRedactionService;

    @Mock
    private QueryHistoryRepository queryHistoryRepository;

    @Mock
    private ClarificationService clarificationService;

    @Mock
    private CacheService cacheService;

    @Mock
    private TemporalQueryAnalyzer temporalQueryAnalyzer;

    @Mock
    private QueryPreprocessingService queryPreprocessingService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private QuestionAnsweringService questionAnsweringService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(questionAnsweringService, "confidenceThreshold", 0.7);
        ReflectionTestUtils.setField(questionAnsweringService, "useHybridSearch", true);
        ReflectionTestUtils.setField(questionAnsweringService, "maxRetrievalResults", 5);
    }

    @Test
    void answerQuestion_shouldReturnErrorForEmptyQuestion() {
        // Arrange
        AskRequest request = AskRequest.builder().question("").build();

        // Act
        AskResponse response = questionAnsweringService.answerQuestion(request);

        // Assert
        assertThat(response.getAnswer()).contains("Question cannot be empty");
        assertThat(response.getConfidence()).isEqualTo(0.0);
    }

    @Test
    void answerQuestion_shouldReturnErrorForNullQuestion() {
        // Arrange
        AskRequest request = AskRequest.builder().question(null).build();

        // Act
        AskResponse response = questionAnsweringService.answerQuestion(request);

        // Assert
        assertThat(response.getAnswer()).contains("Question cannot be empty");
    }

    @Test
    void answerQuestion_shouldHandleGreeting() {
        // Arrange
        AskRequest request = AskRequest.builder().question("Hello").build();
        lenient().when(queryPreprocessingService.preprocessQuestion(anyString())).thenReturn("Hello");
        lenient().when(piiRedactionService.redactPii(anyString())).thenAnswer(i -> i.getArguments()[0]);
        lenient().when(temporalQueryAnalyzer.analyzeQuestion(anyString()))
                .thenReturn(TemporalQueryAnalyzer.TemporalContext.latest());

        // Act
        AskResponse response = questionAnsweringService.answerQuestion(request);

        // Assert
        assertThat(response.getAnswer()).isNotEmpty();
        assertThat(response.getResponseTimeMs()).isNotNull();
    }

    @Test
    void answerQuestion_shouldPreprocessQuestion() {
        // Arrange
        AskRequest request = AskRequest.builder().question("wat is polcy?").build();

        when(queryPreprocessingService.preprocessQuestion("wat is polcy?"))
                .thenReturn("what is policy?");
        lenient().when(piiRedactionService.redactPii(anyString())).thenAnswer(i -> i.getArguments()[0]);
        lenient().when(clarificationService.needsClarification(anyString())).thenReturn(false);
        lenient().when(temporalQueryAnalyzer.analyzeQuestion(anyString()))
                .thenReturn(TemporalQueryAnalyzer.TemporalContext.latest());
        lenient().when(cacheService.getFromCache(anyString())).thenReturn(null);
        lenient().when(hybridRetrievalService.hybridSearchWithVersionFilter(anyString(), anyInt(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        questionAnsweringService.answerQuestion(request);

        // Assert
        verify(queryPreprocessingService, times(1)).preprocessQuestion("wat is polcy?");
    }

    @Test
    void answerQuestion_shouldRedactPII() {
        // Arrange
        AskRequest request = AskRequest.builder()
                .question("My email is john@example.com")
                .build();

        lenient().when(queryPreprocessingService.preprocessQuestion(anyString()))
                .thenAnswer(i -> i.getArguments()[0]);
        when(piiRedactionService.redactPii("My email is john@example.com"))
                .thenReturn("My email is [EMAIL]");
        lenient().when(piiRedactionService.redactPii("My email is [EMAIL]"))
                .thenReturn("My email is [EMAIL]");
        lenient().when(clarificationService.needsClarification(anyString())).thenReturn(false);
        lenient().when(temporalQueryAnalyzer.analyzeQuestion(anyString()))
                .thenReturn(TemporalQueryAnalyzer.TemporalContext.latest());
        lenient().when(cacheService.getFromCache(anyString())).thenReturn(null);
        lenient().when(hybridRetrievalService.hybridSearchWithVersionFilter(anyString(), anyInt(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        questionAnsweringService.answerQuestion(request);

        // Assert
        verify(piiRedactionService, atLeastOnce()).redactPii(anyString());
    }

    @Test
    void answerQuestion_shouldCheckClarification() {
        // Arrange
        AskRequest request = AskRequest.builder()
                .question("Tell me about PTO")
                .build();

        lenient().when(queryPreprocessingService.preprocessQuestion(anyString()))
                .thenAnswer(i -> i.getArguments()[0]);
        lenient().when(piiRedactionService.redactPii(anyString()))
                .thenAnswer(i -> i.getArguments()[0]);
        when(clarificationService.needsClarification("Tell me about PTO")).thenReturn(true);
        when(clarificationService.generateClarificationQuestion("Tell me about PTO"))
                .thenReturn("Are you asking about vacation PTO or sick PTO?");
        lenient().when(temporalQueryAnalyzer.analyzeQuestion(anyString()))
                .thenReturn(TemporalQueryAnalyzer.TemporalContext.latest());
        lenient().when(cacheService.getFromCache(anyString())).thenReturn(null);

        // Act
        AskResponse response = questionAnsweringService.answerQuestion(request);

        // Assert
        assertThat(response.getNeedsClarification()).isTrue();
        assertThat(response.getClarificationQuestion()).isNotEmpty();
        verify(clarificationService, times(1)).needsClarification("Tell me about PTO");
    }
}

