package tech.hanasaki.azusa.modules.character.adapter.out.persistence.repository

import org.jetbrains.exposed.v1.core.*
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
    override suspend fun findCharactersPaged(
        page: Int,
        limit: Int,
        query: String?,
        visibility: String?,
        scope: String?,
        authorId: UserId?,
        userId: UserId?,
        sort: String?,
        tags: Set<String>?,
    ): PageResult<CharacterView> {
        val condition = buildListCondition(
            query = query,
            visibility = visibility,
            scope = scope,
            authorId = authorId,
            userId = userId,
            tags = tags,
        )
        val orderBy = resolveSort(sort)

        val total = CharacterTable.selectAll()
            .where { condition }
            .count()

        val items = joinedQuery()
            .where { condition }
            .orderBy(*orderBy.toTypedArray())
            .limit(limit)
            .offset(((page - 1) * limit).toLong())
            .map(::toView)

        return PageResult(items, total, page, limit)
    }

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
                    ((CharacterTable.isPublic eq true) or (userId?.let { CharacterTable.authorId eq it.value }
                        ?: Op.FALSE))
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

    private fun buildListCondition(
        query: String?,
        visibility: String?,
        scope: String?,
        authorId: UserId?,
        userId: UserId?,
        tags: Set<String>?,
    ): Op<Boolean> {
        var condition: Op<Boolean> = when (visibility) {
            "public" -> CharacterTable.isPublic eq true
            "private" -> userId?.let { CharacterTable.authorId eq it.value } ?: Op.FALSE
            else -> (CharacterTable.isPublic eq true) or (userId?.let { CharacterTable.authorId eq it.value }
                ?: Op.FALSE)
        }

        if (scope == "mine") {
            condition = condition and (userId?.let { CharacterTable.authorId eq it.value } ?: Op.FALSE)
        }

        if (authorId != null) {
            condition = condition and (CharacterTable.authorId eq authorId.value)
        }

        if (!query.isNullOrBlank()) {
            val searchPattern = "%${query.trim().lowercase()}%"
            condition = condition and (
                    (CharacterTable.name.lowerCase() like searchPattern) or
                            (CharacterTable.bio.lowerCase() like searchPattern)
                    )
        }

        if (!tags.isNullOrEmpty()) {
            tags.forEach { tag ->
                val pattern = "%${tag.lowercase()}%"
                condition = condition and (CharacterTable.tags.lowerCase() like pattern)
            }
        }

        return condition
    }

    private fun resolveSort(sort: String?): List<Pair<Expression<*>, SortOrder>> = when (sort) {
        "name" -> listOf(CharacterTable.name to SortOrder.ASC)
        "popular" -> listOf(
            CharacterTable.favoriteCount to SortOrder.DESC,
            CharacterTable.chatCount to SortOrder.DESC,
            CharacterTable.updatedAt to SortOrder.DESC,
        )
        else -> listOf(CharacterTable.updatedAt to SortOrder.DESC)
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
            tags = parseTags(row[CharacterTable.tags]),
            originPrompt = row[CharacterTable.originPrompt],
            isPublic = row[CharacterTable.isPublic],
            favoriteCount = row[CharacterTable.favoriteCount],
            chatCount = row[CharacterTable.chatCount],
            createdAt = row[CharacterTable.createdAt].toString(),
            updatedAt = row[CharacterTable.updatedAt].toString(),
        )
    }

    private fun parseTags(raw: String): List<String> =
        raw.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
}
