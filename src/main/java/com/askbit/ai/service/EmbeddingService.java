package com.askbit.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public List<Double> generateEmbedding(String text) {
        try {
            if (text == null || text.trim().isEmpty()) {
                log.warn("Attempted to generate embedding for empty text");
                return List.of();
            }

            EmbeddingResponse response = embeddingModel.embedForResponse(List.of(text));

            if (response != null && !response.getResults().isEmpty()) {
                float[] embedding = response.getResults().get(0).getOutput();
                return convertToDoubleList(embedding);
            }

            log.warn("Empty embedding response for text");
            return List.of();

        } catch (Exception e) {
            log.error("Error generating embedding: {}", e.getMessage(), e);
            return List.of();
        }
    }

    public List<List<Double>> generateEmbeddings(List<String> texts) {
        try {
            EmbeddingResponse response = embeddingModel.embedForResponse(texts);

            return response.getResults().stream()
                    .map(result -> convertToDoubleList(result.getOutput()))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error generating batch embeddings: {}", e.getMessage(), e);
            return List.of();
        }
    }

    public double cosineSimilarity(List<Double> vec1, List<Double> vec2) {
        if (vec1 == null || vec2 == null || vec1.size() != vec2.size()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += vec1.get(i) * vec1.get(i);
            norm2 += vec2.get(i) * vec2.get(i);
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    public List<Double> parseEmbeddingString(String embeddingStr) {
        if (embeddingStr == null || embeddingStr.trim().isEmpty()) {
            return List.of();
        }

        try {
            return Arrays.stream(embeddingStr.split(","))
                    .map(String::trim)
                    .map(Double::parseDouble)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error parsing embedding string: {}", e.getMessage());
            return List.of();
        }
    }

    private List<Double> convertToDoubleList(float[] array) {
        List<Double> result = new ArrayList<>(array.length);
        for (float value : array) {
            result.add((double) value);
        }
        return result;
    }
}

