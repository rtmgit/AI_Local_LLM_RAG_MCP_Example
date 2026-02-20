package com.ai.llm.adapter.controller;

import com.ai.llm.adapter.model.dto.AgentRequest;
import com.ai.llm.adapter.model.dto.AgentResponse;
import com.ai.llm.adapter.service.AgentService;
import com.ai.llm.adapter.service.AgentServiceWithLLMToolSelection;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/ai")
public class AgentController {

    private final AgentService agentService;

    private final AgentServiceWithLLMToolSelection agentServiceWithLLMToolSelection;
    public AgentController(AgentService agentService, AgentServiceWithLLMToolSelection agentServiceWithLLMToolSelection) {
        this.agentService = agentService;
        this.agentServiceWithLLMToolSelection = agentServiceWithLLMToolSelection;
    }

    @PostMapping("/agent")
    public AgentResponse agent(@Valid @RequestBody AgentRequest req) {

        var result = agentService.run(req.prompt(), req.toolName(), req.toolArguments());

        return new AgentResponse(
                req.prompt(),
                result.toolUsed(),
                result.toolResult(),
                result.llmResponse()
        );
    }

    @PostMapping("/agent-auto")
    public Map<String, Object> agentAuto(@RequestBody Map<String, Object> body) {
        String prompt = String.valueOf(body.getOrDefault("prompt", "")).trim();
        if (prompt.isBlank()) {
            return Map.of("error", "prompt is required");
        }

        var result = agentServiceWithLLMToolSelection.answerWithTools(prompt);

        return Map.of(
                "prompt", prompt,
                "toolUsed", result.toolUsed(),
                "toolArguments", result.toolArguments(),
                "toolResult", result.toolResult(),
                "finalAnswer", result.finalAnswer()
        );
    }
}

