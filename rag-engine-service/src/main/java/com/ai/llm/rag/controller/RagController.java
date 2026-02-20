package com.ai.llm.rag.controller;

import com.ai.llm.rag.service.RagService;
import com.ai.llm.rag.dto.AnswerRequest;
import com.ai.llm.rag.dto.IngestRequest;
import com.ai.llm.rag.dto.SearchRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/rag")
public class RagController {

    private final RagService rag;

    public RagController(RagService rag) {
        this.rag = rag;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of("status", "UP", "service", "rag-engine-service");
    }

    @PostMapping("/ingest")
    public Map<String, Object> ingest(@Valid @RequestBody IngestRequest req) {
        long docId = rag.ingest(req.sourceType(), req.sourceId(), req.title(), req.teamName(), req.projectId(), req.content());
        return Map.of("status", "OK", "documentId", docId);
    }

    @PostMapping("/search")
    public Object search(@RequestBody SearchRequest req) {
        return rag.search(req.query(), req.topK(), req.teamName(), req.projectId());
    }

    @PostMapping("/answer")
    public Object answer(@RequestBody AnswerRequest req) {
        return rag.answer(req.question(), req.topK(), req.teamName(), req.projectId());
    }
}

