package tech.hanasaki.azusa.modules.knowledge.infrastructure.persistence.repository

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
import tech.hanasaki.azusa.common.adapter.out.persistence.dbQuery
import tech.hanasaki.azusa.common.domain.model.KnowledgeBaseId
import tech.hanasaki.azusa.common.domain.model.KnowledgeDocumentId
import tech.hanasaki.azusa.common.domain.model.KnowledgeFileId
import tech.hanasaki.azusa.modules.knowledge.domain.model.KnowledgeDocument
import tech.hanasaki.azusa.modules.knowledge.domain.repository.KnowledgeDocumentRepository
import tech.hanasaki.azusa.modules.knowledge.infrastructure.persistence.table.KnowledgeDocumentTable
import java.sql.Connection
import java.sql.Timestamp
import java.time.Instant

class ExposedKnowledgeDocumentRepository : KnowledgeDocumentRepository {

    override suspend fun findById(id: KnowledgeDocumentId): KnowledgeDocument? = dbQuery {
        KnowledgeDocumentTable.selectAll()
            .where { KnowledgeDocumentTable.id eq id.value }
            .map(::toDomain)
            .singleOrNull()
    }

    override suspend fun save(document: KnowledgeDocument): Unit = dbQuery {
        val embeddingValue = document.embedding?.let { arr ->
            "[${arr.joinToString(",")}]"
        }

        val sql = """
            INSERT INTO knowledge_documents (id, knowledge_base_id, file_id, content, metadata, embedding, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?::jsonb, ?::extensions.vector(1024), ?, ?)
            ON CONFLICT (id) DO UPDATE SET
                content = EXCLUDED.content,
                metadata = EXCLUDED.metadata,
                embedding = EXCLUDED.embedding,
                updated_at = EXCLUDED.updated_at
        """.trimIndent()

        val conn = TransactionManager.current().connection.connection as Connection
        conn.prepareStatement(sql).use { stmt ->
            stmt.setObject(1, document.id.value)
            stmt.setObject(2, document.knowledgeBaseId.value)
            stmt.setObject(3, document.fileId?.value)
            stmt.setString(4, document.content)
            stmt.setString(5, document.metadata.toString())
            stmt.setString(6, embeddingValue)
            stmt.setTimestamp(7, Timestamp.from(Instant.ofEpochSecond(document.createdAt.epochSeconds)))
            stmt.setTimestamp(8, Timestamp.from(Instant.ofEpochSecond(document.updatedAt.epochSeconds)))
            stmt.executeUpdate()
        }
    }

    override suspend fun saveAll(documents: List<KnowledgeDocument>): Unit = dbQuery {
        if (documents.isEmpty()) return@dbQuery

        val sql = """
            INSERT INTO knowledge_documents (id, knowledge_base_id, file_id, content, metadata, embedding, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?::jsonb, ?::extensions.vector(1024), ?, ?)
        """.trimIndent()

        val conn = TransactionManager.current().connection.connection as Connection
        conn.prepareStatement(sql).use { stmt ->
            documents.forEach { document ->
                val embeddingValue = document.embedding?.let { arr ->
                    "[${arr.joinToString(",")}]"
                }
                stmt.setObject(1, document.id.value)
                stmt.setObject(2, document.knowledgeBaseId.value)
                stmt.setObject(3, document.fileId?.value)
                stmt.setString(4, document.content)
                stmt.setString(5, document.metadata.toString())
                stmt.setString(6, embeddingValue)
                stmt.setTimestamp(7, Timestamp.from(Instant.ofEpochSecond(document.createdAt.epochSeconds)))
                stmt.setTimestamp(8, Timestamp.from(Instant.ofEpochSecond(document.updatedAt.epochSeconds)))
                stmt.addBatch()
            }
            stmt.executeBatch()
        }
    }

    override suspend fun deleteById(id: KnowledgeDocumentId): Unit = dbQuery {
        KnowledgeDocumentTable.deleteWhere { KnowledgeDocumentTable.id eq id.value }
    }

    override suspend fun findByKnowledgeBaseId(knowledgeBaseId: KnowledgeBaseId): List<KnowledgeDocument> = dbQuery {
        KnowledgeDocumentTable.selectAll()
            .where { KnowledgeDocumentTable.knowledgeBaseId eq knowledgeBaseId.value }
            .map(::toDomain)
    }

    override suspend fun findByFileId(fileId: KnowledgeFileId): List<KnowledgeDocument> = dbQuery {
        KnowledgeDocumentTable.selectAll()
            .where { KnowledgeDocumentTable.fileId eq fileId.value }
            .map(::toDomain)
    }

    override suspend fun deleteByKnowledgeBaseId(knowledgeBaseId: KnowledgeBaseId): Unit = dbQuery {
        KnowledgeDocumentTable.deleteWhere { KnowledgeDocumentTable.knowledgeBaseId eq knowledgeBaseId.value }
    }

    override suspend fun deleteByFileId(fileId: KnowledgeFileId): Unit = dbQuery {
        KnowledgeDocumentTable.deleteWhere { KnowledgeDocumentTable.fileId eq fileId.value }
    }

    override suspend fun countByKnowledgeBaseId(knowledgeBaseId: KnowledgeBaseId): Long = dbQuery {
        KnowledgeDocumentTable.selectAll()
            .where { KnowledgeDocumentTable.knowledgeBaseId eq knowledgeBaseId.value }
            .count()
    }

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
