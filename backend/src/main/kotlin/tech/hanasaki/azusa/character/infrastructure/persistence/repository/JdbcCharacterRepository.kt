package tech.hanasaki.azusa.character.infrastructure.persistence.repository

import org.springframework.stereotype.Repository
import tech.hanasaki.azusa.character.domain.model.Character
import tech.hanasaki.azusa.character.domain.repository.CharacterRepository
import tech.hanasaki.azusa.character.infrastructure.persistence.mapper.CharacterMapper
import tech.hanasaki.azusa.shared.CharacterId
import tech.hanasaki.azusa.shared.UserId

@Repository
class JdbcCharacterRepository(
    private val characterRepository: SpringDataCharacterEntityRepository,
    private val mapper: CharacterMapper,
) : CharacterRepository {
    override fun findById(id: CharacterId): Character? =
        characterRepository.findById(id.value).orElse(null)?.let(mapper::toDomain)

    override fun findByAuthorId(authorId: UserId): List<Character> =
        characterRepository.findByAuthorId(authorId.value).map(mapper::toDomain)

    override fun findPublicCharacters(): List<Character> =
        characterRepository.findByIsPublicTrue().map(mapper::toDomain)

    override fun save(character: Character) {
        val exists = characterRepository.existsById(character.id.value)
        val entity = mapper.toEntity(character, !exists)
        characterRepository.save(entity)
    }

    override fun deleteById(id: CharacterId) {
        characterRepository.deleteById(id.value)
    }
}
