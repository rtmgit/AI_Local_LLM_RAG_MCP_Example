package com.ai.llm.adapter.model.mcp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record ToolExecuteRequest(
        @NotBlank String toolName,
        @NotNull Map<String, Object> arguments
) {}
