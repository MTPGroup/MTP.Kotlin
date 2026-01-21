package tech.hanasaki.azusa.modules.character.application.service

import tech.hanasaki.azusa.modules.character.application.command.CreateCharacterCommand
import tech.hanasaki.azusa.modules.character.application.command.UpdateCharacterCommand
import tech.hanasaki.azusa.modules.character.domain.model.Character
import tech.hanasaki.azusa.modules.character.domain.repository.CharacterRepository
import tech.hanasaki.azusa.shared.domain.exception.AuthorizationException
import tech.hanasaki.azusa.shared.domain.exception.DomainException
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException
import tech.hanasaki.azusa.shared.domain.model.CharacterId
import tech.hanasaki.azusa.shared.domain.model.UserId
import java.util.*
import kotlin.time.Clock

class CharacterService(
    private val characterRepository: CharacterRepository,
) {
    suspend fun listMyCharacters(authorId: UserId): List<Character> {
        return characterRepository.findByAuthorId(authorId)
    }

    suspend fun listPublicCharacters(): List<Character> {
        return characterRepository.findPublicCharacters()
    }

    suspend fun getCharacter(authorId: UserId, characterId: CharacterId): Character {
        val character = characterRepository.findById(characterId)
            ?: throw NotFoundException("Character not found")
        if (!character.isPublic && character.authorId != authorId) {
            throw AuthorizationException("Access denied")
        }
        return character
    }

    suspend fun createCharacter(authorId: UserId, cmd: CreateCharacterCommand): Character {
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

    suspend fun updateCharacter(authorId: UserId, characterId: CharacterId, cmd: UpdateCharacterCommand): Character {
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

    suspend fun deleteCharacter(authorId: UserId, characterId: CharacterId) {
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
