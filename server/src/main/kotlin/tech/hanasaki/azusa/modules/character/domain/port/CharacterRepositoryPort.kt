package tech.hanasaki.azusa.modules.character.domain.port

import tech.hanasaki.azusa.modules.character.domain.model.Character
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface CharacterRepositoryPort {
    suspend fun findById(id: CharacterId): Character?
    suspend fun findByAuthorId(authorId: UserId): List<Character>
    suspend fun findPublicCharacters(): List<Character>
    suspend fun save(character: Character)
    suspend fun deleteById(id: CharacterId)

    /**
     * 分页查询用户的角色
     */
    suspend fun findByAuthorIdPaged(authorId: UserId, page: Int, limit: Int): PageResult<Character>

    /**
     * 分页查询公开角色
     */
    suspend fun findPublicCharactersPaged(page: Int, limit: Int): PageResult<Character>

    suspend fun searchCharacters(query: String, page: Int, limit: Int, userId: UserId?): PageResult<Character>
}
