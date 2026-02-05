package tech.hanasaki.azusa.modules.knowledge.infrastructure.persistence.repository

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.common.adapter.out.persistence.dbQuery
import tech.hanasaki.azusa.common.domain.model.KnowledgeBaseId
import tech.hanasaki.azusa.common.domain.model.KnowledgeFileId
import tech.hanasaki.azusa.modules.knowledge.domain.model.FileStatus
import tech.hanasaki.azusa.modules.knowledge.domain.model.KnowledgeFile
import tech.hanasaki.azusa.modules.knowledge.domain.repository.KnowledgeFileRepository
import tech.hanasaki.azusa.modules.knowledge.infrastructure.persistence.table.KnowledgeFileTable

class ExposedKnowledgeFileRepository : KnowledgeFileRepository {

    override suspend fun findById(id: KnowledgeFileId): KnowledgeFile? = dbQuery {
        KnowledgeFileTable.selectAll()
            .where { KnowledgeFileTable.id eq id.value }
            .map(::toDomain)
            .singleOrNull()
    }

    override suspend fun save(file: KnowledgeFile): Unit = dbQuery {
        val updatedRows = KnowledgeFileTable.update({ KnowledgeFileTable.id eq file.id.value }) {
            it[status] = file.status
            it[errorMessage] = file.errorMessage
            it[updatedAt] = file.updatedAt
        }
        if (updatedRows == 0) {
            KnowledgeFileTable.insert {
                it[id] = file.id.value
                it[knowledgeBaseId] = file.knowledgeBaseId.value
                it[filePath] = file.filePath
                it[fileName] = file.fileName
                it[fileSize] = file.fileSize?.toInt()
                it[fileType] = file.fileType
                it[status] = file.status
                it[errorMessage] = file.errorMessage
                it[createdAt] = file.createdAt
                it[updatedAt] = file.updatedAt
            }
        }
    }

    override suspend fun deleteById(id: KnowledgeFileId): Unit = dbQuery {
        KnowledgeFileTable.deleteWhere { KnowledgeFileTable.id eq id.value }
    }

    override suspend fun findByKnowledgeBaseId(knowledgeBaseId: KnowledgeBaseId): List<KnowledgeFile> = dbQuery {
        KnowledgeFileTable.selectAll()
            .where { KnowledgeFileTable.knowledgeBaseId eq knowledgeBaseId.value }
            .orderBy(KnowledgeFileTable.createdAt, SortOrder.DESC)
            .map(::toDomain)
    }

    override suspend fun findByStatus(status: FileStatus, limit: Int): List<KnowledgeFile> = dbQuery {
        KnowledgeFileTable.selectAll()
            .where { KnowledgeFileTable.status eq status }
            .orderBy(KnowledgeFileTable.createdAt, SortOrder.ASC)
            .limit(limit)
            .map(::toDomain)
    }

    override suspend fun deleteByKnowledgeBaseId(knowledgeBaseId: KnowledgeBaseId): Unit = dbQuery {
        KnowledgeFileTable.deleteWhere { KnowledgeFileTable.knowledgeBaseId eq knowledgeBaseId.value }
    }

    private fun toDomain(row: ResultRow): KnowledgeFile = KnowledgeFile.reconstitute(
        id = KnowledgeFileId(row[KnowledgeFileTable.id]),
        knowledgeBaseId = KnowledgeBaseId(row[KnowledgeFileTable.knowledgeBaseId]),
        filePath = row[KnowledgeFileTable.filePath],
        fileName = row[KnowledgeFileTable.fileName],
        fileSize = row[KnowledgeFileTable.fileSize]?.toLong(),
        fileType = row[KnowledgeFileTable.fileType],
        status = row[KnowledgeFileTable.status],
        errorMessage = row[KnowledgeFileTable.errorMessage],
        createdAt = row[KnowledgeFileTable.createdAt],
        updatedAt = row[KnowledgeFileTable.updatedAt],
    )
}
