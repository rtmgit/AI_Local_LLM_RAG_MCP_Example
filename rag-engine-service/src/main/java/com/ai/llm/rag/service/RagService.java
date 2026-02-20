package com.ai.llm.rag.service;

import com.ai.llm.rag.repo.RagRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RagService {

    private final RagRepository repo;
    private final TextChunker chunker;
    private final OllamaClient ollama;

    private final int defaultTopK;

    public RagService(RagRepository repo, TextChunker chunker, OllamaClient ollama,
                      @Value("${rag.retrieval.topK}") int defaultTopK) {
        this.repo = repo;
        this.chunker = chunker;
        this.ollama = ollama;
        this.defaultTopK = defaultTopK;
    }

    public long ingest(String sourceType, String sourceId, String title, String teamName, String projectId, String content) {
        long docId = repo.upsertDocument(sourceType, sourceId, title, content, teamName, projectId);

        // If document unchanged, upsertDocument returned existing id and chunks already exist.
        // We can’t easily know if it was unchanged without extra query; for POC we re-chunk always after update.
        // Our upsert deletes chunks when changed, so inserts below are safe.
        List<String> chunks = chunker.chunk(content);

        for (int i = 0; i < chunks.size(); i++) {
            String chunkText = chunks.get(i);
            List<Double> embedding = ollama.embed(chunkText);
            repo.insertChunk(docId, i, chunkText, embedding);
        }

        return docId;
    }

    // Get the chunks from Vector DB to pass the response as context to next response
    public List<Map<String, Object>> search(String query, Integer topK, String teamName, String projectId) {
        int k = (topK == null || topK <= 0) ? defaultTopK : topK;
        List<Double> qEmb = ollama.embed(query);
        return repo.search(qEmb, k, teamName, projectId);
    }

    public Map<String, Object> answer(String question, Integer topK, String teamName, String projectId) {
        List<Map<String, Object>> hits = search(question, topK, teamName, projectId);

        StringBuilder context = new StringBuilder();
        for (int i = 0; i < hits.size(); i++) {
            Map<String, Object> h = hits.get(i);
            context.append("\n[CONTEXT ").append(i + 1).append("]\n");
            context.append(h.get("chunk_text")).append("\n");
        }

        String prompt = """
                You are a helpful assistant.
                Answer the question using ONLY the context below.
                If the context is not enough, say: "I don't have enough information in the documents."

                QUESTION:
                %s

                CONTEXT:
                %s

                Answer in simple English.
                """.formatted(question, context);

        String llm = ollama.chat(prompt);

        return Map.of(
                "question", question,
                "answer", llm,
                "sources", hits
        );
    }
}
