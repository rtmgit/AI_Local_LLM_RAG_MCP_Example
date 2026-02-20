package com.ai.llm.rag.dto;

import jakarta.validation.constraints.NotBlank;

public record IngestRequest(
        @NotBlank String sourceType,
        @NotBlank String sourceId,
        String title,
        String teamName,
        String projectId,
        @NotBlank String content
) {}

