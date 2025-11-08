package com.askbit.ai.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModelRouterService {

    private final ChatModel chatModel;

    @Value("${askbit.ai.model.router.latency-threshold-ms:1500}")
    private long latencyThresholdMs;

    @Value("${askbit.ai.model.router.fallback-enabled:true}")
    private boolean fallbackEnabled;

    @Value("${askbit.ai.models.primary:openai-gpt4}")
    private String primaryModel;

    @Value("${askbit.ai.models.secondary:openai-gpt35}")
    private String secondaryModel;

    private static final int MAX_RETRIES = 2;
    private static final long RETRY_DELAY_MS = 1000;

    public ModelResponse generateResponse(String prompt) {
        return generateResponse(prompt, primaryModel, 0);
    }

    private ModelResponse generateResponse(String prompt, String modelName, int attemptCount) {
        long startTime = System.currentTimeMillis();

        try {
            log.debug("Attempting to generate response with model: {} (attempt {})",
                    modelName, attemptCount + 1);

            // Create prompt
            Message userMessage = new UserMessage(prompt);
            Prompt aiPrompt = new Prompt(List.of(userMessage));

            // Call the model
            ChatResponse response = chatModel.call(aiPrompt);

            long responseTime = System.currentTimeMillis() - startTime;

            String generatedText = response.getResults().get(0).getOutput().getContent();

            log.info("Model {} responded in {}ms", modelName, responseTime);

            return ModelResponse.builder()
                    .content(generatedText)
                    .modelUsed(modelName)
                    .responseTimeMs(responseTime)
                    .fromCache(false)
                    .success(true)
                    .build();

        } catch (Exception e) {
            long failureTime = System.currentTimeMillis() - startTime;
            log.error("Error calling model {} (attempt {}): {}",
                    modelName, attemptCount + 1, e.getMessage());

            // Retry logic
            if (fallbackEnabled && attemptCount < MAX_RETRIES) {
                try {
                    TimeUnit.MILLISECONDS.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }

                // Try secondary model on next attempt
                String nextModel = attemptCount == 0 ? secondaryModel : primaryModel;
                return generateResponse(prompt, nextModel, attemptCount + 1);
            }

            // All retries exhausted
            return ModelResponse.builder()
                    .content("I'm experiencing technical difficulties. Please try again in a moment.")
                    .modelUsed(modelName)
                    .responseTimeMs(failureTime)
                    .fromCache(false)
                    .success(false)
                    .error(e.getMessage())
                    .build();
        }
    }

    public ModelResponse generateResponseWithFallback(String prompt, String cachedResponse) {
        ModelResponse response = generateResponse(prompt);

        // If failed and cache is available, use cached response
        if (!response.getSuccess() && cachedResponse != null && !cachedResponse.isEmpty()) {
            log.info("Using cached response as fallback");
            return ModelResponse.builder()
                    .content(cachedResponse)
                    .modelUsed("cached")
                    .responseTimeMs(0L)
                    .fromCache(true)
                    .success(true)
                    .build();
        }

        return response;
    }

    public boolean shouldUseFastModel(String question) {
        // Use fast model for short questions
        return question != null && question.length() < 100;
    }

    public boolean isLatencyAcceptable(long responseTimeMs) {
        return responseTimeMs <= latencyThresholdMs;
    }

    @Data
    @lombok.Builder
    public static class ModelResponse {
        private final String content;
        private final String modelUsed;
        private final Long responseTimeMs;
        private final Boolean fromCache;
        private final Boolean success;
        private final String error;
    }
}

