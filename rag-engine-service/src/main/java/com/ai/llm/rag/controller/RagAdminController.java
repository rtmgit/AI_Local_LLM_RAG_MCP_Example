package com.ai.llm.rag.controller;

import com.ai.llm.rag.service.RagIngestionJob;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/rag/admin")
public class RagAdminController {

    private final RagIngestionJob job;

    public RagAdminController(RagIngestionJob job) {
        this.job = job;
    }

    @PostMapping("/ingest-folder-once")
    public Map<String, Object> runOnce() {
        job.ingestFolderOnce();
        return Map.of("status", "OK");
    }
}
