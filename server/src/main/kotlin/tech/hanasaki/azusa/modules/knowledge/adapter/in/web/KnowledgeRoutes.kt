package tech.hanasaki.azusa.modules.knowledge.adapter.`in`.web

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.openapi.*
import io.ktor.openapi.ReferenceOr.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.jvm.javaio.*
import org.koin.ktor.ext.inject
import tech.hanasaki.azusa.modules.knowledge.adapter.`in`.web.dto.*
import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.KnowledgeBaseQueryUseCasePort
import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.KnowledgeBaseUseCasePort
import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.KnowledgeFileUseCasePort
import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.KnowledgeSearchUseCasePort
import tech.hanasaki.azusa.shared.domain.exception.ValidationException
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeFileId
import tech.hanasaki.azusa.shared.infrastructure.web.response.ApiResponse
import tech.hanasaki.azusa.shared.infrastructure.web.response.respondOk
import tech.hanasaki.azusa.shared.infrastructure.web.route.optionalUserId
import tech.hanasaki.azusa.shared.infrastructure.web.route.requireUserId
import tech.hanasaki.azusa.shared.infrastructure.web.route.uuidParam
import tech.hanasaki.azusa.shared.infrastructure.web.validation.ValidationCollector
import tech.hanasaki.azusa.shared.infrastructure.web.validation.collectLimit
import tech.hanasaki.azusa.shared.infrastructure.web.validation.collectPage
import tech.hanasaki.azusa.shared.port.out.FileStoragePort
import kotlin.uuid.Uuid

