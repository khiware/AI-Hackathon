package com.askbit.ai.controller;

import com.askbit.ai.dto.AskRequest;
import com.askbit.ai.dto.AskResponse;
import com.askbit.ai.service.QuestionAnsweringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AskController {

    private final QuestionAnsweringService questionAnsweringService;

    @PostMapping("/ask")
    public ResponseEntity<AskResponse> ask(@Valid @RequestBody AskRequest request) {
        log.info("Processing question request");

        try {
            AskResponse response = questionAnsweringService.answerQuestion(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing question", e);

            AskResponse errorResponse = AskResponse.builder()
                    .answer("An error occurred while processing your question. Please try again.")
                    .confidence(0.0)
                    .cached(false)
                    .build();

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("AskBit.AI is running");
    }
}