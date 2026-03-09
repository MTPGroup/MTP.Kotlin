package tech.hanasaki.azusa.modules.character.application.port.out

import tech.hanasaki.azusa.modules.character.application.port.`in`.dto.CharacterView
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface CharacterQueryRepositoryPort {
    suspend fun findCharactersPaged(
        page: Int,
        limit: Int,
        query: String?,
        visibility: String?,
        scope: String?,
        authorId: UserId?,
        userId: UserId?,
        sort: String?,
        tags: Set<String>?,
    ): PageResult<CharacterView>

    suspend fun findByAuthorIdPaged(authorId: UserId, page: Int, limit: Int): PageResult<CharacterView>
    suspend fun findPublicCharactersPaged(page: Int, limit: Int): PageResult<CharacterView>
    suspend fun findTrendingCharacters(period: String, limit: Int): List<CharacterView>
    suspend fun findRecommendedCharacters(userId: UserId, limit: Int): List<CharacterView>
    suspend fun searchCharacters(query: String, page: Int, limit: Int, userId: UserId?): PageResult<CharacterView>
    suspend fun findVisibleById(characterId: CharacterId, userId: UserId?): CharacterView?
}
