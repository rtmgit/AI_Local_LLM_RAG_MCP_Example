package com.ai.llm.rag.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.*;
import java.util.stream.Stream;

@Component
public class RagIngestionJob {

    private final RagService ragService;

    public RagIngestionJob(RagService ragService) {
        this.ragService = ragService;
    }

    // every 10 minutes (change later)
    @Scheduled(cron = "0 */10 * * * *")
    public void run() {
        ingestFolderOnce();
    }

    public void ingestFolderOnce() {
        Path folder = Paths.get("/rag-data");
        if (!Files.exists(folder)) return;

        try (Stream<Path> files = Files.list(folder)) {
            files.filter(p -> p.toString().endsWith(".txt"))
                    .forEach(this::ingestFile);
        } catch (Exception ignored) {
        }
    }

    private void ingestFile(Path p) {
        try {
            String content = Files.readString(p);
            String filename = p.getFileName().toString();

            // For POC: derive team from filename if you want, or keep fixed
            String team = filename.toLowerCase().contains("payments") ? "Payments" : "General";

            ragService.ingest("FILE", filename, filename, team, null, content);
        } catch (Exception ignored) {
        }
    }
}

