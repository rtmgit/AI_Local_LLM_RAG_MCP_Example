package com.ai.llm.adapter.service;

import com.ai.llm.adapter.model.mcp.ToolDefinition;
import com.ai.llm.adapter.model.mcp.ToolSelection;
import com.ai.llm.adapter.util.JsonExtractors;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class OllamaToolSelector {

    private final OllamaService ollamaService; // your existing service that calls /api/generate
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OllamaToolSelector(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    public ToolSelection selectTool(String userQuestion, List<ToolDefinition> tools) {
        String toolsText = tools.stream()
                .map(t -> """
                        - name: %s
                          description: %s
                          inputSchema: %s
                        """.formatted(t.name(), t.description(), t.inputSchema()))
                .collect(Collectors.joining("\n"));

        String prompt = """
                You are a TOOL SELECTOR.

                Choose the best tool from TOOL_LIST to answer USER_QUESTION.
                Return ONLY a valid JSON object (no markdown, no explanation).

                JSON format:
                {"toolName":"<tool name or empty string>","arguments":{...}}

                Rules:
                - If no tool is needed, return: {"toolName":"","arguments":{}}
                - Use only tool names present in TOOL_LIST.
                - Arguments must match the tool inputSchema.
                - If IDs are mentioned (like E001), use them as-is.
                - Be strict JSON.

                TOOL_LIST:
                %s

                USER_QUESTION:
                %s
                """.formatted(toolsText, userQuestion);

        // Use temperature 0.0 for stable JSON/tool decisions
        String raw = ollamaService.chat(prompt, null, 0.0);

        String json = JsonExtractors.firstJsonObjectOrNull(raw);
        if (json == null) return ToolSelection.none();

        try {
            ToolSelection selection = objectMapper.readValue(json, ToolSelection.class);
            if (selection == null || selection.toolName() == null) return ToolSelection.none();
            return selection;
        } catch (Exception e) {
            return ToolSelection.none();
        }
    }
}
