package tech.hanasaki.azusa.modules.plugin.api

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import tech.hanasaki.azusa.common.adapter.`in`.web.response.respondOk
import tech.hanasaki.azusa.common.adapter.`in`.web.route.parseLimitParam
import tech.hanasaki.azusa.common.adapter.`in`.web.route.parsePageParam
import tech.hanasaki.azusa.common.adapter.`in`.web.route.requireUserId
import tech.hanasaki.azusa.common.adapter.`in`.web.route.uuidParam
import tech.hanasaki.azusa.common.domain.model.PluginId
import tech.hanasaki.azusa.modules.plugin.api.dto.CreatePluginRequest
import tech.hanasaki.azusa.modules.plugin.api.dto.UpdatePluginRequest
import tech.hanasaki.azusa.modules.plugin.api.dto.toDetailResponse
import tech.hanasaki.azusa.modules.plugin.api.dto.toResponse
import tech.hanasaki.azusa.modules.plugin.application.service.PluginService

fun Route.pluginRoutes() {
    val pluginService: PluginService by inject()

    route("/plugins") {
        // 获取已通过审核的插件列表（公开）
        get {
            val page = parsePageParam(call.request.queryParameters["page"])
            val limit = parseLimitParam(call.request.queryParameters["limit"])
            val result = pluginService.listApprovedPlugins(page, limit)
            call.respondOk(result.toResponse())
        }

        // 搜索插件（公开）
        get("/search") {
            val query = call.request.queryParameters["q"] ?: ""
            val page = parsePageParam(call.request.queryParameters["page"])
            val limit = parseLimitParam(call.request.queryParameters["limit"])
            val result = pluginService.searchPlugins(query, page, limit)
            call.respondOk(result.toResponse())
        }

        // 获取插件详情（公开）
        get("/{pluginId}") {
            val pluginId = PluginId(call.uuidParam("pluginId"))
            val plugin = pluginService.getPlugin(pluginId)
            call.respondOk(plugin.toDetailResponse())
        }

        authenticate("auth-jwt") {
            // 创建插件
            post {
                val userId = call.requireUserId()
                val request = call.receive<CreatePluginRequest>()
                val plugin = pluginService.createPlugin(userId, request.toCommand())
                call.respondOk(plugin.toResponse(), "Plugin created, pending approval")
            }

            // 更新插件
            put("/{pluginId}") {
                val userId = call.requireUserId()
                val pluginId = PluginId(call.uuidParam("pluginId"))
                val request = call.receive<UpdatePluginRequest>()
                val plugin = pluginService.updatePlugin(userId, pluginId, request.toCommand())
                call.respondOk(plugin.toResponse(), "Plugin updated")
            }

            // 删除插件
            delete("/{pluginId}") {
                val userId = call.requireUserId()
                val pluginId = PluginId(call.uuidParam("pluginId"))
                pluginService.deletePlugin(userId, pluginId)
                call.respond(HttpStatusCode.NoContent)
            }

            // TODO: 添加管理员权限检查

            // 审批通过
            post("/{pluginId}/approve") {
                val pluginId = PluginId(call.uuidParam("pluginId"))
                val plugin = pluginService.approvePlugin(pluginId)
                call.respondOk(plugin.toResponse(), "Plugin approved")
            }

            // 审批拒绝
            post("/{pluginId}/reject") {
                val pluginId = PluginId(call.uuidParam("pluginId"))
                val plugin = pluginService.rejectPlugin(pluginId)
                call.respondOk(plugin.toResponse(), "Plugin rejected")
            }

            // 点赞
            post("/{pluginId}/like") {
                val userId = call.requireUserId()
                val pluginId = PluginId(call.uuidParam("pluginId"))
                pluginService.likePlugin(userId, pluginId)
                call.respondOk("Liked successfully")
            }

            // 取消点赞
            delete("/{pluginId}/like") {
                val userId = call.requireUserId()
                val pluginId = PluginId(call.uuidParam("pluginId"))
                pluginService.unlikePlugin(userId, pluginId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }

    authenticate("auth-jwt") {
        route("/me/plugins") {
            // 我创建的插件
            get {
                val userId = call.requireUserId()
                val page = parsePageParam(call.request.queryParameters["page"])
                val limit = parseLimitParam(call.request.queryParameters["limit"])
                val result = pluginService.listMyPlugins(userId, page, limit)
                call.respondOk(result.toResponse())
            }
        }
    }

    authenticate("auth-jwt") {
        route("/admin/plugins") {
            // 待审核插件列表
            get("/pending") {
                // TODO: 添加管理员权限检查
                val page = parsePageParam(call.request.queryParameters["page"])
                val limit = parseLimitParam(call.request.queryParameters["limit"])
                val result = pluginService.listPendingPlugins(page, limit)
                call.respondOk(result.toResponse())
            }
        }
    }
}
