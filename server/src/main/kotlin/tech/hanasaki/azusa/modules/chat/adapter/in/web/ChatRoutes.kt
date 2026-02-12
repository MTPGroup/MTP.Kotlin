package tech.hanasaki.azusa.modules.chat.adapter.`in`.web

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import tech.hanasaki.azusa.modules.chat.adapter.`in`.web.dto.*
import tech.hanasaki.azusa.modules.chat.application.port.`in`.AgentStreamEvent
import tech.hanasaki.azusa.modules.chat.application.port.`in`.AgentUseCasePort
import tech.hanasaki.azusa.modules.chat.application.port.`in`.ChatConfigUseCasePort
import tech.hanasaki.azusa.modules.chat.application.port.`in`.ChatUseCasePort
import tech.hanasaki.azusa.modules.chat.domain.model.ChatId
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.PluginId
import tech.hanasaki.azusa.shared.infrastructure.web.response.ApiResponse
import tech.hanasaki.azusa.shared.infrastructure.web.response.respondOk
import tech.hanasaki.azusa.shared.infrastructure.web.route.requireUserId
import tech.hanasaki.azusa.shared.infrastructure.web.route.uuidParam
import tech.hanasaki.azusa.shared.infrastructure.web.validation.validateLimit
import tech.hanasaki.azusa.shared.infrastructure.web.validation.validatePage

