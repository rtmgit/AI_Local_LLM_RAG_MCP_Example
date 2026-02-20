package com.ai.llm.rag.dto;

public record SearchRequest(
        String query,
        Integer topK,
        String teamName,
        String projectId
) {}
