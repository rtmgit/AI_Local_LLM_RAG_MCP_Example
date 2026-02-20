package com.ai.llm.adapter.controller;

import com.ai.llm.adapter.model.dto.ChatRequest;
import com.ai.llm.adapter.model.dto.ChatResponse;
import com.ai.llm.adapter.service.OllamaService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/ai")
public class LLMAdapterRestController {

    private final OllamaService ollamaService;

    public LLMAdapterRestController(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest req) {
        String answer = ollamaService.chat(req.prompt(), req.model(), req.temperature());
        String usedModel = (req.model() == null || req.model().isBlank()) ? "default" : req.model();

        return new ChatResponse(usedModel, req.prompt(), answer);
    }
}
