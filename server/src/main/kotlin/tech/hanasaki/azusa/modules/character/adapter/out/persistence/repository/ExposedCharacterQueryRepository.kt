package tech.hanasaki.azusa.modules.character.adapter.out.persistence.repository

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
import tech.hanasaki.azusa.modules.character.adapter.out.persistence.table.CharacterTable
import tech.hanasaki.azusa.modules.character.application.port.`in`.dto.CharacterAuthorView
import tech.hanasaki.azusa.modules.character.application.port.`in`.dto.CharacterView
import tech.hanasaki.azusa.modules.character.application.port.out.CharacterQueryRepositoryPort
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

class ExposedCharacterQueryRepository : CharacterQueryRepositoryPort {
    override suspend fun findByAuthorIdPaged(authorId: UserId, page: Int, limit: Int): PageResult<CharacterView> {
        val condition = { CharacterTable.authorId eq authorId.value }
        return findPaged(condition, page, limit)
    }

    override suspend fun findPublicCharactersPaged(page: Int, limit: Int): PageResult<CharacterView> {
        val condition = { CharacterTable.isPublic eq true }
        return findPaged(condition, page, limit)
    }

    override suspend fun searchCharacters(
        query: String,
        page: Int,
        limit: Int,
        userId: UserId?,
    ): PageResult<CharacterView> {
        val searchPattern = "%${query.lowercase()}%"
        val condition = {
            ((CharacterTable.isPublic eq true) or (userId?.let { CharacterTable.authorId eq it.value } ?: Op.FALSE)) and
                    (CharacterTable.name.lowerCase() like searchPattern)
        }
        return findPaged(condition, page, limit)
    }

    override suspend fun findVisibleById(characterId: CharacterId, userId: UserId?): CharacterView? {
        val condition = {
            (CharacterTable.id eq characterId.value) and
                    ((CharacterTable.isPublic eq true) or (userId?.let { CharacterTable.authorId eq it.value } ?: Op.FALSE))
        }
        return joinedQuery()
            .where(condition)
            .map(::toView)
            .singleOrNull()
    }

    private fun findPaged(condition: () -> Op<Boolean>, page: Int, limit: Int): PageResult<CharacterView> {
        val total = CharacterTable.selectAll()
            .where(condition)
            .count()

        val items = joinedQuery()
            .where(condition)
            .orderBy(CharacterTable.updatedAt, SortOrder.DESC)
            .limit(limit)
            .offset(((page - 1) * limit).toLong())
            .map(::toView)

        return PageResult(items, total, page, limit)
    }

    private fun joinedQuery() = CharacterTable
        .join(ProfileTable, JoinType.LEFT, CharacterTable.authorId, ProfileTable.uid)
        .selectAll()

    private fun toView(row: ResultRow): CharacterView {
        val authorId = row[CharacterTable.authorId]
        val profileId = row.getOrNull(ProfileTable.uid)
        val author = profileId?.let {
            CharacterAuthorView(
                id = it,
                name = row.getOrNull(ProfileTable.username) ?: "",
                avatar = row.getOrNull(ProfileTable.avatar),
            )
        }

        return CharacterView(
            id = row[CharacterTable.id],
            authorId = authorId,
            author = author,
            name = row[CharacterTable.name],
            avatar = row[CharacterTable.avatar],
            bio = row[CharacterTable.bio],
            originPrompt = row[CharacterTable.originPrompt],
            isPublic = row[CharacterTable.isPublic],
            createdAt = row[CharacterTable.createdAt].toString(),
            updatedAt = row[CharacterTable.updatedAt].toString(),
        )
    }
}
