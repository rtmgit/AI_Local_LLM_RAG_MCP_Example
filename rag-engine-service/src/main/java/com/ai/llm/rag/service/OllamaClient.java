package com.ai.llm.rag.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class OllamaClient {

    private final WebClient webClient;
    private final String baseUrl;
    private final String chatModel;
    private final String embedModel;

    public OllamaClient(
            WebClient webClient,
            @Value("${ollama.base-url}") String baseUrl,
            @Value("${ollama.chat-model}") String chatModel,
            @Value("${ollama.embed-model}") String embedModel
    ) {
        this.webClient = webClient;
        this.baseUrl = baseUrl;
        this.chatModel = chatModel;
        this.embedModel = embedModel;
    }

    public List<Double> embed(String text) {
        Map<String, Object> body = Map.of(
                "model", embedModel,
                "prompt", text
        );

        EmbeddingResponse resp = webClient.post()
                .uri(baseUrl + "/api/embeddings")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(EmbeddingResponse.class)
                .timeout(Duration.ofSeconds(60))
                .block();

        if (resp == null || resp.embedding == null) throw new IllegalStateException("No embedding returned from Ollama");
        return resp.embedding;
    }

    public String chat(String prompt) {
        Map<String, Object> body = Map.of(
                "model", chatModel,
                "prompt", prompt,
                "stream", false,
                "options", Map.of("temperature", 0.2)
        );

        GenerateResponse resp = webClient.post()
                .uri(baseUrl + "/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(GenerateResponse.class)
                .timeout(Duration.ofSeconds(180))
                .block();

        return resp == null ? "" : (resp.response == null ? "" : resp.response);
    }

    public static class EmbeddingResponse {
        public List<Double> embedding;
    }
    public static class GenerateResponse {
        public String response;
    }
}
