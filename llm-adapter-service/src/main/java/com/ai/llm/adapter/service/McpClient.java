package com.ai.llm.adapter.service;

import com.ai.llm.adapter.model.mcp.ToolDefinition;
import com.ai.llm.adapter.model.mcp.ToolExecuteRequest;
import com.ai.llm.adapter.model.mcp.ToolExecuteResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class McpClient {

    private final WebClient webClient;
    private final String baseUrl;

    public McpClient(WebClient webClient,
                     @Value("${mcp.base-url}") String baseUrl) {
        this.webClient = webClient;
        this.baseUrl = baseUrl;
    }

    public List<ToolDefinition> listTools() {
        return webClient.get()
                .uri(baseUrl + "/mcp/tools")
                .retrieve()
                .bodyToFlux(ToolDefinition.class)
                .collectList()
                .timeout(Duration.ofSeconds(20))
                .block();
    }

    public ToolExecuteResponse execute(String toolName, Map<String, Object> args) {
        return webClient.post()
                .uri(baseUrl + "/mcp/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ToolExecuteRequest(toolName, args))
                .retrieve()
                .bodyToMono(ToolExecuteResponse.class)
                .timeout(Duration.ofSeconds(20))
                .block();
    }
}

