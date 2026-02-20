package com.ai.llm.adapter.model.dto;

public record AgentResponse(
        String prompt,
        String toolUsed,
        Object toolResult,
        String llmResponse
) {}

