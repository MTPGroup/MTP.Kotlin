package tech.hanasaki.azusa.modules.knowledge.domain.model

import kotlinx.serialization.json.JsonObject
import tech.hanasaki.azusa.common.kernel.model.KnowledgeBaseId
import tech.hanasaki.azusa.common.kernel.model.KnowledgeDocumentId
import tech.hanasaki.azusa.common.kernel.model.KnowledgeFileId
import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * 知识文档实体（向量化后的分块）
 */
data class KnowledgeDocument(
    val id: KnowledgeDocumentId,
    val knowledgeBaseId: KnowledgeBaseId,
    val fileId: KnowledgeFileId?,
    val content: String,
    val metadata: JsonObject,
    val embedding: FloatArray?,
    val createdAt: Instant,
    var updatedAt: Instant,
) {
    companion object {
        /**
         * 创建新文档
         */
        fun create(
            knowledgeBaseId: KnowledgeBaseId,
            fileId: KnowledgeFileId?,
            content: String,
            metadata: JsonObject,
            embedding: FloatArray?,
        ): KnowledgeDocument {
            val now = Clock.System.now()
            return KnowledgeDocument(
                id = KnowledgeDocumentId(UUID.randomUUID()),
                knowledgeBaseId = knowledgeBaseId,
                fileId = fileId,
                content = content,
                metadata = metadata,
                embedding = embedding,
                createdAt = now,
                updatedAt = now,
            )
        }

        /**
         * 从持久化层重建
         */
        fun reconstitute(
            id: KnowledgeDocumentId,
            knowledgeBaseId: KnowledgeBaseId,
            fileId: KnowledgeFileId?,
            content: String,
            metadata: JsonObject,
            embedding: FloatArray?,
            createdAt: Instant,
            updatedAt: Instant,
        ): KnowledgeDocument = KnowledgeDocument(
            id = id,
            knowledgeBaseId = knowledgeBaseId,
            fileId = fileId,
            content = content,
            metadata = metadata,
            embedding = embedding,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KnowledgeDocument

        if (id != other.id) return false
        if (knowledgeBaseId != other.knowledgeBaseId) return false
        if (fileId != other.fileId) return false
        if (content != other.content) return false
        if (embedding != null) {
            if (other.embedding == null) return false
            if (!embedding.contentEquals(other.embedding)) return false
        } else if (other.embedding != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + knowledgeBaseId.hashCode()
        result = 31 * result + (fileId?.hashCode() ?: 0)
        result = 31 * result + content.hashCode()
        result = 31 * result + (embedding?.contentHashCode() ?: 0)
        return result
    }
}
