package tech.hanasaki.azusa.modules.character.application.port.`in`

import tech.hanasaki.azusa.modules.character.application.port.`in`.dto.CharacterView
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface CharacterQueryUseCasePort {
    suspend fun listCharacters(
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

    suspend fun listMyCharacters(authorId: UserId, page: Int = 1, limit: Int = 10): PageResult<CharacterView>
    suspend fun listPublicCharacters(page: Int = 1, limit: Int = 10): PageResult<CharacterView>
    suspend fun searchCharacters(
        query: String,
        page: Int = 1,
        limit: Int = 10,
        userId: UserId?,
    ): PageResult<CharacterView>

    suspend fun getCharacter(authorId: UserId?, characterId: CharacterId): CharacterView
}
