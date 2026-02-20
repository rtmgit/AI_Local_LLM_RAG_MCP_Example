package com.ai.llm.adapter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Service
public class OllamaService {

    private final WebClient ollamaWebClient;
    private final String baseUrl;
    private final String defaultModel;
    private final double defaultTemperature;

    public OllamaService(
            WebClient ollamaWebClient,
            @Value("${ollama.base-url}") String baseUrl,
            @Value("${ollama.model}") String defaultModel,
            @Value("${ollama.temperature}") double defaultTemperature
    ) {
        this.ollamaWebClient = ollamaWebClient;
        this.baseUrl = baseUrl;
        this.defaultModel = defaultModel;
        this.defaultTemperature = defaultTemperature;
    }

    public String chat(String prompt, String modelOverride, Double tempOverride) {

        String model = (modelOverride == null || modelOverride.isBlank()) ? defaultModel : modelOverride;
        double temperature = (tempOverride == null) ? defaultTemperature : tempOverride;

        // Ollama API expects JSON like:
        // { "model": "llama3", "prompt": "...", "stream": false, "options": { "temperature": 0.2 } }
        Map<String, Object> body = Map.of(
                "model", model,
                "prompt", prompt,
                "stream", false,
                "options", Map.of("temperature", temperature)
        );

        // We call /api/generate and read the "response" field
        return ollamaWebClient.post()
                .uri(baseUrl + "/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(OllamaGenerateResponse.class)
                .timeout(Duration.ofSeconds(320))
                .map(OllamaGenerateResponse::response)
                .block();
    }

    // Minimal mapping of Ollama response JSON
    private record OllamaGenerateResponse(String response) {}
}

