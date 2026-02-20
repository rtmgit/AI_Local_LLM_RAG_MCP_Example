package com.ai.llm.rag.repo;

import com.ai.llm.rag.service.PgVector;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

@Repository
public class RagRepository {

    private final JdbcTemplate jdbc;

    public RagRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long upsertDocument(String sourceType, String sourceId, String title, String content,
                               String teamName, String projectId) {

        String hash = sha256(content);

        // If exists and hash same => return existing id (skip re-ingest)
        List<Long> existing = jdbc.query("""
                SELECT id FROM rag_documents
                WHERE source_type=? AND source_id=? AND content_hash=?""",
                (rs, i) -> rs.getLong("id"),
                sourceType, sourceId, hash
        );
        if (!existing.isEmpty()) return existing.get(0);

        // If exists but changed => delete chunks and update document
        List<Long> docIds = jdbc.query("""
                SELECT id FROM rag_documents
                WHERE source_type=? AND source_id=?""",
                (rs, i) -> rs.getLong("id"),
                sourceType, sourceId
        );

        if (!docIds.isEmpty()) {
            long docId = docIds.get(0);
            jdbc.update("DELETE FROM rag_chunks WHERE document_id=?", docId);
            jdbc.update("""
                UPDATE rag_documents
                SET title=?, content=?, team_name=?, project_id=?, content_hash=?, updated_at=now()
                WHERE id=?""",
                    title, content, teamName, projectId, hash, docId
            );
            return docId;
        }

        // Insert new
        return jdbc.queryForObject("""
                INSERT INTO rag_documents(source_type, source_id, title, content, team_name, project_id, content_hash)
                VALUES(?,?,?,?,?,?,?)
                RETURNING id""",
                Long.class,
                sourceType, sourceId, title, content, teamName, projectId, hash
        );
    }

    public void insertChunk(long documentId, int chunkIndex, String chunkText, List<Double> embedding) {
        String vec = PgVector.toSqlVectorLiteral(embedding);
        jdbc.update("""
                INSERT INTO rag_chunks(document_id, chunk_index, chunk_text, embedding)
                VALUES(?,?,?,?::vector)
                """, documentId, chunkIndex, chunkText, vec);
    }

    public List<Map<String, Object>> search(List<Double> queryEmbedding, int topK, String teamName, String projectId) {
        String vec = PgVector.toSqlVectorLiteral(queryEmbedding);

        // Simple filtering on document metadata (team/project)
        String sql = """
            SELECT c.id as chunk_id, c.chunk_text, c.chunk_index,
                   d.source_type, d.source_id, d.title, d.team_name, d.project_id
            FROM rag_chunks c
            JOIN rag_documents d ON d.id = c.document_id
            WHERE (?::text IS NULL OR d.team_name = ?)
              AND (?::text IS NULL OR d.project_id = ?)
            ORDER BY c.embedding <-> ?::vector
            LIMIT ?
            """;

        return jdbc.queryForList(sql,
                teamName, teamName,
                projectId, projectId,
                vec,
                topK
        );
    }

    private static String sha256(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest((text == null ? "" : text).getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
