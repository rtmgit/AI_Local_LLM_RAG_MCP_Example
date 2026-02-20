package com.ai.llm.adapter.service;


import com.ai.llm.adapter.model.mcp.ToolDefinition;
import com.ai.llm.adapter.model.mcp.ToolExecuteResponse;
import com.ai.llm.adapter.model.mcp.ToolSelection;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AgentServiceWithLLMToolSelection {

    private final McpClient mcpClient;
    private final OllamaToolSelector toolSelector;
    private final OllamaService ollamaService;

    public AgentServiceWithLLMToolSelection(McpClient mcpClient, OllamaToolSelector toolSelector, OllamaService ollamaService) {
        this.mcpClient = mcpClient;
        this.toolSelector = toolSelector;
        this.ollamaService = ollamaService;
    }

    public AgentResult answerWithTools(String userQuestion) {

        // 1) Fetch tool list from MCP server
        List<ToolDefinition> tools = mcpClient.listTools();

        // 2) Ask llama3 to select tool + args (JSON)
        ToolSelection selection = toolSelector.selectTool(userQuestion, tools);

        // 3) If no tool required, just ask llama3 normally
        if (selection.toolName() == null || selection.toolName().isBlank()) {
            String llm = ollamaService.chat(userQuestion, null, null);
            return new AgentResult("", Map.of(), null, llm);
        }

        // 4) Execute tool via MCP
        ToolExecuteResponse toolResp = mcpClient.execute(selection.toolName(), selection.arguments());

        Object toolResult;
        if (toolResp == null) {
            toolResult = Map.of("error", "No response from MCP tool service");
        } else if ("ERROR".equalsIgnoreCase(toolResp.status())) {
            toolResult = Map.of("error", toolResp.errorMessage());
        } else {
            toolResult = toolResp.result();
        }

        // 5) Ask llama3 again, grounded on tool result
        String finalPrompt = """
                You are a helpful assistant. Use TOOL_RESULT to answer USER_QUESTION.
                Do not invent data. If TOOL_RESULT has an error, explain it and suggest what to do next.
                Answer in simple English.

                USER_QUESTION:
                %s

                TOOL_USED:
                %s

                TOOL_RESULT (JSON):
                %s
                """.formatted(userQuestion, selection.toolName(), String.valueOf(toolResult));

        String finalAnswer = ollamaService.chat(finalPrompt, null, 0.2);

        return new AgentResult(selection.toolName(), selection.arguments(), toolResult, finalAnswer);
    }

    public record AgentResult(String toolUsed, Map<String, Object> toolArguments, Object toolResult, String finalAnswer) {}
}
