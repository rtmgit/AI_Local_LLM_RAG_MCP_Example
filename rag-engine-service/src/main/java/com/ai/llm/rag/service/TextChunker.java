package com.ai.llm.rag.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TextChunker {

    private final int sizeChars;
    private final int overlapChars;

    public TextChunker(
            @Value("${rag.chunk.sizeChars}") int sizeChars,
            @Value("${rag.chunk.overlapChars}") int overlapChars
    ) {
        this.sizeChars = sizeChars;
        this.overlapChars = overlapChars;
    }

    public List<String> chunk(String text) {
        if (text == null) return List.of();
        String t = text.trim();
        if (t.isEmpty()) return List.of();

        List<String> chunks = new ArrayList<>();
        int start = 0;

        while (start < t.length()) {
            int end = Math.min(start + sizeChars, t.length());
            String part = t.substring(start, end).trim();
            if (!part.isEmpty()) chunks.add(part);

            if (end == t.length()) break;
            start = Math.max(0, end - overlapChars);
        }
        return chunks;
    }
}

