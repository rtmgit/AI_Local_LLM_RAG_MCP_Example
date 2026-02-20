package com.ai.llm.adapter.util;

/**
 * Add JSON helper to safely extract JSON from LLM output
 *
 * llama3 sometimes adds extra text. This helper extracts the first JSON object
 */
public final class JsonExtractors {
    private JsonExtractors() {}

    /** Extract the first top-level JSON object {...} from text. Returns null if not found. */
    public static String firstJsonObjectOrNull(String text) {
        if (text == null) return null;

        int start = text.indexOf('{');
        if (start < 0) return null;

        int depth = 0;
        boolean inString = false;
        boolean escape = false;

        for (int i = start; i < text.length(); i++) {
            char c = text.charAt(i);

            if (escape) {
                escape = false;
                continue;
            }
            if (c == '\\' && inString) {
                escape = true;
                continue;
            }
            if (c == '"') {
                inString = !inString;
                continue;
            }
            if (inString) continue;

            if (c == '{') depth++;
            if (c == '}') depth--;

            if (depth == 0) {
                return text.substring(start, i + 1);
            }
        }
        return null;
    }
}
