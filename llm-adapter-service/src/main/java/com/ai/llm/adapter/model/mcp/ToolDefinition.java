package com.ai.llm.adapter.model.mcp;

import java.util.Map;

public record ToolDefinition(
        String name,
        String description,
        Map<String, Object> inputSchema
) {}
