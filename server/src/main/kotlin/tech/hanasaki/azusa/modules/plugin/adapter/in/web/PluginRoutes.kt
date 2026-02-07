package tech.hanasaki.azusa.modules.plugin.adapter.`in`.web

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import org.koin.ktor.ext.inject
import tech.hanasaki.azusa.modules.plugin.adapter.`in`.web.dto.*
import tech.hanasaki.azusa.modules.plugin.application.port.`in`.PluginUseCasePort
import tech.hanasaki.azusa.shared.domain.model.vo.PluginId
import tech.hanasaki.azusa.shared.infrastructure.web.response.ApiResponse
import tech.hanasaki.azusa.shared.infrastructure.web.response.respondOk
import tech.hanasaki.azusa.shared.infrastructure.web.route.requireAdmin
import tech.hanasaki.azusa.shared.infrastructure.web.route.requireUserId
import tech.hanasaki.azusa.shared.infrastructure.web.route.uuidParam
import tech.hanasaki.azusa.shared.infrastructure.web.validation.validateLimit
import tech.hanasaki.azusa.shared.infrastructure.web.validation.validatePage

fun Route.pluginRoutes() {
    val pluginService: PluginUseCasePort by inject()

    route("/plugins") {
        get {
            val page = call.request.queryParameters["page"]?.let { validatePage(it) } ?: 1
            val limit = call.request.queryParameters["limit"]?.let { validateLimit(it) } ?: 10
            val result = pluginService.listApprovedPlugins(page, limit)
            call.respondOk(result.toResponse())
        }.describe {
            tag("插件管理")
            operationId = "listApprovedPlugins"
            summary = "获取已通过审核的插件列表"
            description = "获取所有已通过审核的插件，按点赞数和更新时间排序，支持分页"
            parameters {
                query("page") {
                    description = "页码，从1开始"
                    required = false
                }
                query("limit") {
                    description = "每页数量，1-100"
                    required = false
                }
            }
            responses {
                HttpStatusCode.OK {
                    description = "获取成功"
                    schema = jsonSchema<ApiResponse<PagedPluginResponse>>()
                }
            }
        }

        get("/search") {
            val query = call.request.queryParameters["q"] ?: ""
            val page = call.request.queryParameters["page"]?.let { validatePage(it) } ?: 1
            val limit = call.request.queryParameters["limit"]?.let { validateLimit(it) } ?: 10
            val result = pluginService.searchPlugins(query, page, limit)
            call.respondOk(result.toResponse())
        }.describe {
            tag("插件管理")
            operationId = "searchPlugins"
            summary = "搜索插件"
            description = "按名称或描述模糊搜索已通过审核的插件"
            parameters {
                query("q") {
                    description = "搜索关键词"
                    required = false
                }
                query("page") {
                    description = "页码，从1开始"
                    required = false
                }
                query("limit") {
                    description = "每页数量，1-100"
                    required = false
                }
            }
            responses {
                HttpStatusCode.OK {
                    description = "搜索成功"
                    schema = jsonSchema<ApiResponse<PagedPluginResponse>>()
                }
            }
        }

        get("/{pluginId}") {
            val pluginId = PluginId(call.uuidParam("pluginId"))
            val plugin = pluginService.getPlugin(pluginId)
            call.respondOk(plugin.toDetailResponse())
        }.describe {
            tag("插件管理")
            operationId = "getPlugin"
            summary = "获取插件详情"
            description = "获取指定插件的详细信息，包括代码"
            responses {
                HttpStatusCode.OK {
                    description = "获取成功"
                    schema = jsonSchema<ApiResponse<PluginDetailResponse>>()
                }
                HttpStatusCode.NotFound { description = "插件不存在" }
            }
        }

        authenticate("auth-jwt") {
            post {
                val userId = call.requireUserId()
                val request = call.receive<CreatePluginRequest>()
                val plugin = pluginService.createPlugin(
                    authorId = userId,
                    name = request.name,
                    description = request.description,
                    version = request.version,
                    schema = request.schema.toDomain(),
                    code = request.code,
                )
                call.respondOk(plugin.toResponse(), "Plugin created, pending approval")
            }.describe {
                tag("插件管理")
                operationId = "createPlugin"
                summary = "创建插件"
                description = "创建新插件，状态为待审核"
                requestBody {
                    description = "插件信息"
                    schema = jsonSchema<CreatePluginRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "创建成功"
                        schema = jsonSchema<ApiResponse<PluginResponse>>()
                    }
                    HttpStatusCode.Unauthorized { description = "未登录或令牌无效" }
                }
            }

            put("/{pluginId}") {
                val userId = call.requireUserId()
                val pluginId = PluginId(call.uuidParam("pluginId"))
                val request = call.receive<UpdatePluginRequest>()
                val plugin = pluginService.updatePlugin(
                    userId = userId,
                    pluginId = pluginId,
                    name = request.name,
                    description = request.description,
                    version = request.version,
                    schema = request.schema.toDomain(),
                    code = request.code,
                )
                call.respondOk(plugin.toResponse(), "Plugin updated")
            }.describe {
                tag("插件管理")
                operationId = "updatePlugin"
                summary = "更新插件"
                description = "更新指定插件的信息，仅作者可操作"
                requestBody {
                    description = "插件信息"
                    schema = jsonSchema<UpdatePluginRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "更新成功"
                        schema = jsonSchema<ApiResponse<PluginResponse>>()
                    }
                    HttpStatusCode.NotFound { description = "插件不存在" }
                    HttpStatusCode.Unauthorized { description = "未登录或令牌无效" }
                    HttpStatusCode.Forbidden { description = "无权操作" }
                }
            }

            delete("/{pluginId}") {
                val userId = call.requireUserId()
                val pluginId = PluginId(call.uuidParam("pluginId"))
                pluginService.deletePlugin(userId, pluginId)
                call.respond(HttpStatusCode.NoContent)
            }.describe {
                tag("插件管理")
                operationId = "deletePlugin"
                summary = "删除插件"
                description = "删除指定插件，仅作者可操作"
                responses {
                    HttpStatusCode.NoContent { description = "删除成功" }
                    HttpStatusCode.NotFound { description = "插件不存在" }
                    HttpStatusCode.Unauthorized { description = "未登录或令牌无效" }
                    HttpStatusCode.Forbidden { description = "无权操作" }
                }
            }

            post("/{pluginId}/approve") {
                call.requireAdmin()
                val pluginId = PluginId(call.uuidParam("pluginId"))
                val plugin = pluginService.approvePlugin(pluginId)
                call.respondOk(plugin.toResponse(), "Plugin approved")
            }.describe {
                tag("插件管理")
                operationId = "approvePlugin"
                summary = "审批通过插件"
                description = "将指定插件状态设为已通过（需要管理员权限）"
                responses {
                    HttpStatusCode.OK {
                        description = "审批成功"
                        schema = jsonSchema<ApiResponse<PluginResponse>>()
                    }
                    HttpStatusCode.NotFound { description = "插件不存在" }
                    HttpStatusCode.Unauthorized { description = "未登录或令牌无效" }
                    HttpStatusCode.Forbidden { description = "需要管理员权限" }
                }
            }

            post("/{pluginId}/reject") {
                call.requireAdmin()
                val pluginId = PluginId(call.uuidParam("pluginId"))
                val plugin = pluginService.rejectPlugin(pluginId)
                call.respondOk(plugin.toResponse(), "Plugin rejected")
            }.describe {
                tag("插件管理")
                operationId = "rejectPlugin"
                summary = "审批拒绝插件"
                description = "将指定插件状态设为已拒绝（需要管理员权限）"
                responses {
                    HttpStatusCode.OK {
                        description = "审批成功"
                        schema = jsonSchema<ApiResponse<PluginResponse>>()
                    }
                    HttpStatusCode.NotFound { description = "插件不存在" }
                    HttpStatusCode.Unauthorized { description = "未登录或令牌无效" }
                    HttpStatusCode.Forbidden { description = "需要管理员权限" }
                }
            }

            post("/{pluginId}/like") {
                val userId = call.requireUserId()
                val pluginId = PluginId(call.uuidParam("pluginId"))
                pluginService.likePlugin(userId, pluginId)
                call.respondOk("Liked successfully")
            }.describe {
                tag("插件管理")
                operationId = "likePlugin"
                summary = "点赞插件"
                description = "为指定插件点赞"
                responses {
                    HttpStatusCode.OK { description = "点赞成功" }
                    HttpStatusCode.NotFound { description = "插件不存在" }
                    HttpStatusCode.Conflict { description = "已经点赞过" }
                    HttpStatusCode.Unauthorized { description = "未登录或令牌无效" }
                }
            }

            delete("/{pluginId}/like") {
                val userId = call.requireUserId()
                val pluginId = PluginId(call.uuidParam("pluginId"))
                pluginService.unlikePlugin(userId, pluginId)
                call.respond(HttpStatusCode.NoContent)
            }.describe {
                tag("插件管理")
                operationId = "unlikePlugin"
                summary = "取消点赞"
                description = "取消对指定插件的点赞"
                responses {
                    HttpStatusCode.NoContent { description = "取消成功" }
                    HttpStatusCode.NotFound { description = "点赞记录不存在" }
                    HttpStatusCode.Unauthorized { description = "未登录或令牌无效" }
                }
            }
        }
    }

    authenticate("auth-jwt") {
        route("/me/plugins") {
            get {
                val userId = call.requireUserId()
                val page = call.request.queryParameters["page"]?.let { validatePage(it) } ?: 1
                val limit = call.request.queryParameters["limit"]?.let { validateLimit(it) } ?: 10
                val result = pluginService.listMyPlugins(userId, page, limit)
                call.respondOk(result.toResponse())
            }.describe {
                tag("插件管理")
                operationId = "listMyPlugins"
                summary = "获取我的插件"
                description = "获取当前用户创建的所有插件"
                parameters {
                    query("page") {
                        description = "页码，从1开始"
                        required = false
                    }
                    query("limit") {
                        description = "每页数量，1-100"
                        required = false
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "获取成功"
                        schema = jsonSchema<ApiResponse<PagedPluginResponse>>()
                    }
                    HttpStatusCode.Unauthorized { description = "未登录或令牌无效" }
                }
            }
        }
    }

    authenticate("auth-jwt") {
        route("/admin/plugins") {
            get("/pending") {
                call.requireAdmin()
                val page = call.request.queryParameters["page"]?.let { validatePage(it) } ?: 1
                val limit = call.request.queryParameters["limit"]?.let { validateLimit(it) } ?: 10
                val result = pluginService.listPendingPlugins(page, limit)
                call.respondOk(result.toResponse())
            }.describe {
                tag("插件管理")
                operationId = "listPendingPlugins"
                summary = "获取待审核插件列表"
                description = "获取所有待审核的插件列表（需要管理员权限）"
                responses {
                    HttpStatusCode.OK {
                        description = "获取成功"
                        schema = jsonSchema<ApiResponse<PagedPluginResponse>>()
                    }
                    HttpStatusCode.Unauthorized { description = "未登录或令牌无效" }
                    HttpStatusCode.Forbidden { description = "需要管理员权限" }
                }
            }
        }
    }
}