fun Route.knowledgeRoutes() {
    val knowledgeBaseService: KnowledgeBaseUseCasePort by inject()
    val knowledgeBaseQueryService: KnowledgeBaseQueryUseCasePort by inject()
    val knowledgeFileService: KnowledgeFileUseCasePort by inject()
    val knowledgeSearchService: KnowledgeSearchUseCasePort by inject()
    val fileStoragePort: FileStoragePort by inject()

    // 公开知识库（无需认证）
    route("/knowledge-bases") {
        // 获取公开知识库列表
        get("/public") {
            val validator = ValidationCollector()
            val page = validator.collectPage(call.request.queryParameters["page"]) ?: 1
            val limit = validator.collectLimit(call.request.queryParameters["limit"]) ?: 10
            validator.throwIfAny()

            val result = knowledgeBaseQueryService.listPublicKnowledgeBases(page, limit)
            call.respondOk(result.toResponse())
        }.describe {
            tag("知识库管理")
            operationId = "listPublicKnowledgeBases"
            summary = "获取公开知识库列表"
            description = "获取所有公开的知识库列表，支持分页"
            parameters {
                query("page") {
                    description = "页码（从1开始）"
                    required = false
                }
                query("limit") {
                    description = "每页数量"
                    required = false
                }
            }
            responses {
                HttpStatusCode.OK {
                    description = "获取成功"
                    schema = jsonSchema<ApiResponse<PagedKnowledgeBaseResponse>>()
                }
            }
        }

        // 搜索公开知识库
        get("/search") {
            val query = call.request.queryParameters["q"] ?: ""
            val validator = ValidationCollector()
            val page = validator.collectPage(call.request.queryParameters["page"]) ?: 1
            val limit = validator.collectLimit(call.request.queryParameters["limit"]) ?: 10
            validator.throwIfAny()

            val result = knowledgeBaseQueryService.searchKnowledgeBases(query, page, limit)
            call.respondOk(result.toResponse())
        }.describe {
            tag("知识库管理")
            operationId = "searchKnowledgeBases"
            summary = "搜索知识库"
            description = "根据关键词搜索知识库名称和描述，支持分页"
            parameters {
                query("q") {
                    description = "搜索关键词"
                    required = false
                }
                query("page") {
                    description = "页码（从1开始）"
                    required = false
                }
                query("limit") {
                    description = "每页数量"
                    required = false
                }
            }
            responses {
                HttpStatusCode.OK {
                    description = "搜索成功"
                    schema = jsonSchema<ApiResponse<PagedKnowledgeBaseResponse>>()
                }
            }
        }

        authenticate("auth-jwt", optional = true) {
            // 获取知识库详情（公开知识库无需认证，私有知识库需要是所有者）
            get("/{knowledgeBaseId}") {
                val userId = call.optionalUserId()
                val knowledgeBaseId = KnowledgeBaseId(call.uuidParam("knowledgeBaseId"))
                val kb = knowledgeBaseQueryService.getKnowledgeBase(userId, knowledgeBaseId)
                call.respondOk(kb.toResponse())
            }.describe {
                tag("知识库管理")
                operationId = "getKnowledgeBase"
                summary = "获取知识库详情"
                description = "获取指定知识库的详细信息，公开知识库无需认证，私有知识库需要是所有者"
                parameters {
                    path("knowledgeBaseId") {
                        description = "知识库ID"
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "获取成功"
                        schema = jsonSchema<ApiResponse<KnowledgeBaseResponse>>()
                    }
                    HttpStatusCode.NotFound {
                        description = "知识库不存在"
                    }
                    HttpStatusCode.Forbidden {
                        description = "无权访问该知识库"
                    }
                }
            }

            // 获取知识库统计（公开知识库无需认证，私有知识库需要是所有者）
            get("/{knowledgeBaseId}/stats") {
                val userId = call.optionalUserId()
                val knowledgeBaseId = KnowledgeBaseId(call.uuidParam("knowledgeBaseId"))
                val stats = knowledgeBaseService.getKnowledgeBaseStats(userId, knowledgeBaseId)
                call.respondOk(stats.toResponse())
            }.describe {
                tag("知识库管理")
                operationId = "getKnowledgeBaseStats"
                summary = "获取知识库统计"
                description = "获取指定知识库的文件数量和文档数量统计，公开知识库无需认证，私有知识库需要是所有者"
                parameters {
                    path("knowledgeBaseId") {
                        description = "知识库ID"
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "获取成功"
                        schema = jsonSchema<ApiResponse<KnowledgeBaseStatsResponse>>()
                    }
                    HttpStatusCode.NotFound {
                        description = "知识库不存在"
                    }
                    HttpStatusCode.Forbidden {
                        description = "无权访问该知识库"
                    }
                }
            }

            // 获取知识库的文件列表（公开知识库无需认证，私有知识库需要是所有者）
            get("/{knowledgeBaseId}/files") {
                val userId = call.optionalUserId()
                val knowledgeBaseId = KnowledgeBaseId(call.uuidParam("knowledgeBaseId"))
                val files = knowledgeFileService.listFiles(userId, knowledgeBaseId)
                call.respondOk(files.map { it.toResponse() })
            }.describe {
                tag("知识库文件管理")
                operationId = "listKnowledgeFiles"
                summary = "获取文件列表"
                description = "获取指定知识库中的所有文件列表，公开知识库无需认证，私有知识库需要是所有者"
                parameters {
                    path("knowledgeBaseId") {
                        description = "知识库ID"
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "获取成功"
                        schema = jsonSchema<ApiResponse<Array<KnowledgeFileResponse>>>()
                    }
                    HttpStatusCode.NotFound {
                        description = "知识库不存在"
                    }
                    HttpStatusCode.Forbidden {
                        description = "无权访问该知识库"
                    }
                }
            }
        }
    }

    // 公开知识库搜索（无需认证）
    route("/knowledge") {
        post("/search/public") {
            val request = call.receive<SearchKnowledgeRequest>()
            val results = knowledgeSearchService.searchPublicKnowledgeBases(
                query = request.query,
                threshold = request.threshold,
                limit = request.limit,
            )
            call.respondOk(results.map { it.toResponse() })
        }.describe {
            tag("知识库搜索")
            operationId = "searchPublicKnowledge"
            summary = "搜索公开知识库"
            description = "在所有公开的知识库中进行向量相似度搜索"
            requestBody {
                description = "搜索请求"
                schema = jsonSchema<SearchKnowledgeRequest>()
            }
            responses {
                HttpStatusCode.OK {
                    description = "搜索成功"
                    schema = jsonSchema<ApiResponse<Array<SearchResultResponse>>>()
                }
            }
        }
    }

    authenticate("auth-jwt") {
        route("/knowledge-bases") {
            // 获取我的知识库列表
            get {
                val userId = call.requireUserId()
                val validator = ValidationCollector()
                val page = validator.collectPage(call.request.queryParameters["page"]) ?: 1
                val limit = validator.collectLimit(call.request.queryParameters["limit"]) ?: 10
                validator.throwIfAny()

                val result = knowledgeBaseQueryService.listMyKnowledgeBases(userId, page, limit)
                call.respondOk(result.toResponse())
            }.describe {
                tag("知识库管理")
                operationId = "listMyKnowledgeBases"
                summary = "获取我的知识库列表"
                description = "获取当前用户创建的所有知识库列表，支持分页"
                parameters {
                    query("page") {
                        description = "页码（从1开始）"
                        required = false
                    }
                    query("limit") {
                        description = "每页数量"
                        required = false
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "获取成功"
                        schema = jsonSchema<ApiResponse<PagedKnowledgeBaseResponse>>()
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }

            // 创建知识库
            post {
                val userId = call.requireUserId()
                val request = call.receive<CreateKnowledgeBaseRequest>()
                val kb = knowledgeBaseService.createKnowledgeBase(
                    userId,
                    request.name,
                    request.description,
                    request.isPublic
                )
                val view = knowledgeBaseQueryService.getKnowledgeBase(userId, kb.id)
                call.respondOk(
                    view.toResponse(),
                    "知识库创建成功",
                    HttpStatusCode.Created
                )
            }.describe {
                tag("知识库管理")
                operationId = "createKnowledgeBase"
                summary = "创建知识库"
                description = "创建新的知识库，初始为空"
                requestBody {
                    description = "知识库信息"
                    schema = jsonSchema<CreateKnowledgeBaseRequest>()
                }
                responses {
                    HttpStatusCode.Created {
                        description = "创建成功"
                        schema = jsonSchema<ApiResponse<KnowledgeBaseResponse>>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "输入参数无效"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }

            // 更新知识库
            put("/{knowledgeBaseId}") {
                val userId = call.requireUserId()
                val knowledgeBaseId = KnowledgeBaseId(call.uuidParam("knowledgeBaseId"))
                val request = call.receive<UpdateKnowledgeBaseRequest>()
                val kb = knowledgeBaseService.updateKnowledgeBase(
                    userId, knowledgeBaseId,
                    request.name,
                    request.description,
                    request.isPublic,
                )
                val view = knowledgeBaseQueryService.getKnowledgeBase(userId, kb.id)
                call.respondOk(view.toResponse(), "知识库更新成功")
            }.describe {
                tag("知识库管理")
                operationId = "updateKnowledgeBase"
                summary = "更新知识库"
                description = "更新指定知识库的名称、描述或公开状态"
                parameters {
                    path("knowledgeBaseId") {
                        description = "知识库ID"
                    }
                }
                requestBody {
                    description = "更新的知识库信息"
                    schema = jsonSchema<UpdateKnowledgeBaseRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "更新成功"
                        schema = jsonSchema<ApiResponse<KnowledgeBaseResponse>>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "输入参数无效"
                    }
                    HttpStatusCode.NotFound {
                        description = "知识库不存在"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                    HttpStatusCode.Forbidden {
                        description = "无权修改该知识库"
                    }
                }
            }

            // 删除知识库
            delete("/{knowledgeBaseId}") {
                val userId = call.requireUserId()
                val knowledgeBaseId = KnowledgeBaseId(call.uuidParam("knowledgeBaseId"))
                knowledgeBaseService.deleteKnowledgeBase(userId, knowledgeBaseId)
                call.respond(HttpStatusCode.NoContent)
            }.describe {
                tag("知识库管理")
                operationId = "deleteKnowledgeBase"
                summary = "删除知识库"
                description = "删除指定知识库及其所有关联文件和文档"
                parameters {
                    path("knowledgeBaseId") {
                        description = "知识库ID"
                    }
                }
                responses {
                    HttpStatusCode.NoContent {
                        description = "删除成功"
                    }
                    HttpStatusCode.NotFound {
                        description = "知识库不存在"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                    HttpStatusCode.Forbidden {
                        description = "无权删除该知识库"
                    }
                }
            }

            // 上传文件到知识库（multipart/form-data）
            post("/{knowledgeBaseId}/files") {
                val userId = call.requireUserId()
                val knowledgeBaseId = KnowledgeBaseId(call.uuidParam("knowledgeBaseId"))

                val multipart = call.receiveMultipart()
                var fileName: String? = null
                var contentType: String? = null
                var fileBytes: ByteArray? = null

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

                val bytes = fileBytes ?: throw ValidationException("未上传文件")
                val name = fileName ?: "unknown"
                val ext = name.substringAfterLast('.', "bin")
                val objectKey = "knowledge/${knowledgeBaseId.value}/${Uuid.random()}.$ext"

                fileStoragePort.upload(objectKey, contentType ?: "application/octet-stream", bytes)

                val file = knowledgeFileService.uploadFile(
                    userId = userId,
                    knowledgeBaseId = knowledgeBaseId,
                    objectKey = objectKey,
                    fileName = name,
                    fileSize = bytes.size.toLong(),
                    fileType = contentType,
                )
                call.respondOk(file.toResponse(), "文件上传成功")
            }.describe {
                tag("知识库文件管理")
                operationId = "uploadKnowledgeFile"
                summary = "上传文件"
                description = "通过 multipart/form-data 上传文件到指定知识库，文件将被解析并向量化"
                parameters {
                    path("knowledgeBaseId") {
                        description = "知识库ID"
                    }
                }
                requestBody {
                    description = "要上传的文件（字段名为 'file'）"
                    required = true
                    ContentType.MultiPart.FormData {
                        schema = JsonSchema(
                            type = JsonType.OBJECT,
                            properties = mapOf(
                                "file" to Value(
                                    JsonSchema(
                                        type = JsonType.STRING,
                                        format = "binary",
                                        description = "文件内容（支持 PDF、DOCX、TXT 等格式）"
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
                        schema = jsonSchema<ApiResponse<KnowledgeFileResponse>>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "输入参数无效或未上传文件"
                    }
                    HttpStatusCode.NotFound {
                        description = "知识库不存在"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                    HttpStatusCode.Forbidden {
                        description = "无权修改该知识库"
                    }
                }
            }

            // 获取文件详情
            get("/{knowledgeBaseId}/files/{fileId}") {
                val userId = call.requireUserId()
                val fileId = KnowledgeFileId(call.uuidParam("fileId"))
                val file = knowledgeFileService.getFile(userId, fileId)
                call.respondOk(file.toResponse())
            }.describe {
                tag("知识库文件管理")
                operationId = "getKnowledgeFile"
                summary = "获取文件详情"
                description = "获取指定文件的详细信息"
                parameters {
                    path("knowledgeBaseId") {
                        description = "知识库ID"
                    }
                    path("fileId") {
                        description = "文件ID"
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "获取成功"
                        schema = jsonSchema<ApiResponse<KnowledgeFileResponse>>()
                    }
                    HttpStatusCode.NotFound {
                        description = "文件不存在"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                    HttpStatusCode.Forbidden {
                        description = "无权访问该文件"
                    }
                }
            }

            // 删除文件
            delete("/{knowledgeBaseId}/files/{fileId}") {
                val userId = call.requireUserId()
                val fileId = KnowledgeFileId(call.uuidParam("fileId"))
                knowledgeFileService.deleteFile(userId, fileId)
                call.respond(HttpStatusCode.NoContent)
            }.describe {
                tag("知识库文件管理")
                operationId = "deleteKnowledgeFile"
                summary = "删除文件"
                description = "删除指定文件及其关联的所有文档片段"
                parameters {
                    path("knowledgeBaseId") {
                        description = "知识库ID"
                    }
                    path("fileId") {
                        description = "文件ID"
                    }
                }
                responses {
                    HttpStatusCode.NoContent {
                        description = "删除成功"
                    }
                    HttpStatusCode.NotFound {
                        description = "文件不存在"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                    HttpStatusCode.Forbidden {
                        description = "无权删除该文件"
                    }
                }
            }

            // 重试失败的文件
            post("/{knowledgeBaseId}/files/{fileId}/retry") {
                val userId = call.requireUserId()
                val fileId = KnowledgeFileId(call.uuidParam("fileId"))
                knowledgeFileService.retryFailedFile(userId, fileId)
                call.respondOk("文件已加入重试队列")
            }.describe {
                tag("知识库文件管理")
                operationId = "retryKnowledgeFile"
                summary = "重试处理文件"
                description = "重新处理状态为失败的文件"
                parameters {
                    path("knowledgeBaseId") {
                        description = "知识库ID"
                    }
                    path("fileId") {
                        description = "文件ID"
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "已加入重试队列"
                        schema = jsonSchema<ApiResponse<String>>()
                    }
                    HttpStatusCode.NotFound {
                        description = "文件不存在"
                    }
                    HttpStatusCode.BadRequest {
                        description = "文件状态不是失败"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                    HttpStatusCode.Forbidden {
                        description = "无权操作该文件"
                    }
                }
            }

            // 搜索知识库
            post("/{knowledgeBaseId}/search") {
                val userId = call.requireUserId()
                val knowledgeBaseId = KnowledgeBaseId(call.uuidParam("knowledgeBaseId"))
                val request = call.receive<SearchKnowledgeRequest>()
                val results = knowledgeSearchService.searchKnowledgeBase(
                    userId = userId,
                    knowledgeBaseId = knowledgeBaseId,
                    query = request.query,
                    threshold = request.threshold,
                    limit = request.limit,
                )
                call.respondOk(results.map { it.toResponse() })
            }.describe {
                tag("知识库搜索")
                operationId = "searchKnowledgeBase"
                summary = "搜索知识库"
                description = "在指定知识库中进行向量相似度搜索"
                parameters {
                    path("knowledgeBaseId") {
                        description = "知识库ID"
                    }
                }
                requestBody {
                    description = "搜索请求"
                    schema = jsonSchema<SearchKnowledgeRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "搜索成功"
                        schema = jsonSchema<ApiResponse<Array<SearchResultResponse>>>()
                    }
                    HttpStatusCode.NotFound {
                        description = "知识库不存在"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                    HttpStatusCode.Forbidden {
                        description = "无权访问该知识库"
                    }
                }
            }
        }

        // 跨知识库搜索
        route("/knowledge") {
            // 搜索我的所有知识库
            post("/search") {
                val userId = call.requireUserId()
                val request = call.receive<SearchKnowledgeRequest>()
                val validator = ValidationCollector()

                val selectedKnowledgeBaseIds = request.knowledgeBaseIds?.mapIndexedNotNull { index, rawId ->
                    validator.vo("knowledgeBaseIds[$index]") { KnowledgeBaseId(rawId) }
                }
                validator.throwIfAny()

                val results = if (selectedKnowledgeBaseIds.isNullOrEmpty()) {
                    knowledgeSearchService.searchMyKnowledgeBases(
                        userId = userId,
                        query = request.query,
                        threshold = request.threshold,
                        limit = request.limit,
                    )
                } else {
                    knowledgeSearchService.search(
                        userId = userId,
                        knowledgeBaseIds = selectedKnowledgeBaseIds,
                        query = request.query,
                        threshold = request.threshold,
                        limit = request.limit,
                    )
                }
                call.respondOk(results.map { it.toResponse() })
            }.describe {
                tag("知识库搜索")
                operationId = "searchMyKnowledge"
                summary = "搜索我的知识库"
                description = "在用户的所有知识库或指定的多个知识库中进行向量相似度搜索"
                requestBody {
                    description = "搜索请求"
                    schema = jsonSchema<SearchKnowledgeRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "搜索成功"
                        schema = jsonSchema<ApiResponse<Array<SearchResultResponse>>>()
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                    HttpStatusCode.Forbidden {
                        description = "无权访问指定的知识库"
                    }
                }
            }

        }
    }
}
