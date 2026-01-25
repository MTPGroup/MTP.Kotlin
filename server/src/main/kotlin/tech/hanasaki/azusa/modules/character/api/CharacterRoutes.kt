package tech.hanasaki.azusa.modules.character.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import tech.hanasaki.azusa.modules.character.api.dto.CreateCharacterRequest
import tech.hanasaki.azusa.modules.character.api.dto.UpdateCharacterRequest
import tech.hanasaki.azusa.modules.character.api.dto.toResponse
import tech.hanasaki.azusa.modules.character.application.service.CharacterService
import tech.hanasaki.azusa.shared.api.response.ApiException
import tech.hanasaki.azusa.shared.domain.model.CharacterId
import tech.hanasaki.azusa.shared.domain.model.UserId
import tech.hanasaki.azusa.shared.api.helper.uuidParam
import java.util.*

fun Route.characterRoutes() {
    val characterService: CharacterService by inject()

    authenticate("auth-jwt") {
        route("/characters") {
            get {
                val userId = call.requireUserId()
                val characters = characterService.listMyCharacters(userId).map { it.toResponse() }
                call.respond(characters)
            }

            get("/public") {
                val characters = characterService.listPublicCharacters().map { it.toResponse() }
                call.respond(characters)
            }

            get("/{characterId}") {
                val userId = call.requireUserId()
                val characterId = CharacterId(call.uuidParam("characterId"))
                val character = characterService.getCharacter(userId, characterId)
                call.respond(character.toResponse())
            }

            post {
                val userId = call.requireUserId()
                val request = call.receive<CreateCharacterRequest>()
                val character = characterService.createCharacter(userId, request.toCommand())
                call.respond(character.toResponse())
            }

            put("/{characterId}") {
                val userId = call.requireUserId()
                val characterId = CharacterId(call.uuidParam("characterId"))
                val request = call.receive<UpdateCharacterRequest>()
                val character = characterService.updateCharacter(userId, characterId, request.toCommand())
                call.respond(character.toResponse())
            }

            delete("/{characterId}") {
                val userId = call.requireUserId()
                val characterId = CharacterId(call.uuidParam("characterId"))
                characterService.deleteCharacter(userId, characterId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}

private fun ApplicationCall.requireUserId(): UserId {
    val principal = principal<JWTPrincipal>() ?: throw ApiException(
        HttpStatusCode.Unauthorized,
        "UNAUTHORIZED",
        "Missing authentication"
    )
    val userId = principal.subject?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        ?: throw ApiException(HttpStatusCode.Unauthorized, "UNAUTHORIZED", "Invalid subject")
    return UserId(userId)
}
