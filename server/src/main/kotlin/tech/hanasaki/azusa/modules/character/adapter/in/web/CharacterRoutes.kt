package tech.hanasaki.azusa.modules.character.adapter.`in`.web

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.openapi.*
import io.ktor.openapi.ReferenceOr.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import org.koin.ktor.ext.inject
import tech.hanasaki.azusa.modules.character.adapter.`in`.web.dto.*
import tech.hanasaki.azusa.modules.character.application.port.`in`.CharacterUseCasePort
import tech.hanasaki.azusa.shared.domain.model.vo.AvatarUrl
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.infrastructure.web.response.ApiResponse
import tech.hanasaki.azusa.shared.infrastructure.web.response.respondOk
import tech.hanasaki.azusa.shared.infrastructure.web.route.optionalUserId
import tech.hanasaki.azusa.shared.infrastructure.web.route.requireUserId
import tech.hanasaki.azusa.shared.infrastructure.web.route.uuidParam
import tech.hanasaki.azusa.shared.infrastructure.web.validation.validateLimit
import tech.hanasaki.azusa.shared.infrastructure.web.validation.validatePage
import tech.hanasaki.azusa.shared.port.out.FileStoragePort
import kotlin.uuid.Uuid

@OptIn(InternalAPI::class)
fun Route.characterRoutes() {
    val characterService: CharacterUseCasePort by inject()
    val fileStoragePort: FileStoragePort by inject()

    route("/characters") {
        // 获取公开角色列表（分页）
        get("/public") {
            val page = call.request.queryParameters["page"]?.let { validatePage(it) } ?: 1
            val limit = call.request.queryParameters["limit"]?.let { validateLimit(it) } ?: 10
            val result = characterService.listPublicCharacters(page, limit)
            call.respondOk(result.toResponse())
        }.describe {
            tag("角色管理")
            operationId = "listPublicCharacters"
            summary = "获取公开角色列表"
            description = "获取所有公开角色（分页）"
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
                    schema = jsonSchema<ApiResponse<PagedCharacterResponse>>()
                }
            }
        }

        get("/search") {
            val userId = call.optionalUserId()
            val query = call.request.queryParameters["q"] ?: ""
            val page = call.request.queryParameters["page"]?.let { validatePage(it) } ?: 1
            val limit = call.request.queryParameters["limit"]?.let { validateLimit(it) } ?: 10
            val result = characterService.searchCharacters(query, page, limit, userId)
            call.respondOk(result.toResponse())
        }.describe {
            tag("角色管理")
            operationId = "searchCharacters"
            summary = "搜索角色"
            description = "按名称搜索角色，返回公开角色及自己的私有角色"
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
                    schema = jsonSchema<ApiResponse<PagedCharacterResponse>>()
                }
                HttpStatusCode.Unauthorized {
                    description = "未登录或令牌无效"
                }
            }
        }

        // 获取角色详情（公开角色无需认证，私有角色需要是作者）
        get("/{characterId}") {
            val userId = call.optionalUserId()
            val characterId = CharacterId(call.uuidParam("characterId"))
            val character = characterService.getCharacter(userId, characterId)
            call.respondOk(character.toResponse())
        }.describe {
            tag("角色管理")
            operationId = "getCharacter"
            summary = "获取角色详情"
            description = "获取指定角色的详细信息，公开角色无需认证，私有角色需要是作者"
            parameters {
                path("characterId") {
                    description = "角色ID（UUID格式）"
                }
            }
            responses {
                HttpStatusCode.OK {
                    description = "获取成功"
                    schema = jsonSchema<ApiResponse<CharacterResponse>>()
                }
                HttpStatusCode.NotFound {
                    description = "角色不存在"
                }
                HttpStatusCode.Forbidden {
                    description = "权限不足"
                }
            }
        }

        authenticate("auth-jwt") {
            // 获取我的角色列表
            get {
                val userId = call.requireUserId()
                val page = call.request.queryParameters["page"]?.let { validatePage(it) } ?: 1
                val limit = call.request.queryParameters["limit"]?.let { validateLimit(it) } ?: 10

                val result = characterService.listMyCharacters(userId, page, limit)
                call.respondOk(result.toResponse())
            }.describe {
                tag("角色管理")
                operationId = "listMyCharacters"
                summary = "获取我的角色列表"
                description = "获取当前登录用户创建的所有角色（分页）"
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
                        schema = jsonSchema<ApiResponse<PagedCharacterResponse>>()
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }


            // 创建角色
            post {
                val userId = call.requireUserId()
                val request = call.receive<CreateCharacterRequest>()
                val character = characterService.createCharacter(
                    authorId = userId,
                    name = request.name,
                    avatar = request.avatar?.let { AvatarUrl(it) },
                    bio = request.bio,
                    originPrompt = request.originPrompt,
                    isPublic = request.isPublic
                )
                call.respondOk(character.toResponse(), "角色创建成功", HttpStatusCode.Created)
            }.describe {
                tag("角色管理")
                operationId = "createCharacter"
                summary = "创建角色"
                description = "创建一个新的AI角色"
                requestBody {
                    description = "角色信息"
                    schema = jsonSchema<CreateCharacterRequest>()
                }
                responses {
                    HttpStatusCode.Created {
                        description = "创建成功"
                        schema = jsonSchema<ApiResponse<CharacterResponse>>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "输入参数无效"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }

            // 更新角色
            put("/{characterId}") {
                val userId = call.requireUserId()
                val characterId = CharacterId(call.uuidParam("characterId"))
                val request = call.receive<UpdateCharacterRequest>()
                val character = characterService.updateCharacter(
                    userId = userId,
                    characterId = characterId,
                    name = request.name,
                    avatar = request.avatar?.let { AvatarUrl(it) },
                    bio = request.bio,
                    originPrompt = request.originPrompt,
                    isPublic = request.isPublic
                )
                call.respondOk(character.toResponse(), "角色更新成功")
            }.describe {
                tag("角色管理")
                operationId = "updateCharacter"
                summary = "更新角色"
                description = "更新指定角色的信息，需要是角色的作者"
                parameters {
                    path("characterId") {
                        description = "角色ID（UUID格式）"
                    }
                }
                requestBody {
                    description = "更新的角色信息"
                    schema = jsonSchema<UpdateCharacterRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "更新成功"
                        schema = jsonSchema<ApiResponse<CharacterResponse>>()
                    }
                    HttpStatusCode.NotFound {
                        description = "角色不存在"
                    }
                    HttpStatusCode.Forbidden {
                        description = "权限不足"
                    }
                    HttpStatusCode.BadRequest {
                        description = "输入参数无效"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }

            // 删除角色
            delete("/{characterId}") {
                val userId = call.requireUserId()
                val characterId = CharacterId(call.uuidParam("characterId"))
                characterService.deleteCharacter(userId, characterId)
                call.respond(HttpStatusCode.NoContent)
            }.describe {
                tag("角色管理")
                operationId = "deleteCharacter"
                summary = "删除角色"
                description = "永久删除指定角色，需要是角色的作者"
                parameters {
                    path("characterId") {
                        description = "角色ID（UUID格式）"
                    }
                }
                responses {
                    HttpStatusCode.NoContent {
                        description = "删除成功"
                    }
                    HttpStatusCode.NotFound {
                        description = "角色不存在"
                    }
                    HttpStatusCode.Forbidden {
                        description = "权限不足"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }

            // 上传角色头像
            put("/{characterId}/avatar") {
                val userId = call.requireUserId()
                val characterId = CharacterId(call.uuidParam("characterId"))

                val multipart = call.receiveMultipart()
                var contentType: String? = null
                var fileBytes: ByteArray? = null
                var fileName: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            fileName = part.originalFileName
                            contentType = part.contentType?.toString()
                            fileBytes = part.provider().toInputStream().readBytes()
                        }
                        else -> {}
                    }
                    part.dispose()
                }

                val bytes = fileBytes ?: throw IllegalArgumentException("未上传文件")
                val mime = contentType ?: "image/png"
                val ext = (fileName ?: "avatar").substringAfterLast('.', "png")
                val objectKey = "characters/${characterId.value}/${Uuid.random()}.$ext"

                fileStoragePort.upload(objectKey, mime, bytes)
                val avatarUrl = AvatarUrl(fileStoragePort.publicUrl(objectKey))
                val character = characterService.updateCharacterAvatar(userId, characterId, avatarUrl)
                call.respondOk(character.toResponse(), "头像上传成功")
            }.describe {
                tag("角色管理")
                operationId = "uploadCharacterAvatar"
                summary = "上传角色头像"
                description = "通过 multipart/form-data 上传角色头像，需要是角色的作者"
                parameters {
                    path("characterId") {
                        description = "角色ID（UUID格式）"
                    }
                }
                requestBody {
                    description = "头像文件"
                    required = true
                    ContentType.MultiPart.FormData {
                        schema = JsonSchema(
                            type = JsonType.OBJECT,
                            properties = mapOf(
                                "file" to Value(
                                    JsonSchema(
                                        type = JsonType.STRING,
                                        format = "binary",
                                        description = "头像图片文件"
                                    )
                                )
                            ),
                            required = listOf("file")
                        )
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "上传成功"
                        schema = jsonSchema<ApiResponse<CharacterResponse>>()
                    }
                    HttpStatusCode.NotFound {
                        description = "角色不存在"
                    }
                    HttpStatusCode.Forbidden {
                        description = "权限不足"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }

            // 获取角色订阅的知识库列表
            get("/{characterId}/knowledge-bases") {
                val userId = call.requireUserId()
                val characterId = CharacterId(call.uuidParam("characterId"))
                characterService.getCharacter(userId, characterId) // 权限校验
                val subscriptions = characterService.getKnowledgeSubscriptions(characterId)
                call.respondOk(subscriptions.toResponse())
            }.describe {
                tag("角色管理")
                operationId = "listCharacterKnowledgeBases"
                summary = "获取角色关联的知识库"
                description = "获取指定角色订阅的所有知识库列表，需要是角色的作者"
                parameters {
                    path("characterId") {
                        description = "角色ID（UUID格式）"
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "获取成功"
                        schema = jsonSchema<ApiResponse<List<KnowledgeSubscriptionResponse>>>()
                    }
                    HttpStatusCode.NotFound {
                        description = "角色不存在"
                    }
                    HttpStatusCode.Forbidden {
                        description = "权限不足"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
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
                call.respondOk(SuccessResponse(), "知识库已连接")
            }.describe {
                tag("角色管理")
                operationId = "subscribeKnowledgeBase"
                summary = "关联知识库到角色"
                description = "将指定知识库关联到角色，需要是角色的作者"
                parameters {
                    path("characterId") {
                        description = "角色ID（UUID格式）"
                    }
                }
                requestBody {
                    description = "知识库关联信息"
                    schema = jsonSchema<SubscribeKnowledgeBaseRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "关联成功"
                        schema = jsonSchema<ApiResponse<SuccessResponse>>()
                    }
                    HttpStatusCode.NotFound {
                        description = "角色不存在"
                    }
                    HttpStatusCode.Forbidden {
                        description = "权限不足"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }

            // 解除角色与知识库的链接
            delete("/{characterId}/knowledge-bases/{knowledgeBaseId}") {
                val userId = call.requireUserId()
                val characterId = CharacterId(call.uuidParam("characterId"))
                val knowledgeBaseId = KnowledgeBaseId(call.uuidParam("knowledgeBaseId"))
                characterService.unsubscribeKnowledgeBase(userId, characterId, knowledgeBaseId)
                call.respond(HttpStatusCode.NoContent)
            }.describe {
                tag("角色管理")
                operationId = "unsubscribeKnowledgeBase"
                summary = "解除角色与知识库的关联"
                description = "移除角色与指定知识库的关联关系，需要是角色的作者"
                parameters {
                    path("characterId") {
                        description = "角色ID（UUID格式）"
                    }
                    path("knowledgeBaseId") {
                        description = "知识库ID（UUID格式）"
                    }
                }
                responses {
                    HttpStatusCode.NoContent {
                        description = "解除关联成功"
                    }
                    HttpStatusCode.NotFound {
                        description = "角色不存在"
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
