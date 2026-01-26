package tech.hanasaki.azusa.modules.character.api

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import tech.hanasaki.azusa.common.platform.api.respondOk
import tech.hanasaki.azusa.common.kernel.model.CharacterId
import tech.hanasaki.azusa.common.kernel.model.KnowledgeBaseId
import tech.hanasaki.azusa.common.platform.api.parseLimitParam
import tech.hanasaki.azusa.common.platform.api.parsePageParam
import tech.hanasaki.azusa.common.platform.api.requireUserId
import tech.hanasaki.azusa.common.platform.api.uuidParam
import tech.hanasaki.azusa.modules.character.api.dto.CreateCharacterRequest
import tech.hanasaki.azusa.modules.character.api.dto.SubscribeKnowledgeBaseRequest
import tech.hanasaki.azusa.modules.character.api.dto.UpdateCharacterRequest
import tech.hanasaki.azusa.modules.character.api.dto.toResponse
import tech.hanasaki.azusa.modules.character.application.service.CharacterService

fun Route.characterRoutes() {
    val characterService: CharacterService by inject()

    authenticate("auth-jwt") {
        route("/characters") {
            // 获取我的角色列表（分页）
            get {
                val userId = call.requireUserId()
                val page = parsePageParam(call.request.queryParameters["page"])
                val limit = parseLimitParam(call.request.queryParameters["limit"])
                val result = characterService.listMyCharactersPaged(userId, page, limit)
                call.respondOk(result.toResponse())
            }

            // 获取公开角色列表（分页）
            get("/public") {
                val page = parsePageParam(call.request.queryParameters["page"])
                val limit = parseLimitParam(call.request.queryParameters["limit"])
                val result = characterService.listPublicCharactersPaged(page, limit)
                call.respondOk(result.toResponse())
            }

            // 搜索公开角色
            get("/search") {
                val query = call.request.queryParameters["q"] ?: ""
                val page = parsePageParam(call.request.queryParameters["page"])
                val limit = parseLimitParam(call.request.queryParameters["limit"])
                val result = characterService.searchPublicCharacters(query, page, limit)
                call.respondOk(result.toResponse())
            }

            // 获取角色详情
            get("/{characterId}") {
                val userId = call.requireUserId()
                val characterId = CharacterId(call.uuidParam("characterId"))
                val character = characterService.getCharacter(userId, characterId)
                call.respondOk(character.toResponse())
            }

            // 创建角色
            post {
                val userId = call.requireUserId()
                val request = call.receive<CreateCharacterRequest>()
                val character = characterService.createCharacter(userId, request.toCommand())
                call.respondOk(character.toResponse(), "Character created")
            }

            // 更新角色
            put("/{characterId}") {
                val userId = call.requireUserId()
                val characterId = CharacterId(call.uuidParam("characterId"))
                val request = call.receive<UpdateCharacterRequest>()
                val character = characterService.updateCharacter(userId, characterId, request.toCommand())
                call.respondOk(character.toResponse(), "Character updated")
            }

            // 删除角色
            delete("/{characterId}") {
                val userId = call.requireUserId()
                val characterId = CharacterId(call.uuidParam("characterId"))
                characterService.deleteCharacter(userId, characterId)
                call.respond(HttpStatusCode.NoContent)
            }

            // 获取角色订阅的知识库列表
            get("/{characterId}/knowledge-bases") {
                val userId = call.requireUserId()
                val characterId = CharacterId(call.uuidParam("characterId"))
                val subscriptions = characterService.getKnowledgeSubscriptions(userId, characterId)
                call.respondOk(subscriptions.map { it.toResponse() })
            }

            // 链接知识库到角色
            post("/{characterId}/knowledge-bases") {
                val userId = call.requireUserId()
                val characterId = CharacterId(call.uuidParam("characterId"))
                val request = call.receive<SubscribeKnowledgeBaseRequest>()
                characterService.subscribeKnowledgeBase(
                    userId,
                    characterId,
                    KnowledgeBaseId(request.knowledgeBaseId),
                    request.priority
                )
                call.respondOk("Knowledge base linked")
            }

            // 解除角色与知识库的链接
            delete("/{characterId}/knowledge-bases/{knowledgeBaseId}") {
                val userId = call.requireUserId()
                val characterId = CharacterId(call.uuidParam("characterId"))
                val knowledgeBaseId = KnowledgeBaseId(call.uuidParam("knowledgeBaseId"))
                characterService.unsubscribeKnowledgeBase(userId, characterId, knowledgeBaseId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
