package tech.hanasaki.azusa.modules.knowledge.adapter.out.persistence.table

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.json.jsonb
import kotlin.time.Clock


object KnowledgeDocumentTable : Table("knowledge_documents") {
    val id = uuid("id")
    val knowledgeBaseId = uuid("knowledge_base_id")
    val fileId = uuid("file_id").nullable()
    val content = text("content")
    val metadata = jsonb<JsonObject>("metadata", Json { prettyPrint = true })

    // embedding 字段使用原生 SQL 处理（pgvector 类型）
    val createdAt = timestamp("created_at").default(Clock.System.now())
    val updatedAt = timestamp("updated_at").default(Clock.System.now())

    override val primaryKey = PrimaryKey(id)
}
