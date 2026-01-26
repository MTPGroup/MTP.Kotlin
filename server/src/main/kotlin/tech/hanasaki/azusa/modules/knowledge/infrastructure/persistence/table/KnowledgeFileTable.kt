package tech.hanasaki.azusa.modules.knowledge.infrastructure.persistence.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp
import tech.hanasaki.azusa.modules.knowledge.domain.model.FileStatus
import kotlin.time.Clock

object KnowledgeFileTable : Table("knowledge_files") {
    val id = uuid("id")
    val knowledgeBaseId = uuid("knowledge_base_id")
    val filePath = text("file_path")
    val fileName = text("file_name")
    val fileSize = integer("file_size").nullable()
    val fileType = text("file_type").nullable()
    val status = enumerationByName<FileStatus>("status", 20)
    val errorMessage = text("error_message").nullable()
    val createdAt = timestamp("created_at").default(Clock.System.now())
    val updatedAt = timestamp("updated_at").default(Clock.System.now())

    override val primaryKey = PrimaryKey(id)
}
