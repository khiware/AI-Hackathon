package com.askbit.ai.service;

import com.askbit.ai.dto.ModelResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.util.List;
import java.util.concurrent.*;

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

    @Value("${askbit.ai.model.timeout-ms:30000}")
    private long timeoutMs;

    private static final int MAX_RETRIES = 2;
    private static final long RETRY_DELAY_MS = 1000;
    private static final String FALLBACK_MESSAGE = "Service temporarily slow — try again in 30s";

    // Thread pool for timeout handling
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    public ModelResponse generateResponse(String prompt) {
        return generateResponseWithFailover(prompt, null);
    }

    public ModelResponse generateResponseWithFailover(String prompt, String cachedResponse) {
        // Try primary model first
        ModelResponse primaryResponse = attemptModelCall(prompt, primaryModel, 1, "primary");

        if (primaryResponse.getSuccess()) {
            return primaryResponse;
        }

        // Log primary failure
        logFailoverEvent("PRIMARY_FAILED", primaryModel, primaryResponse.getError());

        // If primary failed, try secondary model
        if (fallbackEnabled && secondaryModel != null && !secondaryModel.isEmpty()) {
            log.warn("Primary model failed, attempting secondary model: {}", secondaryModel);
            ModelResponse secondaryResponse = attemptModelCall(prompt, secondaryModel, 1, "secondary");

            if (secondaryResponse.getSuccess()) {
                logFailoverEvent("SECONDARY_SUCCESS", secondaryModel, "Failover successful");
                return secondaryResponse;
            }

            logFailoverEvent("SECONDARY_FAILED", secondaryModel, secondaryResponse.getError());
        }

        // If both models failed, try cached response
        if (cachedResponse != null && !cachedResponse.isEmpty()) {
            log.warn("Both models failed, using cached response");
            logFailoverEvent("CACHE_FALLBACK", "cached", "Using cached response");

            return ModelResponse.builder()
                    .content(cachedResponse)
                    .modelUsed("cached")
                    .responseTimeMs(0L)
                    .fromCache(true)
                    .success(true)
                    .failoverUsed(true)
                    .failoverReason("Both primary and secondary models failed")
                    .build();
        }

        // Final fallback: return user-friendly error message
        log.error("All failover mechanisms exhausted, returning fallback message");
        logFailoverEvent("FINAL_FALLBACK", "none", "All mechanisms exhausted");

        return ModelResponse.builder()
                .content(FALLBACK_MESSAGE)
                .modelUsed("fallback")
                .responseTimeMs(0L)
                .fromCache(false)
                .success(true) // Set to true so UI displays the message
                .failoverUsed(true)
                .failoverReason("All models failed - service degradation")
                .build();
    }

    private ModelResponse attemptModelCall(String prompt, String modelName, int attemptNumber, String modelType) {
        long startTime = System.currentTimeMillis();

        try {
            log.debug("Attempting {} model: {} (attempt {})", modelType, modelName, attemptNumber);

            // Create callable for timeout handling
            Callable<ChatResponse> task = () -> {
                Message userMessage = new UserMessage(prompt);
                Prompt aiPrompt = new Prompt(List.of(userMessage));
                return chatModel.call(aiPrompt);
            };

            // Execute with timeout
            Future<ChatResponse> future = executorService.submit(task);
            ChatResponse response;

            try {
                response = future.get(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                future.cancel(true);
                long failureTime = System.currentTimeMillis() - startTime;
                log.error("Model {} timed out after {}ms", modelName, failureTime);

                return ModelResponse.builder()
                        .content(null)
                        .modelUsed(modelName)
                        .responseTimeMs(failureTime)
                        .fromCache(false)
                        .success(false)
                        .error("TIMEOUT: Model took longer than " + timeoutMs + "ms")
                        .failoverUsed(false)
                        .build();
            }

            long responseTime = System.currentTimeMillis() - startTime;
            String generatedText = response.getResults().get(0).getOutput().getContent();

            log.info("Model {} ({}) responded successfully in {}ms", modelName, modelType, responseTime);

            return ModelResponse.builder()
                    .content(generatedText)
                    .modelUsed(modelName)
                    .responseTimeMs(responseTime)
                    .fromCache(false)
                    .success(true)
                    .failoverUsed(false)
                    .build();

        } catch (HttpClientErrorException e) {
            long failureTime = System.currentTimeMillis() - startTime;
            String errorType = "HTTP_" + e.getStatusCode().value();

            // Handle specific error codes
            if (e.getStatusCode().value() == 429) {
                log.error("Model {} rate limited (429 Too Many Requests)", modelName);
                errorType = "RATE_LIMIT_429";
            } else if (e.getStatusCode().value() >= 400 && e.getStatusCode().value() < 500) {
                log.error("Model {} client error: {} {}", modelName, e.getStatusCode(), e.getMessage());
                errorType = "CLIENT_ERROR_" + e.getStatusCode().value();
            }

            return ModelResponse.builder()
                    .content(null)
                    .modelUsed(modelName)
                    .responseTimeMs(failureTime)
                    .fromCache(false)
                    .success(false)
                    .error(errorType + ": " + e.getMessage())
                    .failoverUsed(false)
                    .build();

        } catch (HttpServerErrorException e) {
            long failureTime = System.currentTimeMillis() - startTime;
            log.error("Model {} server error: {} {}", modelName, e.getStatusCode(), e.getMessage());

            return ModelResponse.builder()
                    .content(null)
                    .modelUsed(modelName)
                    .responseTimeMs(failureTime)
                    .fromCache(false)
                    .success(false)
                    .error("SERVER_ERROR_" + e.getStatusCode().value() + ": " + e.getMessage())
                    .failoverUsed(false)
                    .build();

        } catch (ResourceAccessException e) {
            long failureTime = System.currentTimeMillis() - startTime;
            log.error("Model {} connection error: {}", modelName, e.getMessage());

            return ModelResponse.builder()
                    .content(null)
                    .modelUsed(modelName)
                    .responseTimeMs(failureTime)
                    .fromCache(false)
                    .success(false)
                    .error("CONNECTION_ERROR: " + e.getMessage())
                    .failoverUsed(false)
                    .build();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            long failureTime = System.currentTimeMillis() - startTime;
            log.error("Model {} interrupted: {}", modelName, e.getMessage());

            return ModelResponse.builder()
                    .content(null)
                    .modelUsed(modelName)
                    .responseTimeMs(failureTime)
                    .fromCache(false)
                    .success(false)
                    .error("INTERRUPTED: " + e.getMessage())
                    .failoverUsed(false)
                    .build();

        } catch (ExecutionException e) {
            long failureTime = System.currentTimeMillis() - startTime;
            Throwable cause = e.getCause();
            log.error("Model {} execution error: {}", modelName, cause != null ? cause.getMessage() : e.getMessage());

            return ModelResponse.builder()
                    .content(null)
                    .modelUsed(modelName)
                    .responseTimeMs(failureTime)
                    .fromCache(false)
                    .success(false)
                    .error("EXECUTION_ERROR: " + (cause != null ? cause.getMessage() : e.getMessage()))
                    .failoverUsed(false)
                    .build();

        } catch (Exception e) {
            long failureTime = System.currentTimeMillis() - startTime;
            log.error("Model {} unexpected error: {}", modelName, e.getMessage(), e);

            return ModelResponse.builder()
                    .content(null)
                    .modelUsed(modelName)
                    .responseTimeMs(failureTime)
                    .fromCache(false)
                    .success(false)
                    .error("UNEXPECTED_ERROR: " + e.getMessage())
                    .failoverUsed(false)
                    .build();
        }
    }

    private void logFailoverEvent(String eventType, String model, String reason) {
        // Structured logging for failover events
        log.warn("FAILOVER_EVENT | Type: {} | Model: {} | Reason: {} | Timestamp: {}",
                eventType, model, reason, System.currentTimeMillis());

        // TODO: Can be enhanced to send to monitoring system (e.g., CloudWatch, Datadog)
    }

    public boolean shouldUseFastModel(String question) {
        // Use fast model for short questions
        return question != null && question.length() < 100;
    }

    public boolean isLatencyAcceptable(long responseTimeMs) {
        return responseTimeMs <= latencyThresholdMs;
    }
}

