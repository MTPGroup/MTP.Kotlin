package tech.hanasaki.azusa.modules.setting.api

import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import tech.hanasaki.azusa.common.platform.api.respondOk
import tech.hanasaki.azusa.common.platform.api.requireUserId
import tech.hanasaki.azusa.common.platform.api.uuidParam
import tech.hanasaki.azusa.modules.setting.api.dto.CreateLLMConfigRequest
import tech.hanasaki.azusa.modules.setting.api.dto.UpdateLLMConfigRequest
import tech.hanasaki.azusa.modules.setting.api.dto.UpdateSettingRequest
import tech.hanasaki.azusa.modules.setting.api.dto.toResponse
import tech.hanasaki.azusa.modules.setting.application.command.GetSettingCommand
import tech.hanasaki.azusa.modules.setting.application.service.SettingService
import tech.hanasaki.azusa.modules.setting.domain.model.LLMConfigId

fun Route.settingRoutes() {
    val settingService: SettingService by inject()

    authenticate("auth-jwt") {
        route("/settings") {
            get {
                val userId = call.requireUserId()
                val setting = settingService.getSetting(GetSettingCommand(userId))
                call.respondOk(setting.toResponse())
            }

            put {
                val userId = call.requireUserId()
                val request = call.receive<UpdateSettingRequest>()
                val updated = settingService.updateSetting(userId, request.toCommand())
                call.respondOk(updated.toResponse())
            }

            get("/llm-configs") {
                val userId = call.requireUserId()
                val configs = settingService.listLlmConfigs(userId).map { it.toResponse() }.toSet()
                call.respondOk(configs)
            }

            get("/llm-configs/{configId}") {
                val userId = call.requireUserId()
                val configId = call.uuidParam("configId")
                val config = settingService.getLlmConfig(
                    userId,
                    tech.hanasaki.azusa.modules.setting.domain.model.LLMConfigId(configId)
                )
                call.respondOk(config.toResponse())
            }

            post("/llm-configs") {
                val userId = call.requireUserId()
                val request = call.receive<CreateLLMConfigRequest>()
                val updated = settingService.addLlmConfig(userId, request.toDomain())
                call.respondOk(updated.toResponse())
            }

            put("/llm-configs/{configId}") {
                val userId = call.requireUserId()
                val configId = call.uuidParam("configId")
                val request = call.receive<UpdateLLMConfigRequest>()
                val updated = settingService.updateLlmConfig(
                    userId,
                    request.toDomain(configId)
                )
                call.respondOk(updated.toResponse())
            }

            delete("/llm-configs/{configId}") {
                val userId = call.requireUserId()
                val configId = call.uuidParam("configId")
                val updated = settingService.deleteLlmConfig(
                    userId,
                    LLMConfigId(configId)
                )
                call.respondOk(updated.toResponse())
            }

            post("/llm-configs/{configId}/select") {
                val userId = call.requireUserId()
                val configId = call.uuidParam("configId")
                val updated = settingService.selectLlmConfig(
                    userId,
                    LLMConfigId(configId)
                )
                call.respondOk(updated.toResponse())
            }
        }
    }
}
