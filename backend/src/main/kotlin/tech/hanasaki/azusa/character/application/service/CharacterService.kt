package tech.hanasaki.azusa.character.application.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tech.hanasaki.azusa.character.application.command.CreateCharacterCommand
import tech.hanasaki.azusa.character.application.command.UpdateCharacterCommand
import tech.hanasaki.azusa.character.domain.model.Character
import tech.hanasaki.azusa.character.domain.repository.CharacterRepository
import tech.hanasaki.azusa.common.AuthorizationException
import tech.hanasaki.azusa.common.CharacterId
import tech.hanasaki.azusa.common.DomainException
import tech.hanasaki.azusa.common.NotFoundException
import tech.hanasaki.azusa.common.UserId
import java.util.UUID
import kotlin.time.Clock

@Service
class CharacterService(
    private val characterRepository: CharacterRepository,
) {
    @Transactional
    fun listMyCharacters(authorId: UserId): List<Character> {
        return characterRepository.findByAuthorId(authorId)
    }

    @Transactional
    fun listPublicCharacters(): List<Character> {
        return characterRepository.findPublicCharacters()
    }

    @Transactional
    fun getCharacter(authorId: UserId, characterId: CharacterId): Character {
        val character = characterRepository.findById(characterId)
            ?: throw NotFoundException("Character not found")
        if (!character.isPublic && character.authorId != authorId) {
            throw AuthorizationException("Access denied")
        }
        return character
    }

    @Transactional
    fun createCharacter(authorId: UserId, cmd: CreateCharacterCommand): Character {
        validate(cmd.name)
        val now = Clock.System.now()
        val character = Character(
            id = CharacterId(UUID.randomUUID()),
            authorId = authorId,
            name = cmd.name,
            avatar = cmd.avatar,
            bio = cmd.bio,
            originPrompt = cmd.originPrompt,
            isPublic = cmd.isPublic,
            createdAt = now,
            updatedAt = now,
        )
        characterRepository.save(character)
        return character
    }

    @Transactional
    fun updateCharacter(authorId: UserId, characterId: CharacterId, cmd: UpdateCharacterCommand): Character {
        validate(cmd.name)
        val existing = characterRepository.findById(characterId)
            ?: throw NotFoundException("Character not found")
        if (existing.authorId != authorId) {
            throw AuthorizationException("Access denied")
        }
        val updated = existing.copy(
            name = cmd.name,
            avatar = cmd.avatar,
            bio = cmd.bio,
            originPrompt = cmd.originPrompt,
            isPublic = cmd.isPublic,
            updatedAt = Clock.System.now(),
        )
        characterRepository.save(updated)
        return updated
    }

    @Transactional
    fun deleteCharacter(authorId: UserId, characterId: CharacterId) {
        val existing = characterRepository.findById(characterId)
            ?: throw NotFoundException("Character not found")
        if (existing.authorId != authorId) {
            throw AuthorizationException("Access denied")
        }
        characterRepository.deleteById(characterId)
    }

    private fun validate(name: String) {
        if (name.isBlank()) {
            throw DomainException("Name is required")
        }
    }
}
