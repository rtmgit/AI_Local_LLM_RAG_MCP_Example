package com.ai.llm.adapter.service;


import com.ai.llm.adapter.model.mcp.ToolExecuteResponse;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AgentService {

    private final McpClient mcpClient;
    private final OllamaService ollamaService;

    public AgentService(McpClient mcpClient, OllamaService ollamaService) {
        this.mcpClient = mcpClient;
        this.ollamaService = ollamaService;
    }

    @SuppressWarnings("unchecked")
    public AgentResult run(String userPrompt, String toolName, Object toolArguments) {

        // 1) Decide tool (if user didn't provide)
        String decidedTool = (toolName == null || toolName.isBlank())
                ? decideTool(userPrompt)
                : toolName;

        Map<String, Object> args = Map.of();
        if (toolArguments instanceof Map<?, ?> m) {
            args = (Map<String, Object>) m;
        }

        // If auto mode picked a tool but args are missing, we try basic extraction
        if (!decidedTool.isBlank() && args.isEmpty()) {
            args = buildArgsFromPrompt(decidedTool, userPrompt);
        }

        Object toolResult = null;
        String toolUsed = null;

        // 2) Call MCP tool (if any)
        if (decidedTool != null && !decidedTool.isBlank()) {
            ToolExecuteResponse toolResp = mcpClient.execute(decidedTool, args);
            toolUsed = decidedTool;

            if (toolResp == null) {
                toolResult = Map.of("error", "No response from MCP tool service");
            } else if ("ERROR".equalsIgnoreCase(toolResp.status())) {
                toolResult = Map.of("error", toolResp.errorMessage());
            } else {
                toolResult = toolResp.result();
            }
        }

        // 3) Ask LLM to produce final response using tool result (grounded)
        String finalPrompt = buildFinalPrompt(userPrompt, toolUsed, toolResult);

        String llmAnswer = ollamaService.chat(finalPrompt, null, null);

        return new AgentResult(toolUsed, toolResult, llmAnswer);
    }

    private String decideTool(String prompt) {
        String p = prompt == null ? "" : prompt.toLowerCase();

        if (p.contains("compare")) return "compareEmployees";
        if (p.contains("ticket") || p.contains("servicenow")) return "queryServiceNowTickets";
        if (p.contains("team") && (p.contains("members") || p.contains("list"))) return "getTeamMembers";
        if (p.contains("employee") && p.contains("details")) return "getEmployeeById";

        // no tool required
        return "";
    }

    private Map<String, Object> buildArgsFromPrompt(String tool, String prompt) {
        // Basic “good enough” parsing for POC
        // You can refine later.
        if (tool.equals("compareEmployees")) {
            // expects E001 and E005 in the prompt
            String e1 = extractFirstEmployeeId(prompt);
            String e2 = extractSecondEmployeeId(prompt, e1);
            if (e1 != null && e2 != null) {
                return Map.of("employeeId1", e1, "employeeId2", e2);
            }
        }

        if (tool.equals("getEmployeeById")) {
            String e = extractFirstEmployeeId(prompt);
            if (e != null) return Map.of("employeeId", e);
        }

        if (tool.equals("getTeamMembers")) {
            // expects "Payments" or "Risk"
            String team = extractTeam(prompt);
            if (team != null) return Map.of("teamName", team);
        }

        if (tool.equals("queryServiceNowTickets")) {
            String status = extractTicketStatus(prompt);
            String group = extractTeam(prompt);
            // any of these can be null
            return Map.of(
                    "status", status == null ? "" : status,
                    "assignedGroup", group == null ? "" : group
            );
        }

        return Map.of();
    }

    private String buildFinalPrompt(String userPrompt, String toolUsed, Object toolResult) {
        if (toolUsed == null || toolUsed.isBlank()) {
            return userPrompt;
        }

        return """
               You are an assistant. Use the TOOL_RESULT to answer the user's question.
               If the TOOL_RESULT has an error, explain the error and suggest what to try.
               
               USER_QUESTION:
               %s
               
               TOOL_USED:
               %s
               
               TOOL_RESULT (JSON):
               %s
               
               Answer in simple English. Be concise.
               """.formatted(userPrompt, toolUsed, String.valueOf(toolResult));
    }

    // ---- simple extractors ----

    private String extractFirstEmployeeId(String prompt) {
        return extractEmployeeIds(prompt).stream().findFirst().orElse(null);
    }

    private String extractSecondEmployeeId(String prompt, String first) {
        return extractEmployeeIds(prompt).stream().filter(id -> !id.equals(first)).findFirst().orElse(null);
    }

    private java.util.List<String> extractEmployeeIds(String prompt) {
        if (prompt == null) return java.util.List.of();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\bE\\d{3}\\b").matcher(prompt.toUpperCase());
        java.util.List<String> ids = new java.util.ArrayList<>();
        while (m.find()) ids.add(m.group());
        return ids;
    }

    private String extractTeam(String prompt) {
        if (prompt == null) return null;
        String p = prompt.toLowerCase();
        if (p.contains("payments")) return "Payments";
        if (p.contains("risk")) return "Risk";
        return null;
    }

    private String extractTicketStatus(String prompt) {
        if (prompt == null) return null;
        String p = prompt.toLowerCase();
        if (p.contains("open")) return "OPEN";
        if (p.contains("closed")) return "CLOSED";
        if (p.contains("in progress") || p.contains("in_progress")) return "IN_PROGRESS";
        return null;
    }

    public record AgentResult(String toolUsed, Object toolResult, String llmResponse) {}
}

