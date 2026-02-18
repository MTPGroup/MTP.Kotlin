package tech.hanasaki.azusa.modules.knowledge.adapter.out.persistence.repository

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import tech.hanasaki.azusa.modules.knowledge.adapter.out.persistence.table.KnowledgeDocumentTable
import tech.hanasaki.azusa.modules.knowledge.domain.model.KnowledgeDocument
import tech.hanasaki.azusa.modules.knowledge.domain.port.KnowledgeDocumentRepositoryPort
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeDocumentId
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeFileId
import java.sql.Connection
import java.sql.Timestamp
import java.time.Instant
import kotlin.uuid.toJavaUuid

class ExposedKnowledgeDocumentRepository : KnowledgeDocumentRepositoryPort {

    override suspend fun findById(id: KnowledgeDocumentId): KnowledgeDocument? =
        KnowledgeDocumentTable.selectAll()
            .where { KnowledgeDocumentTable.id eq id.value }
            .map(::toDomain)
            .singleOrNull()

    override suspend fun save(document: KnowledgeDocument) {
        val hasEmbedding = document.embedding != null && document.embedding.isNotEmpty()
        val embeddingValue = document.embedding?.takeIf { it.isNotEmpty() }?.let { arr ->
            "[${arr.joinToString(",")}]"
        }

        val sql = if (hasEmbedding) {
            """
                INSERT INTO knowledge_documents (id, knowledge_base_id, file_id, content, metadata, embedding, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?::jsonb, ?::extensions.vector(1024), ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    content = EXCLUDED.content,
                    metadata = EXCLUDED.metadata,
                    embedding = EXCLUDED.embedding,
                    updated_at = EXCLUDED.updated_at
            """.trimIndent()
        } else {
            """
                INSERT INTO knowledge_documents (id, knowledge_base_id, file_id, content, metadata, embedding, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?::jsonb, NULL, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    content = EXCLUDED.content,
                    metadata = EXCLUDED.metadata,
                    embedding = NULL,
                    updated_at = EXCLUDED.updated_at
            """.trimIndent()
        }

        val conn = TransactionManager.current().connection.connection as Connection
        conn.prepareStatement(sql).use { stmt ->
            stmt.setObject(1, document.id.value.toJavaUuid())
            stmt.setObject(2, document.knowledgeBaseId.value.toJavaUuid())
            stmt.setObject(3, document.fileId?.value?.toJavaUuid())
            stmt.setString(4, document.content)
            stmt.setString(5, document.metadata.toString())
            if (hasEmbedding) {
                stmt.setString(6, embeddingValue)
                stmt.setTimestamp(7, Timestamp.from(Instant.ofEpochSecond(document.createdAt.epochSeconds)))
                stmt.setTimestamp(8, Timestamp.from(Instant.ofEpochSecond(document.updatedAt.epochSeconds)))
            } else {
                stmt.setTimestamp(6, Timestamp.from(Instant.ofEpochSecond(document.createdAt.epochSeconds)))
                stmt.setTimestamp(7, Timestamp.from(Instant.ofEpochSecond(document.updatedAt.epochSeconds)))
            }
            stmt.executeUpdate()
        }
    }

    override suspend fun saveAll(documents: List<KnowledgeDocument>) {
        if (documents.isEmpty()) return

        val conn = TransactionManager.current().connection.connection as Connection

        documents.forEach { document ->
            val hasEmbedding = document.embedding != null && document.embedding.isNotEmpty()
            val embeddingValue = document.embedding?.takeIf { it.isNotEmpty() }?.let { arr ->
                "[${arr.joinToString(",")}]"
            }

            val sql = if (hasEmbedding) {
                """
                    INSERT INTO knowledge_documents (id, knowledge_base_id, file_id, content, metadata, embedding, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?::jsonb, ?::extensions.vector(1024), ?, ?)
                """.trimIndent()
            } else {
                """
                    INSERT INTO knowledge_documents (id, knowledge_base_id, file_id, content, metadata, embedding, created_at, updated_at)
                    VALUES (?, ?, ?, ?, ?::jsonb, NULL, ?, ?)
                """.trimIndent()
            }

            conn.prepareStatement(sql).use { stmt ->
                stmt.setObject(1, document.id.value.toJavaUuid())
                stmt.setObject(2, document.knowledgeBaseId.value.toJavaUuid())
                stmt.setObject(3, document.fileId?.value?.toJavaUuid())
                stmt.setString(4, document.content)
                stmt.setString(5, document.metadata.toString())
                if (hasEmbedding) {
                    stmt.setString(6, embeddingValue)
                    stmt.setTimestamp(7, Timestamp.from(Instant.ofEpochSecond(document.createdAt.epochSeconds)))
                    stmt.setTimestamp(8, Timestamp.from(Instant.ofEpochSecond(document.updatedAt.epochSeconds)))
                } else {
                    stmt.setTimestamp(6, Timestamp.from(Instant.ofEpochSecond(document.createdAt.epochSeconds)))
                    stmt.setTimestamp(7, Timestamp.from(Instant.ofEpochSecond(document.updatedAt.epochSeconds)))
                }
                stmt.executeUpdate()
            }
        }
    }

    override suspend fun deleteById(id: KnowledgeDocumentId) {
        KnowledgeDocumentTable.deleteWhere { KnowledgeDocumentTable.id eq id.value }
    }

    override suspend fun findByKnowledgeBaseId(knowledgeBaseId: KnowledgeBaseId): List<KnowledgeDocument> =
        KnowledgeDocumentTable.selectAll()
            .where { KnowledgeDocumentTable.knowledgeBaseId eq knowledgeBaseId.value }
            .map(::toDomain)

    override suspend fun findByFileId(fileId: KnowledgeFileId): List<KnowledgeDocument> =
        KnowledgeDocumentTable.selectAll()
            .where { KnowledgeDocumentTable.fileId eq fileId.value }
            .map(::toDomain)

    override suspend fun deleteByKnowledgeBaseId(knowledgeBaseId: KnowledgeBaseId) {
        KnowledgeDocumentTable.deleteWhere { KnowledgeDocumentTable.knowledgeBaseId eq knowledgeBaseId.value }
    }

    override suspend fun deleteByFileId(fileId: KnowledgeFileId) {
        KnowledgeDocumentTable.deleteWhere { KnowledgeDocumentTable.fileId eq fileId.value }
    }

    override suspend fun countByKnowledgeBaseId(knowledgeBaseId: KnowledgeBaseId): Long =
        KnowledgeDocumentTable.selectAll()
            .where { KnowledgeDocumentTable.knowledgeBaseId eq knowledgeBaseId.value }
            .count()

    private fun toDomain(row: ResultRow): KnowledgeDocument = KnowledgeDocument.reconstitute(
        id = KnowledgeDocumentId(row[KnowledgeDocumentTable.id]),
        knowledgeBaseId = KnowledgeBaseId(row[KnowledgeDocumentTable.knowledgeBaseId]),
        fileId = row[KnowledgeDocumentTable.fileId]?.let { KnowledgeFileId(it) },
        content = row[KnowledgeDocumentTable.content],
        metadata = row[KnowledgeDocumentTable.metadata],
        embedding = null, // embedding 需要通过原生 SQL 查询
        createdAt = row[KnowledgeDocumentTable.createdAt],
        updatedAt = row[KnowledgeDocumentTable.updatedAt],
    )
}
