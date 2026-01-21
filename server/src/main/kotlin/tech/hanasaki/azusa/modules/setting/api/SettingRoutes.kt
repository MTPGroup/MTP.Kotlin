package tech.hanasaki.azusa.modules.setting.api

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import tech.hanasaki.azusa.modules.setting.api.dto.CreateLLMConfigRequest
import tech.hanasaki.azusa.modules.setting.api.dto.UpdateLLMConfigRequest
import tech.hanasaki.azusa.modules.setting.api.dto.UpdateSettingRequest
import tech.hanasaki.azusa.modules.setting.api.dto.toResponse
import tech.hanasaki.azusa.modules.setting.application.command.GetSettingCommand
import tech.hanasaki.azusa.modules.setting.application.service.SettingService
import tech.hanasaki.azusa.shared.api.ApiException
import tech.hanasaki.azusa.shared.domain.model.UserId
import tech.hanasaki.azusa.shared.infrastructure.utils.uuidParam
import java.util.*

fun Route.settingRoutes() {
    val settingService: SettingService by inject()

    authenticate("auth-jwt") {
        route("/settings") {
            get {
                val userId = call.requireUserId()
                val setting = settingService.getSetting(GetSettingCommand(userId))
                call.respond(setting.toResponse())
            }

            put {
                val userId = call.requireUserId()
                val request = call.receive<UpdateSettingRequest>()
                val updated = settingService.updateSetting(userId, request.toCommand())
                call.respond(updated.toResponse())
            }

            get("/llm-configs") {
                val userId = call.requireUserId()
                val configs = settingService.listLlmConfigs(userId).map { it.toResponse() }.toSet()
                call.respond(configs)
            }

            get("/llm-configs/{configId}") {
                val userId = call.requireUserId()
                val configId = call.uuidParam("configId")
                val config = settingService.getLlmConfig(
                    userId,
                    tech.hanasaki.azusa.modules.setting.domain.model.LLMConfigId(configId)
                )
                call.respond(config.toResponse())
            }

            post("/llm-configs") {
                val userId = call.requireUserId()
                val request = call.receive<CreateLLMConfigRequest>()
                val updated = settingService.addLlmConfig(userId, request.toDomain())
                call.respond(updated.toResponse())
            }

            put("/llm-configs/{configId}") {
                val userId = call.requireUserId()
                val configId = call.uuidParam("configId")
                val request = call.receive<UpdateLLMConfigRequest>()
                val updated = settingService.updateLlmConfig(
                    userId,
                    request.toDomain(configId)
                )
                call.respond(updated.toResponse())
            }

            delete("/llm-configs/{configId}") {
                val userId = call.requireUserId()
                val configId = call.uuidParam("configId")
                val updated = settingService.deleteLlmConfig(
                    userId,
                    tech.hanasaki.azusa.modules.setting.domain.model.LLMConfigId(configId)
                )
                call.respond(updated.toResponse())
            }

            post("/llm-configs/{configId}/select") {
                val userId = call.requireUserId()
                val configId = call.uuidParam("configId")
                val updated = settingService.selectLlmConfig(
                    userId,
                    tech.hanasaki.azusa.modules.setting.domain.model.LLMConfigId(configId)
                )
                call.respond(updated.toResponse())
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
