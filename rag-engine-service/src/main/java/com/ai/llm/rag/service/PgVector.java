package com.ai.llm.rag.service;

import java.util.List;
import java.util.StringJoiner;

public final class PgVector {
    private PgVector() {}

    public static String toSqlVectorLiteral(List<Double> embedding) {
        // Returns like: [0.1,0.2,...]
        StringJoiner j = new StringJoiner(",", "[", "]");
        for (Double d : embedding) {
            j.add(d == null ? "0" : d.toString());
        }
        return j.toString();
    }
}
