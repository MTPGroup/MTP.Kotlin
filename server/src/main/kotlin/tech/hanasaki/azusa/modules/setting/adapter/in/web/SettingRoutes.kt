package tech.hanasaki.azusa.modules.setting.adapter.`in`.web

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import org.koin.ktor.ext.inject
import tech.hanasaki.azusa.modules.setting.adapter.`in`.web.dto.SettingResponse
import tech.hanasaki.azusa.modules.setting.adapter.`in`.web.dto.UpdateSettingRequest
import tech.hanasaki.azusa.modules.setting.adapter.`in`.web.dto.toResponse
import tech.hanasaki.azusa.modules.setting.application.port.`in`.SettingUseCasePort
import tech.hanasaki.azusa.shared.infrastructure.web.response.ApiResponse
import tech.hanasaki.azusa.shared.infrastructure.web.response.respondOk
import tech.hanasaki.azusa.shared.infrastructure.web.route.requireUserId

fun Route.settingRoutes() {
    val settingService: SettingUseCasePort by inject()

    authenticate("auth-jwt") {
        route("/settings") {
            get {
                val userId = call.requireUserId()
                val setting = settingService.getSetting(userId)
                call.respondOk(setting.toResponse())
            }.describe {
                tag("设置管理")
                operationId = "getSetting"
                summary = "获取用户设置"
                description = "获取当前登录用户的设置信息，包括主题和LLM配置"
                responses {
                    HttpStatusCode.OK {
                        description = "获取成功"
                        schema = jsonSchema<ApiResponse<SettingResponse>>()
                    }
                    HttpStatusCode.NotFound {
                        description = "设置不存在"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }

            put {
                val userId = call.requireUserId()
                val request = call.receive<UpdateSettingRequest>()
                val updated = settingService.updateSetting(
                    userId = userId,
                    theme = request.theme,
                    llmConfigs = request.llmConfigs.map { it.toDomain() }.toSet(),
                    activeThemeId = request.activeThemeIdVo,
                    activeLlmConfigId = request.activeLlmConfigIdVo,
                )
                call.respondOk(updated.toResponse())
            }.describe {
                tag("设置管理")
                operationId = "updateSetting"
                summary = "更新用户设置"
                description = "全量更新当前用户的设置，包括主题、LLM配置列表及激活的配置"
                requestBody {
                    description = "设置信息"
                    schema = jsonSchema<UpdateSettingRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "更新成功"
                        schema = jsonSchema<ApiResponse<SettingResponse>>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "输入参数无效（如温度超出范围、activeLlmConfigId不在配置列表中等）"
                    }
                    HttpStatusCode.NotFound {
                        description = "设置不存在"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }
        }
    }
}
