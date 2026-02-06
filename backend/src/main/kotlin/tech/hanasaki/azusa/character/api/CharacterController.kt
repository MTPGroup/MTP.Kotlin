package tech.hanasaki.azusa.character.api

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tech.hanasaki.azusa.character.api.dto.CreateCharacterRequest
import tech.hanasaki.azusa.character.api.dto.UpdateCharacterRequest
import tech.hanasaki.azusa.character.application.service.CharacterService
import tech.hanasaki.azusa.character.domain.model.Character
import tech.hanasaki.azusa.shared.ApiException
import tech.hanasaki.azusa.shared.CharacterId
import tech.hanasaki.azusa.shared.UserId
import java.util.UUID

@RestController
@RequestMapping("/characters")
class CharacterController(
    private val characterService: CharacterService,
) {

    @GetMapping
    fun listMyCharacters(authentication: Authentication): ResponseEntity<List<Character>> {
        val userId = requireUserId(authentication)
        val characters = characterService.listMyCharacters(userId)
        return ResponseEntity.ok(characters)
    }

    @GetMapping("/public")
    fun listPublicCharacters(): ResponseEntity<List<Character>> {
        val characters = characterService.listPublicCharacters()
        return ResponseEntity.ok(characters)
    }

    @GetMapping("/{characterId}")
    fun getCharacter(
        authentication: Authentication,
        @PathVariable characterId: UUID,
    ): ResponseEntity<Character> {
        val userId = requireUserId(authentication)
        val character = characterService.getCharacter(userId, CharacterId(characterId))
        return ResponseEntity.ok(character)
    }

    @PostMapping
    fun createCharacter(
        authentication: Authentication,
        @RequestBody request: CreateCharacterRequest,
    ): ResponseEntity<Character> {
        val userId = requireUserId(authentication)
        val character = characterService.createCharacter(userId, request.toCommand())
        return ResponseEntity.ok(character)
    }

    @PutMapping("/{characterId}")
    fun updateCharacter(
        authentication: Authentication,
        @PathVariable characterId: UUID,
        @RequestBody request: UpdateCharacterRequest,
    ): ResponseEntity<Character> {
        val userId = requireUserId(authentication)
        val character = characterService.updateCharacter(userId, CharacterId(characterId), request.toCommand())
        return ResponseEntity.ok(character)
    }

    @DeleteMapping("/{characterId}")
    fun deleteCharacter(
        authentication: Authentication,
        @PathVariable characterId: UUID,
    ): ResponseEntity<Void> {
        val userId = requireUserId(authentication)
        characterService.deleteCharacter(userId, CharacterId(characterId))
        return ResponseEntity.noContent().build()
    }

    private fun requireUserId(authentication: Authentication): UserId {
        val subject = authentication.principal as? String
            ?: throw ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Missing authentication")
        val userId = runCatching { UUID.fromString(subject) }.getOrNull()
            ?: throw ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Invalid subject")
        return UserId(userId)
    }
}
