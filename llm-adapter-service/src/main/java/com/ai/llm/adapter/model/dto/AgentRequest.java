package com.ai.llm.adapter.model.dto;

public record AgentRequest(
        String prompt,
        String toolName,          // optional
        Object toolArguments      // optional: pass JSON object as map
) {}

