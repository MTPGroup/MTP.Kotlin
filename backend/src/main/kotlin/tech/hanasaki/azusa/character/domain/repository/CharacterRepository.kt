package tech.hanasaki.azusa.character.domain.repository

import tech.hanasaki.azusa.character.domain.model.Character
import tech.hanasaki.azusa.common.CharacterId
import tech.hanasaki.azusa.common.UserId

interface CharacterRepository {
    fun findById(id: CharacterId): Character?
    fun findByAuthorId(authorId: UserId): List<Character>
    fun findPublicCharacters(): List<Character>
    fun save(character: Character)
    fun deleteById(id: CharacterId)
}
