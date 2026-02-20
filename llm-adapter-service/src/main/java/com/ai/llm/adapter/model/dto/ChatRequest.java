package com.ai.llm.adapter.model.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(
        @NotBlank String prompt,
        String model,
        Double temperature
) {}
