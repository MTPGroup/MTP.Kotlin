package tech.hanasaki.azusa.modules.knowledge.adapter.out.persistence.repository

import org.jetbrains.exposed.v1.core.JoinType
import org.jetbrains.exposed.v1.core.Op
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.like
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.jdbc.selectAll
import tech.hanasaki.azusa.modules.auth.adapter.out.persistence.table.ProfileTable
import tech.hanasaki.azusa.modules.knowledge.adapter.out.persistence.table.KnowledgeBaseTable
import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.dto.KnowledgeBaseAuthorView
import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.dto.KnowledgeBaseView
import tech.hanasaki.azusa.modules.knowledge.application.port.out.KnowledgeBaseQueryRepositoryPort
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

class ExposedKnowledgeBaseQueryRepository : KnowledgeBaseQueryRepositoryPort {
    override suspend fun findPublicPaged(page: Int, limit: Int): PageResult<KnowledgeBaseView> {
        val condition = { KnowledgeBaseTable.isPublic eq true }
        return findPaged(condition, page, limit)
    }

    override suspend fun searchPublic(query: String, page: Int, limit: Int): PageResult<KnowledgeBaseView> {
        val searchPattern = "%${query.lowercase()}%"
        val condition = {
            (KnowledgeBaseTable.isPublic eq true) and
                    (KnowledgeBaseTable.name.lowerCase() like searchPattern)
        }
        return findPaged(condition, page, limit)
    }

    override suspend fun findByAuthorIdPaged(authorId: UserId, page: Int, limit: Int): PageResult<KnowledgeBaseView> {
        val condition = { KnowledgeBaseTable.authorId eq authorId.value }
        return findPaged(condition, page, limit)
    }

    override suspend fun findVisibleById(knowledgeBaseId: KnowledgeBaseId, userId: UserId?): KnowledgeBaseView? {
        val condition = {
            (KnowledgeBaseTable.id eq knowledgeBaseId.value) and
                    ((KnowledgeBaseTable.isPublic eq true) or (userId?.let { KnowledgeBaseTable.authorId eq it.value } ?: Op.FALSE))
        }

        return joinedQuery()
            .where(condition)
            .map(::toView)
            .singleOrNull()
    }

    private fun findPaged(condition: () -> Op<Boolean>, page: Int, limit: Int): PageResult<KnowledgeBaseView> {
        val total = KnowledgeBaseTable.selectAll()
            .where(condition)
            .count()

        val items = joinedQuery()
            .where(condition)
            .orderBy(KnowledgeBaseTable.updatedAt, SortOrder.DESC)
            .limit(limit)
            .offset(((page - 1) * limit).toLong())
            .map(::toView)

        return PageResult(items, total, page, limit)
    }

    private fun joinedQuery() = KnowledgeBaseTable
        .join(ProfileTable, JoinType.LEFT, KnowledgeBaseTable.authorId, ProfileTable.uid)
        .selectAll()

    private fun toView(row: ResultRow): KnowledgeBaseView {
        val authorId = row[KnowledgeBaseTable.authorId]
        val profileId = row.getOrNull(ProfileTable.uid)
        val author = profileId?.let {
            KnowledgeBaseAuthorView(
                id = it,
                name = row.getOrNull(ProfileTable.username) ?: "",
                avatar = row.getOrNull(ProfileTable.avatar),
            )
        }

        return KnowledgeBaseView(
            id = row[KnowledgeBaseTable.id],
            name = row[KnowledgeBaseTable.name],
            description = row[KnowledgeBaseTable.description],
            authorId = authorId,
            author = author,
            isPublic = row[KnowledgeBaseTable.isPublic],
            createdAt = row[KnowledgeBaseTable.createdAt].toString(),
            updatedAt = row[KnowledgeBaseTable.updatedAt].toString(),
        )
    }
}
