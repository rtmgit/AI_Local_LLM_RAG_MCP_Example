package com.ai.llm.adapter.model.dto;

public record ChatResponse(
        String model,
        String prompt,
        String response
) {}

