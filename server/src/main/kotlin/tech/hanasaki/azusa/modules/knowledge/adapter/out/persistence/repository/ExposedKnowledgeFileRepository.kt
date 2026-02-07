package tech.hanasaki.azusa.modules.knowledge.adapter.out.persistence.repository

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.modules.knowledge.adapter.out.persistence.table.KnowledgeFileTable
import tech.hanasaki.azusa.modules.knowledge.domain.model.FileStatus
import tech.hanasaki.azusa.modules.knowledge.domain.model.KnowledgeFile
import tech.hanasaki.azusa.modules.knowledge.domain.port.KnowledgeFileRepositoryPort
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeFileId

class ExposedKnowledgeFileRepository : KnowledgeFileRepositoryPort {

    override suspend fun findById(id: KnowledgeFileId): KnowledgeFile? =
        KnowledgeFileTable.selectAll()
            .where { KnowledgeFileTable.id eq id.value }
            .map(::toDomain)
            .singleOrNull()

    override suspend fun save(file: KnowledgeFile) {
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

    override suspend fun deleteById(id: KnowledgeFileId) {
        KnowledgeFileTable.deleteWhere { KnowledgeFileTable.id eq id.value }
    }

    override suspend fun findByKnowledgeBaseId(knowledgeBaseId: KnowledgeBaseId): List<KnowledgeFile> =
        KnowledgeFileTable.selectAll()
            .where { KnowledgeFileTable.knowledgeBaseId eq knowledgeBaseId.value }
            .orderBy(KnowledgeFileTable.createdAt, SortOrder.DESC)
            .map(::toDomain)

    override suspend fun findByStatus(status: FileStatus, limit: Int): List<KnowledgeFile> =
        KnowledgeFileTable.selectAll()
            .where { KnowledgeFileTable.status eq status }
            .orderBy(KnowledgeFileTable.createdAt, SortOrder.ASC)
            .limit(limit)
            .map(::toDomain)

    override suspend fun deleteByKnowledgeBaseId(knowledgeBaseId: KnowledgeBaseId) {
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
