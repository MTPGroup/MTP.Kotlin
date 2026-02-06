package tech.hanasaki.azusa.modules.knowledge.api

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import tech.hanasaki.azusa.modules.knowledge.api.dto.*
import tech.hanasaki.azusa.modules.knowledge.application.service.KnowledgeBaseService
import tech.hanasaki.azusa.modules.knowledge.application.service.KnowledgeFileService
import tech.hanasaki.azusa.modules.knowledge.application.service.KnowledgeSearchService
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeFileId
import tech.hanasaki.azusa.shared.infrastructure.web.response.respondOk
import tech.hanasaki.azusa.shared.infrastructure.web.route.requireUserId
import tech.hanasaki.azusa.shared.infrastructure.web.route.uuidParam
import tech.hanasaki.azusa.shared.infrastructure.web.validation.validateLimit
import tech.hanasaki.azusa.shared.infrastructure.web.validation.validatePage

fun Route.knowledgeRoutes() {
    val knowledgeBaseService: KnowledgeBaseService by inject()
    val knowledgeFileService: KnowledgeFileService by inject()
    val knowledgeSearchService: KnowledgeSearchService by inject()

    // 公开知识库（无需认证）
    route("/knowledge-bases") {
        // 获取公开知识库列表
        get("/public") {
            val page = validatePage(call.request.queryParameters["page"])
            val limit = validateLimit(call.request.queryParameters["limit"])
            val result = knowledgeBaseService.listPublicKnowledgeBases(page, limit)
            call.respondOk(result.toResponse())
        }

        // 搜索公开知识库
        get("/search") {
            val query = call.request.queryParameters["q"] ?: ""
            val page = validatePage(call.request.queryParameters["page"])
            val limit = validateLimit(call.request.queryParameters["limit"])
            val result = knowledgeBaseService.searchKnowledgeBases(query, page, limit)
            call.respondOk(result.toResponse())
        }
    }

    authenticate("auth-jwt") {
        route("/knowledge-bases") {
            // 获取我的知识库列表
            get {
                val userId = call.requireUserId()
                val page = validatePage(call.request.queryParameters["page"])
                val limit = validateLimit(call.request.queryParameters["limit"])
                val result = knowledgeBaseService.listMyKnowledgeBases(userId, page, limit)
                call.respondOk(result.toResponse())
            }

            // 创建知识库
            post {
                val userId = call.requireUserId()
                val request = call.receive<CreateKnowledgeBaseRequest>()
                val kb = knowledgeBaseService.createKnowledgeBase(userId, request.toCommand())
                call.respondOk(kb.toResponse(), "Knowledge base created")
            }

            // 获取知识库详情
            get("/{knowledgeBaseId}") {
                val userId = call.requireUserId()
                val knowledgeBaseId = KnowledgeBaseId(call.uuidParam("knowledgeBaseId"))
                val kb = knowledgeBaseService.getKnowledgeBase(userId, knowledgeBaseId)
                call.respondOk(kb.toResponse())
            }

            // 更新知识库
            put("/{knowledgeBaseId}") {
                val userId = call.requireUserId()
                val knowledgeBaseId = KnowledgeBaseId(call.uuidParam("knowledgeBaseId"))
                val request = call.receive<UpdateKnowledgeBaseRequest>()
                val kb = knowledgeBaseService.updateKnowledgeBase(userId, knowledgeBaseId, request.toCommand())
                call.respondOk(kb.toResponse(), "Knowledge base updated")
            }

            // 删除知识库
            delete("/{knowledgeBaseId}") {
                val userId = call.requireUserId()
                val knowledgeBaseId = KnowledgeBaseId(call.uuidParam("knowledgeBaseId"))
                knowledgeBaseService.deleteKnowledgeBase(userId, knowledgeBaseId)
                call.respond(HttpStatusCode.NoContent)
            }

            // 获取知识库统计
            get("/{knowledgeBaseId}/stats") {
                val userId = call.requireUserId()
                val knowledgeBaseId = KnowledgeBaseId(call.uuidParam("knowledgeBaseId"))
                val stats = knowledgeBaseService.getKnowledgeBaseStats(userId, knowledgeBaseId)
                call.respondOk(stats.toResponse())
            }

            // ========== 文件管理 ==========

            // 获取知识库的文件列表
            get("/{knowledgeBaseId}/files") {
                val userId = call.requireUserId()
                val knowledgeBaseId = KnowledgeBaseId(call.uuidParam("knowledgeBaseId"))
                val files = knowledgeFileService.listFiles(userId, knowledgeBaseId)
                call.respondOk(files.map { it.toResponse() })
            }

            // 上传文件到知识库
            post("/{knowledgeBaseId}/files") {
                val userId = call.requireUserId()
                val knowledgeBaseId = KnowledgeBaseId(call.uuidParam("knowledgeBaseId"))
                val request = call.receive<UploadFileRequest>()
                val file = knowledgeFileService.uploadFile(
                    userId = userId,
                    knowledgeBaseId = knowledgeBaseId,
                    fileName = request.fileName,
                    filePath = request.filePath,
                    fileSize = request.fileSize,
                    fileType = request.fileType,
                )
                call.respondOk(file.toResponse(), "File uploaded")
            }

            // 获取文件详情
            get("/{knowledgeBaseId}/files/{fileId}") {
                val userId = call.requireUserId()
                val fileId = KnowledgeFileId(call.uuidParam("fileId"))
                val file = knowledgeFileService.getFile(userId, fileId)
                call.respondOk(file.toResponse())
            }

            // 删除文件
            delete("/{knowledgeBaseId}/files/{fileId}") {
                val userId = call.requireUserId()
                val fileId = KnowledgeFileId(call.uuidParam("fileId"))
                knowledgeFileService.deleteFile(userId, fileId)
                call.respond(HttpStatusCode.NoContent)
            }

            // 重试失败的文件
            post("/{knowledgeBaseId}/files/{fileId}/retry") {
                val userId = call.requireUserId()
                val fileId = KnowledgeFileId(call.uuidParam("fileId"))
                knowledgeFileService.retryFailedFile(userId, fileId)
                call.respondOk("File retry queued")
            }

            // ========== 搜索 ==========

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
            }
        }

        // 跨知识库搜索
        route("/knowledge") {
            // 搜索我的所有知识库
            post("/search") {
                val userId = call.requireUserId()
                val request = call.receive<SearchKnowledgeRequest>()

                val results = if (request.knowledgeBaseIds.isNullOrEmpty()) {
                    knowledgeSearchService.searchMyKnowledgeBases(
                        userId = userId,
                        query = request.query,
                        threshold = request.threshold,
                        limit = request.limit,
                    )
                } else {
                    knowledgeSearchService.search(
                        userId = userId,
                        knowledgeBaseIds = request.knowledgeBaseIds.map { KnowledgeBaseId(it) },
                        query = request.query,
                        threshold = request.threshold,
                        limit = request.limit,
                    )
                }
                call.respondOk(results.map { it.toResponse() })
            }

            // 搜索公开知识库
            post("/search/public") {
                val request = call.receive<SearchKnowledgeRequest>()
                val results = knowledgeSearchService.searchPublicKnowledgeBases(
                    query = request.query,
                    threshold = request.threshold,
                    limit = request.limit,
                )
                call.respondOk(results.map { it.toResponse() })
            }
        }
    }
}
