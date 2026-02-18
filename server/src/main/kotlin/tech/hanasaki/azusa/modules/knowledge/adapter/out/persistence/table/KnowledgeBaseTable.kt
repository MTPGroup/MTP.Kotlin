package tech.hanasaki.azusa.modules.knowledge.adapter.out.persistence.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock


object KnowledgeBaseTable : Table("knowledge_bases") {
    val id = uuid("id")
    val name = varchar("name", 100)
    val description = text("description").nullable()
    val authorId = uuid("author_id")
    val isPublic = bool("is_public").default(false)
    val createdAt = timestamp("created_at").default(Clock.System.now())
    val updatedAt = timestamp("updated_at").default(Clock.System.now())

    override val primaryKey = PrimaryKey(id)
}
