package com.example.mcptool.controller;

import com.example.mcptool.mcp.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/mcp")
public class McpController {

    private final ToolRegistry toolRegistry;
    private final ToolExecutionService toolExecutionService;

    public McpController(ToolRegistry toolRegistry, ToolExecutionService toolExecutionService) {
        this.toolRegistry = toolRegistry;
        this.toolExecutionService = toolExecutionService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "service", "mcp-tool-service");
    }

    @GetMapping("/tools")
    public Object tools() {
        return toolRegistry.tools();
    }

    @PostMapping("/execute")
    public ToolExecuteResponse execute(@Valid @RequestBody ToolExecuteRequest request) {
        try {
            Object result = toolExecutionService.execute(request.toolName(), request.arguments());
            return ToolExecuteResponse.ok(request.toolName(), result);
        } catch (Exception e) {
            return ToolExecuteResponse.error(request.toolName(), e.getMessage());
        }
    }
}
