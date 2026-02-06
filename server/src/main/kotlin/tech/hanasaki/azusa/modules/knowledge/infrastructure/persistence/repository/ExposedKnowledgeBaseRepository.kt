package tech.hanasaki.azusa.modules.knowledge.infrastructure.persistence.repository

import org.jetbrains.exposed.v1.core.*
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.modules.knowledge.domain.model.KnowledgeBase
import tech.hanasaki.azusa.modules.knowledge.domain.repository.KnowledgeBaseRepository
import tech.hanasaki.azusa.modules.knowledge.infrastructure.persistence.table.KnowledgeBaseTable

class ExposedKnowledgeBaseRepository : KnowledgeBaseRepository {

    override suspend fun findById(id: KnowledgeBaseId): KnowledgeBase? =
        KnowledgeBaseTable.selectAll()
            .where { KnowledgeBaseTable.id eq id.value }
            .map(::toDomain)
            .singleOrNull()

    override suspend fun save(knowledgeBase: KnowledgeBase) {
        val updatedRows = KnowledgeBaseTable.update({ KnowledgeBaseTable.id eq knowledgeBase.id.value }) {
            it[name] = knowledgeBase.name
            it[description] = knowledgeBase.description
            it[isPublic] = knowledgeBase.isPublic
            it[updatedAt] = knowledgeBase.updatedAt
        }
        if (updatedRows == 0) {
            KnowledgeBaseTable.insert {
                it[id] = knowledgeBase.id.value
                it[name] = knowledgeBase.name
                it[description] = knowledgeBase.description
                it[authorId] = knowledgeBase.authorId.value
                it[isPublic] = knowledgeBase.isPublic
                it[createdAt] = knowledgeBase.createdAt
                it[updatedAt] = knowledgeBase.updatedAt
            }
        }
    }

    override suspend fun deleteById(id: KnowledgeBaseId) {
        KnowledgeBaseTable.deleteWhere { KnowledgeBaseTable.id eq id.value }
    }

    override suspend fun findByAuthorId(authorId: UserId): List<KnowledgeBase> =
        KnowledgeBaseTable.selectAll()
            .where { KnowledgeBaseTable.authorId eq authorId.value }
            .orderBy(KnowledgeBaseTable.updatedAt, SortOrder.DESC)
            .map(::toDomain)

    override suspend fun findByAuthorIdPaged(authorId: UserId, page: Int, limit: Int): PageResult<KnowledgeBase> {
        val condition = { KnowledgeBaseTable.authorId eq authorId.value }

        val total = KnowledgeBaseTable.selectAll()
            .where(condition)
            .count()

        val items = KnowledgeBaseTable.selectAll()
            .where(condition)
            .orderBy(KnowledgeBaseTable.updatedAt, SortOrder.DESC)
            .limit(limit)
            .offset(((page - 1) * limit).toLong())
            .map(::toDomain)

        return PageResult(items, total, page, limit)
    }

    override suspend fun findPublic(): List<KnowledgeBase> =
        KnowledgeBaseTable.selectAll()
            .where { KnowledgeBaseTable.isPublic eq true }
            .orderBy(KnowledgeBaseTable.updatedAt, SortOrder.DESC)
            .map(::toDomain)

    override suspend fun findPublicPaged(page: Int, limit: Int): PageResult<KnowledgeBase> {
        val condition = { KnowledgeBaseTable.isPublic eq true }

        val total = KnowledgeBaseTable.selectAll()
            .where(condition)
            .count()

        val items = KnowledgeBaseTable.selectAll()
            .where(condition)
            .orderBy(KnowledgeBaseTable.updatedAt, SortOrder.DESC)
            .limit(limit)
            .offset(((page - 1) * limit).toLong())
            .map(::toDomain)

        return PageResult(items, total, page, limit)
    }

    override suspend fun searchPublic(query: String, page: Int, limit: Int): PageResult<KnowledgeBase> {
        val searchPattern = "%${query.lowercase()}%"
        val condition = {
            (KnowledgeBaseTable.isPublic eq true) and
                    (KnowledgeBaseTable.name.lowerCase() like searchPattern)
        }

        val total = KnowledgeBaseTable.selectAll()
            .where(condition)
            .count()

        val items = KnowledgeBaseTable.selectAll()
            .where(condition)
            .orderBy(KnowledgeBaseTable.updatedAt, SortOrder.DESC)
            .limit(limit)
            .offset(((page - 1) * limit).toLong())
            .map(::toDomain)

        return PageResult(items, total, page, limit)
    }

    private fun toDomain(row: ResultRow): KnowledgeBase = KnowledgeBase.reconstitute(
        id = KnowledgeBaseId(row[KnowledgeBaseTable.id]),
        name = row[KnowledgeBaseTable.name],
        description = row[KnowledgeBaseTable.description],
        authorId = UserId(row[KnowledgeBaseTable.authorId]),
        isPublic = row[KnowledgeBaseTable.isPublic],
        createdAt = row[KnowledgeBaseTable.createdAt],
        updatedAt = row[KnowledgeBaseTable.updatedAt],
    )
}
