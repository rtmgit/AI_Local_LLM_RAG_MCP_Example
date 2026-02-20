package com.ai.llm.rag.dto;

public record AnswerRequest(
        String question,
        Integer topK,
        String teamName,
        String projectId
) {}
