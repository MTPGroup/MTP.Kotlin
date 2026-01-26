package tech.hanasaki.azusa.modules.character.domain.repository

import tech.hanasaki.azusa.modules.character.domain.model.Character
import tech.hanasaki.azusa.common.kernel.model.CharacterId
import tech.hanasaki.azusa.common.kernel.model.PageResult
import tech.hanasaki.azusa.common.kernel.model.UserId

interface CharacterRepository {
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

    /**
     * 搜索公开角色（按名称模糊匹配）
     */
    suspend fun searchPublicCharacters(query: String, page: Int, limit: Int): PageResult<Character>
}
