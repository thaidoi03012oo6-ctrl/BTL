package com.recruitment.ai.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class EmbeddingHelperService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingHelperService.class);

    private final EmbeddingModel embeddingModel;

    public EmbeddingHelperService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public String embedToPgVectorString(String text) {
        if (text == null || text.isBlank()) {
            text = "empty";
        }
        Exception lastError = null;
        for (int i = 1; i <= 3; i++) {
            try {
                Embedding embedding = embeddingModel.embed(text).content();
                return Arrays.toString(embedding.vector());
            } catch (Exception e) {
                lastError = e;
                log.warn("Gemini Embedding failed (attempt {}/3): {}", i, e.getMessage());
                if (i < 3) {
                    try {
                        Thread.sleep(1200L * i);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }
        throw new RuntimeException("Gemini Embedding failed: " + (lastError != null ? lastError.getMessage() : "unknown"), lastError);
    }
}
