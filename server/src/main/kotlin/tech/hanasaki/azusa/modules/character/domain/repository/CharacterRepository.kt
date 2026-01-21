package tech.hanasaki.azusa.modules.character.domain.repository

import tech.hanasaki.azusa.modules.character.domain.model.Character
import tech.hanasaki.azusa.shared.domain.model.CharacterId
import tech.hanasaki.azusa.shared.domain.model.UserId

interface CharacterRepository {
    suspend fun findById(id: CharacterId): Character?
    suspend fun findByAuthorId(authorId: UserId): List<Character>
    suspend fun findPublicCharacters(): List<Character>
    suspend fun save(character: Character)
    suspend fun deleteById(id: CharacterId)
}
