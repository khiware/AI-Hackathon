package com.askbit.ai.controller;

import com.askbit.ai.dto.AskRequest;
import com.askbit.ai.dto.AskResponse;
import com.askbit.ai.service.QuestionAnsweringService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AskController.class)
class AskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private QuestionAnsweringService questionAnsweringService;

    @Test
    void ask_shouldReturnSuccessResponse() throws Exception {
        // Arrange
        AskRequest request = AskRequest.builder()
                .question("What is the WFH policy?")
                .build();

        AskResponse response = AskResponse.builder()
                .answer("The WFH policy allows employees to work from home up to 3 days per week.")
                .confidence(0.92)
                .cached(false)
                .citations(new ArrayList<>())
                .build();

        when(questionAnsweringService.answerQuestion(any(AskRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value(response.getAnswer()))
                .andExpect(jsonPath("$.confidence").value(0.92))
                .andExpect(jsonPath("$.cached").value(false));
    }

    @Test
    void ask_shouldHandleException() throws Exception {
        // Arrange
        AskRequest request = AskRequest.builder()
                .question("Test question")
                .build();

        when(questionAnsweringService.answerQuestion(any(AskRequest.class)))
                .thenThrow(new RuntimeException("Service error"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.answer").value("An error occurred while processing your question. Please try again."))
                .andExpect(jsonPath("$.confidence").value(0.0))
                .andExpect(jsonPath("$.cached").value(false));
    }

    @Test
    void ask_shouldReturnCachedResponse() throws Exception {
        // Arrange
        AskRequest request = AskRequest.builder()
                .question("What is the WFH policy?")
                .build();

        AskResponse response = AskResponse.builder()
                .answer("Cached answer")
                .confidence(0.95)
                .cached(true)
                .responseTimeMs(50L)
                .build();

        when(questionAnsweringService.answerQuestion(any(AskRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/ask")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cached").value(true))
                .andExpect(jsonPath("$.responseTimeMs").value(50));
    }

    @Test
    void health_shouldReturnOk() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("AskBit.AI is running"));
    }
}

