package com.ai.llm.adapter.model.mcp;

import java.util.Map;

public record ToolSelection(
        String toolName,
        Map<String, Object> arguments
) {
    public static ToolSelection none() {
        return new ToolSelection("", Map.of());
    }
}