fun Route.chatRoutes() {
    val chatService: ChatUseCasePort by inject()
    val chatConfigService: ChatConfigUseCasePort by inject()
    val agentService: AgentUseCasePort by inject()

    authenticate("auth-jwt") {
        route("/chats") {
            // 创建会话
            post {
                val userId = call.requireUserId()
                val request = call.receive<CreateChatRequest>()
                val chat = chatService.createChat(
                    userId = userId,
                    characterId = CharacterId(request.characterId),
                    name = request.name,
                )
                call.respondOk(chat.toResponse(), "会话创建成功", HttpStatusCode.Created)
            }.describe {
                tag("会话管理")
                operationId = "createChat"
                summary = "创建会话"
                description = "创建一个新的私聊会话，需要指定对话的角色ID"
                requestBody {
                    description = "会话信息"
                    schema = jsonSchema<CreateChatRequest>()
                }
                responses {
                    HttpStatusCode.Created {
                        description = "创建成功"
                        schema = jsonSchema<ApiResponse<ChatResponse>>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "输入参数无效"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }

            // 获取会话列表
            get {
                val userId = call.requireUserId()
                val page = call.request.queryParameters["page"]?.let { validatePage(it) } ?: 1
                val limit = call.request.queryParameters["limit"]?.let { validateLimit(it) } ?: 10
                val result = chatService.listChats(userId, page, limit)
                call.respondOk(result.toResponse())
            }.describe {
                tag("会话管理")
                operationId = "listChats"
                summary = "获取会话列表"
                description = "获取当前用户的所有会话（分页）"
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
                        schema = jsonSchema<ApiResponse<PagedChatResponse>>()
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }

            // 获取会话详情
            get("/{chatId}") {
                val userId = call.requireUserId()
                val chatId = ChatId(call.uuidParam("chatId"))
                val chat = chatService.getChat(userId, chatId)
                call.respondOk(chat.toResponse())
            }.describe {
                tag("会话管理")
                operationId = "getChat"
                summary = "获取会话详情"
                description = "获取指定会话的详细信息，需要是会话的所有者"
                parameters {
                    path("chatId") {
                        description = "会话ID（UUID格式）"
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "获取成功"
                        schema = jsonSchema<ApiResponse<ChatResponse>>()
                    }
                    HttpStatusCode.NotFound {
                        description = "会话不存在"
                    }
                    HttpStatusCode.Forbidden {
                        description = "权限不足"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }

            // 更新会话名称
            put("/{chatId}/name") {
                val userId = call.requireUserId()
                val chatId = ChatId(call.uuidParam("chatId"))
                val request = call.receive<UpdateChatNameRequest>()
                chatService.updateChatName(userId, chatId, request.name)
                call.respondOk(Unit, "会话名称更新成功")
            }.describe {
                tag("会话管理")
                operationId = "updateChatName"
                summary = "更新会话名称"
                description = "更新指定会话的名称，需要是会话的所有者"
                parameters {
                    path("chatId") {
                        description = "会话ID（UUID格式）"
                    }
                }
                requestBody {
                    description = "新的会话名称"
                    schema = jsonSchema<UpdateChatNameRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "更新成功"
                    }
                    HttpStatusCode.NotFound {
                        description = "会话不存在"
                    }
                    HttpStatusCode.Forbidden {
                        description = "权限不足"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }

            // 删除会话
            delete("/{chatId}") {
                val userId = call.requireUserId()
                val chatId = ChatId(call.uuidParam("chatId"))
                chatService.deleteChat(userId, chatId)
                call.respond(HttpStatusCode.NoContent)
            }.describe {
                tag("会话管理")
                operationId = "deleteChat"
                summary = "删除会话"
                description = "永久删除指定会话及其所有消息、配置和插件订阅，需要是会话的所有者"
                parameters {
                    path("chatId") {
                        description = "会话ID（UUID格式）"
                    }
                }
                responses {
                    HttpStatusCode.NoContent {
                        description = "删除成功"
                    }
                    HttpStatusCode.NotFound {
                        description = "会话不存在"
                    }
                    HttpStatusCode.Forbidden {
                        description = "权限不足"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }

            // 获取历史消息
            get("/{chatId}/messages") {
                val userId = call.requireUserId()
                val chatId = ChatId(call.uuidParam("chatId"))
                val page = call.request.queryParameters["page"]?.let { validatePage(it) } ?: 1
                val limit = call.request.queryParameters["limit"]?.let { validateLimit(it) } ?: 10
                val result = chatService.getMessages(userId, chatId, page, limit)
                call.respondOk(result.toMessageResponse())
            }.describe {
                tag("消息")
                operationId = "listMessages"
                summary = "获取历史消息"
                description = "获取指定会话的历史消息（分页），需要是会话的所有者"
                parameters {
                    path("chatId") {
                        description = "会话ID（UUID格式）"
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
                        description = "获取成功"
                        schema = jsonSchema<ApiResponse<PagedMessageResponse>>()
                    }
                    HttpStatusCode.NotFound {
                        description = "会话不存在"
                    }
                    HttpStatusCode.Forbidden {
                        description = "权限不足"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }

            // 发送消息（SSE 流式响应）
            post("/{chatId}/messages") {
                val userId = call.requireUserId()
                val chatId = ChatId(call.uuidParam("chatId"))
                val request = call.receive<SendMessageRequest>()
                val content = request.content.map { it.toDomain() }

                call.response.header(HttpHeaders.CacheControl, "no-cache")
                call.response.header(HttpHeaders.Connection, "keep-alive")
                call.respondTextWriter(contentType = ContentType.Text.EventStream) {
                    agentService.processMessage(userId, chatId, content).collect { event ->
                        val (type, data) = when (event) {
                            is AgentStreamEvent.Delta -> "delta" to event.text
                            is AgentStreamEvent.ToolCallStart -> "tool_call_start" to Json.encodeToString(
                                ToolCallStartData.serializer(),
                                ToolCallStartData(event.name, event.arguments),
                            )
                            is AgentStreamEvent.ToolCallResult -> "tool_call_result" to Json.encodeToString(
                                ToolCallResultData.serializer(),
                                ToolCallResultData(event.name, event.result),
                            )
                            is AgentStreamEvent.Done -> "done" to event.fullContent
                            is AgentStreamEvent.Error -> "error" to event.message
                        }
                        write("event: $type\ndata: $data\n\n")
                        flush()
                    }
                }
            }.describe {
                tag("消息")
                operationId = "sendMessage"
                summary = "发送消息"
                description = "向指定会话发送消息并获取 AI 回复，响应为 SSE (Server-Sent Events) 流式格式。" +
                        "事件类型包括：delta（增量文本）、tool_call_start（工具调用开始）、" +
                        "tool_call_result（工具调用结果）、done（完成）、error（错误）"
                parameters {
                    path("chatId") {
                        description = "会话ID（UUID格式）"
                    }
                }
                requestBody {
                    description = "消息内容，支持多模态（文本、图片、文件、代码）"
                    schema = jsonSchema<SendMessageRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "SSE 流式响应"
                    }
                    HttpStatusCode.NotFound {
                        description = "会话不存在"
                    }
                    HttpStatusCode.Forbidden {
                        description = "权限不足"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }

            // 获取会话配置
            get("/{chatId}/config") {
                val userId = call.requireUserId()
                val chatId = ChatId(call.uuidParam("chatId"))
                val config = chatConfigService.getConfig(userId, chatId)
                call.respondOk(config?.toResponse())
            }.describe {
                tag("会话配置")
                operationId = "getChatConfig"
                summary = "获取会话配置"
                description = "获取指定会话的 LLM 配置参数，需要是会话的所有者"
                parameters {
                    path("chatId") {
                        description = "会话ID（UUID格式）"
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "获取成功"
                        schema = jsonSchema<ApiResponse<ChatConfigResponse>>()
                    }
                    HttpStatusCode.NotFound {
                        description = "会话不存在"
                    }
                    HttpStatusCode.Forbidden {
                        description = "权限不足"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }

            // 更新会话配置
            put("/{chatId}/config") {
                val userId = call.requireUserId()
                val chatId = ChatId(call.uuidParam("chatId"))
                val request = call.receive<UpdateChatConfigRequest>()
                val config = chatConfigService.updateConfig(
                    userId = userId,
                    chatId = chatId,
                    temperature = request.temperature,
                    maxTokens = request.maxTokens,
                    topP = request.topP,
                    systemPrompt = request.systemPrompt,
                )
                call.respondOk(config.toResponse(), "配置更新成功")
            }.describe {
                tag("会话配置")
                operationId = "updateChatConfig"
                summary = "更新会话配置"
                description = "更新指定会话的 LLM 配置参数（temperature、maxTokens、topP、systemPrompt），需要是会话的所有者"
                parameters {
                    path("chatId") {
                        description = "会话ID（UUID格式）"
                    }
                }
                requestBody {
                    description = "配置参数"
                    schema = jsonSchema<UpdateChatConfigRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "更新成功"
                        schema = jsonSchema<ApiResponse<ChatConfigResponse>>()
                    }
                    HttpStatusCode.NotFound {
                        description = "会话不存在"
                    }
                    HttpStatusCode.Forbidden {
                        description = "权限不足"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }

            // 获取插件订阅列表
            get("/{chatId}/plugins") {
                val userId = call.requireUserId()
                val chatId = ChatId(call.uuidParam("chatId"))
                val subscriptions = chatConfigService.getPluginSubscriptions(userId, chatId)
                call.respondOk(subscriptions.map { it.toResponse() })
            }.describe {
                tag("插件订阅")
                operationId = "listChatPlugins"
                summary = "获取插件订阅列表"
                description = "获取指定会话的所有插件订阅，需要是会话的所有者"
                parameters {
                    path("chatId") {
                        description = "会话ID（UUID格式）"
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "获取成功"
                        schema = jsonSchema<ApiResponse<List<PluginSubscriptionResponse>>>()
                    }
                    HttpStatusCode.NotFound {
                        description = "会话不存在"
                    }
                    HttpStatusCode.Forbidden {
                        description = "权限不足"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }

            // 启用/禁用插件
            put("/{chatId}/plugins/{pluginId}") {
                val userId = call.requireUserId()
                val chatId = ChatId(call.uuidParam("chatId"))
                val pluginId = PluginId(call.uuidParam("pluginId"))
                val request = call.receive<TogglePluginRequest>()
                val subscription = chatConfigService.togglePlugin(userId, chatId, pluginId, request.enabled)
                call.respondOk(subscription.toResponse())
            }.describe {
                tag("插件订阅")
                operationId = "toggleChatPlugin"
                summary = "启用/禁用插件"
                description = "为指定会话启用或禁用一个插件，如果订阅不存在则自动创建"
                parameters {
                    path("chatId") {
                        description = "会话ID（UUID格式）"
                    }
                    path("pluginId") {
                        description = "插件ID（UUID格式）"
                    }
                }
                requestBody {
                    description = "启用/禁用状态"
                    schema = jsonSchema<TogglePluginRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "操作成功"
                        schema = jsonSchema<ApiResponse<PluginSubscriptionResponse>>()
                    }
                    HttpStatusCode.NotFound {
                        description = "会话或插件不存在"
                    }
                    HttpStatusCode.Forbidden {
                        description = "权限不足"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }

            // 更新插件配置
            put("/{chatId}/plugins/{pluginId}/config") {
                val userId = call.requireUserId()
                val chatId = ChatId(call.uuidParam("chatId"))
                val pluginId = PluginId(call.uuidParam("pluginId"))
                val request = call.receive<UpdatePluginConfigRequest>()
                val subscription = chatConfigService.updatePluginConfig(userId, chatId, pluginId, request.config)
                call.respondOk(subscription.toResponse(), "插件配置更新成功")
            }.describe {
                tag("插件订阅")
                operationId = "updateChatPluginConfig"
                summary = "更新插件配置"
                description = "更新指定会话中某个插件的配置参数，插件订阅必须已存在"
                parameters {
                    path("chatId") {
                        description = "会话ID（UUID格式）"
                    }
                    path("pluginId") {
                        description = "插件ID（UUID格式）"
                    }
                }
                requestBody {
                    description = "插件配置（JSON对象）"
                    schema = jsonSchema<UpdatePluginConfigRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "更新成功"
                        schema = jsonSchema<ApiResponse<PluginSubscriptionResponse>>()
                    }
                    HttpStatusCode.NotFound {
                        description = "会话或插件订阅不存在"
                    }
                    HttpStatusCode.Forbidden {
                        description = "权限不足"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }
        }
    }
}
