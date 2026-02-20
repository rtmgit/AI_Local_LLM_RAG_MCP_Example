package com.ai.llm.adapter.model.mcp;

public record ToolExecuteResponse(
        String toolName,
        String status, // OK / ERROR
        Object result,
        String errorMessage
) {
    public static ToolExecuteResponse ok(String toolName, Object result) {
        return new ToolExecuteResponse(toolName, "OK", result, null);
    }

    public static ToolExecuteResponse error(String toolName, String errorMessage) {
        return new ToolExecuteResponse(toolName, "ERROR", null, errorMessage);
    }
}
