package tech.hanasaki.azusa.routes

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.readAvailable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import tech.hanasaki.azusa.app.healthRoutes
import tech.hanasaki.azusa.auth.AuthContext
import tech.hanasaki.azusa.auth.requireAuthContext
import tech.hanasaki.azusa.common.ApiException
import tech.hanasaki.azusa.db.ProfilesTable
import tech.hanasaki.azusa.db.SettingsTable
import tech.hanasaki.azusa.db.UsersTable
import tech.hanasaki.azusa.db.dbQuery
import tech.hanasaki.azusa.permissions.Permissions
import tech.hanasaki.azusa.storage.S3Config
import tech.hanasaki.azusa.storage.S3Storage
import java.io.ByteArrayOutputStream

fun Application.configureRouting(config: ApplicationConfig): Unit {
    val storageConfig = config.readS3Config()
    val s3Storage = S3Storage(storageConfig)
    routing {
        openAPI(path = "openapi", swaggerFile = "openapi/documentation.yaml")
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        authRoutes(config)
        healthRoutes()
        profileRoutes(s3Storage)
        settingsRoutes()
        characterRoutes()
        contactRoutes()
        pluginRoutes()
        chatRoutes()
        knowledgeRoutes()
    }
}

private fun Route.profileRoutes(storage: S3Storage): Unit {
    authenticate("auth-jwt") {
        route("/profiles") {
            get {
                val auth = call.requireAuthContext()
                val profile = dbQuery {
                    ProfilesTable
                        .selectAll()
                        .where { ProfilesTable.id eq EntityID(auth.profileId, ProfilesTable) }
                        .limit(1)
                        .singleOrNull()
                } ?: throw ApiException(HttpStatusCode.NotFound, "PROFILE_NOT_FOUND", "Profile not found")

                call.respond(
                    tech.hanasaki.azusa.profile.ProfileResponse(
                        success = true,
                        message = "成功获取用户信息",
                        data = tech.hanasaki.azusa.profile.ProfileData(
                            id = profile[ProfilesTable.id].value.toString(),
                            uid = profile[ProfilesTable.uid].value.toString(),
                            username = profile[ProfilesTable.username],
                            avatar = profile[ProfilesTable.avatar],
                            createdAt = profile[ProfilesTable.createdAt].toString(),
                            updatedAt = profile[ProfilesTable.updatedAt].toString(),
                        ),
                    ),
                )
            }
            put {
                val auth = call.requireAuthContext()
                val request = call.receive<tech.hanasaki.azusa.profile.UpdateProfileRequest>()
                validateProfileUpdate(request)

                val updated = dbQuery {
                    ProfilesTable
                        .update({ ProfilesTable.id eq EntityID(auth.profileId, ProfilesTable) }) { row ->
                            request.username?.let { row[ProfilesTable.username] = it }
                            if (request.avatar != null) {
                                row[ProfilesTable.avatar] = request.avatar
                            }
                        }

                    ProfilesTable
                        .selectAll()
                        .where { ProfilesTable.id eq EntityID(auth.profileId, ProfilesTable) }
                        .limit(1)
                        .single()
                }

                call.respond(
                    tech.hanasaki.azusa.profile.ProfileResponse(
                        success = true,
                        message = "成功更新用户信息",
                        data = tech.hanasaki.azusa.profile.ProfileData(
                            id = updated[ProfilesTable.id].value.toString(),
                            uid = updated[ProfilesTable.uid].value.toString(),
                            username = updated[ProfilesTable.username],
                            avatar = updated[ProfilesTable.avatar],
                            createdAt = updated[ProfilesTable.createdAt].toString(),
                            updatedAt = updated[ProfilesTable.updatedAt].toString(),
                        ),
                    ),
                )
            }
            post("/avatar") {
                val auth = call.requireAuthContext()
                val multipart = call.receiveMultipart()
                val file = multipart.readFilePart()
                    ?: throw ApiException(HttpStatusCode.BadRequest, "FILE_REQUIRED", "Avatar file is required")

                try {
                    val contentType = file.contentType?.toString() ?: ""
                    if (!ALLOWED_AVATAR_TYPES.contains(contentType)) {
                        throw ApiException(
                            HttpStatusCode.BadRequest,
                            "INVALID_FILE_TYPE",
                            "Avatar file type is not allowed",
                        )
                    }

                    val safeName = makeSafeFileName(file.originalFileName ?: "avatar")
                    val fileName = "${System.currentTimeMillis()}_$safeName"
                    val objectKey = "${auth.userId}/$fileName"
                    val bytes = file.readBytesWithLimit(MAX_AVATAR_SIZE)
                        ?: throw ApiException(HttpStatusCode.BadRequest, "FILE_TOO_LARGE", "Avatar file is too large")
                    val avatarUrl = storage.uploadAvatar(objectKey, contentType, bytes)

                    val profile = dbQuery {
                        ProfilesTable
                            .update({ ProfilesTable.id eq EntityID(auth.profileId, ProfilesTable) }) { row ->
                                row[ProfilesTable.avatar] = avatarUrl
                            }

                        ProfilesTable
                            .selectAll()
                            .where { ProfilesTable.id eq EntityID(auth.profileId, ProfilesTable) }
                            .limit(1)
                            .single()
                    }

                    call.respond(
                        tech.hanasaki.azusa.profile.AvatarUploadResponse(
                            success = true,
                            message = "头像上传成功",
                            data = tech.hanasaki.azusa.profile.AvatarUploadData(
                                avatarUrl = avatarUrl,
                                profile = tech.hanasaki.azusa.profile.ProfileData(
                                    id = profile[ProfilesTable.id].value.toString(),
                                    uid = profile[ProfilesTable.uid].value.toString(),
                                    username = profile[ProfilesTable.username],
                                    avatar = profile[ProfilesTable.avatar],
                                    createdAt = profile[ProfilesTable.createdAt].toString(),
                                    updatedAt = profile[ProfilesTable.updatedAt].toString(),
                                ),
                            ),
                        ),
                    )
                } finally {
                    file.dispose()
                }
            }
        }
    }
}

private fun Route.settingsRoutes(): Unit {
    authenticate("auth-jwt") {
        route("/settings") {
            get {
                val auth = call.requireAuthContext()
                val settings = dbQuery {
                    SettingsTable
                        .selectAll()
                        .where { SettingsTable.ownerId eq EntityID(auth.profileId, ProfilesTable) }
                        .limit(1)
                        .singleOrNull()
                } ?: throw ApiException(HttpStatusCode.NotFound, "SETTINGS_NOT_FOUND", "Settings not found")

                call.respond(
                    tech.hanasaki.azusa.settings.SettingsResponse(
                        success = true,
                        message = "成功获取用户设置",
                        data = tech.hanasaki.azusa.settings.SettingsData(
                            ownerId = settings[SettingsTable.ownerId].value.toString(),
                            theme = settings[SettingsTable.theme],
                            chatModels = settings[SettingsTable.chatModels],
                            createdAt = settings[SettingsTable.createdAt].toString(),
                            updatedAt = settings[SettingsTable.updatedAt].toString(),
                        ),
                    ),
                )
            }
            patch {
                val auth = call.requireAuthContext()
                val request = call.receive<tech.hanasaki.azusa.settings.UpdateSettingsRequest>()

                val updated = dbQuery {
                    SettingsTable
                        .update({ SettingsTable.ownerId eq EntityID(auth.profileId, ProfilesTable) }) { row ->
                            request.theme?.let { row[SettingsTable.theme] = it }
                            request.chatModels?.let { row[SettingsTable.chatModels] = it }
                        }

                    SettingsTable
                        .selectAll()
                        .where { SettingsTable.ownerId eq EntityID(auth.profileId, ProfilesTable) }
                        .limit(1)
                        .singleOrNull()
                } ?: throw ApiException(HttpStatusCode.NotFound, "SETTINGS_NOT_FOUND", "Settings not found")

                call.respond(
                    tech.hanasaki.azusa.settings.SettingsResponse(
                        success = true,
                        message = "成功更新用户设置",
                        data = tech.hanasaki.azusa.settings.SettingsData(
                            ownerId = updated[SettingsTable.ownerId].value.toString(),
                            theme = updated[SettingsTable.theme],
                            chatModels = updated[SettingsTable.chatModels],
                            createdAt = updated[SettingsTable.createdAt].toString(),
                            updatedAt = updated[SettingsTable.updatedAt].toString(),
                        ),
                    ),
                )
            }
        }
    }
}

private fun Route.characterRoutes(): Unit {
    route("/characters") {
        get {
            call.respondNotImplemented("GET /characters is not implemented yet")
        }
        authenticate("auth-jwt") {
            post {
                call.requireAuthContext()
                call.respondNotImplemented("POST /characters is not implemented yet")
            }
        }
    }

    route("/characters/{id}") {
        get {
            val auth = call.principalOrNull()
            val characterId = call.uuidParam("id")
            val canView = Permissions.canViewCharacter(auth?.profileId, characterId)
            if (!canView) {
                throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", "Character not accessible")
            }
            call.respondNotImplemented("GET /characters/:id is not implemented yet")
        }
        authenticate("auth-jwt") {
            put {
                val auth = call.requireAuthContext()
                val characterId = call.uuidParam("id")
                if (!Permissions.isCharacterOwner(auth.profileId, characterId)) {
                    throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", "Character not owned by user")
                }
                call.respondNotImplemented("PUT /characters/:id is not implemented yet")
            }
            delete {
                val auth = call.requireAuthContext()
                val characterId = call.uuidParam("id")
                if (!Permissions.isCharacterOwner(auth.profileId, characterId)) {
                    throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", "Character not owned by user")
                }
                call.respondNotImplemented("DELETE /characters/:id is not implemented yet")
            }
        }
    }

    route("/characters/{id}/knowledge-bases") {
        get {
            val auth = call.principalOrNull()
            val characterId = call.uuidParam("id")
            val canView = Permissions.canViewCharacter(auth?.profileId, characterId)
            if (!canView) {
                throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", "Character not accessible")
            }
            call.respondNotImplemented("GET /characters/:id/knowledge-bases is not implemented yet")
        }
        authenticate("auth-jwt") {
            post {
                val auth = call.requireAuthContext()
                val characterId = call.uuidParam("id")
                if (!Permissions.isCharacterOwner(auth.profileId, characterId)) {
                    throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", "Character not owned by user")
                }
                call.respondNotImplemented("POST /characters/:id/knowledge-bases is not implemented yet")
            }
        }
    }

    authenticate("auth-jwt") {
        delete("/characters/{id}/knowledge-bases/{kbId}") {
            val auth = call.requireAuthContext()
            val characterId = call.uuidParam("id")
            if (!Permissions.isCharacterOwner(auth.profileId, characterId)) {
                throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", "Character not owned by user")
            }
            call.respondNotImplemented("DELETE /characters/:id/knowledge-bases/:kbId is not implemented yet")
        }
    }
}

private fun Route.contactRoutes(): Unit {
    authenticate("auth-jwt") {
        route("/contacts") {
            get {
                call.requireAuthContext()
                call.respondNotImplemented("GET /contacts is not implemented yet")
            }
        }
        route("/contacts/{characterId}") {
            post {
                call.requireAuthContext()
                call.respondNotImplemented("POST /contacts/:characterId is not implemented yet")
            }
            put {
                val auth = call.requireAuthContext()
                val characterId = call.uuidParam("characterId")
                if (!Permissions.isContactOwner(auth.profileId, characterId)) {
                    throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", "Contact not owned by user")
                }
                call.respondNotImplemented("PUT /contacts/:characterId is not implemented yet")
            }
            delete {
                val auth = call.requireAuthContext()
                val characterId = call.uuidParam("characterId")
                if (!Permissions.isContactOwner(auth.profileId, characterId)) {
                    throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", "Contact not owned by user")
                }
                call.respondNotImplemented("DELETE /contacts/:characterId is not implemented yet")
            }
        }
    }
}

private fun Route.pluginRoutes(): Unit {
    route("/plugins") {
        get {
            call.respondNotImplemented("GET /plugins is not implemented yet")
        }
        authenticate("auth-jwt") {
            post {
                call.requireAuthContext()
                call.respondNotImplemented("POST /plugins is not implemented yet")
            }
        }
    }

    route("/plugins/{id}") {
        get {
            val auth = call.principalOrNull()
            val pluginId = call.uuidParam("id")
            if (!Permissions.canViewPlugin(auth?.profileId, pluginId)) {
                throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", "Plugin not accessible")
            }
            call.respondNotImplemented("GET /plugins/:id is not implemented yet")
        }
        authenticate("auth-jwt") {
            put {
                val auth = call.requireAuthContext()
                val pluginId = call.uuidParam("id")
                if (!Permissions.isPluginOwner(auth.profileId, pluginId)) {
                    throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", "Plugin not owned by user")
                }
                call.respondNotImplemented("PUT /plugins/:id is not implemented yet")
            }
            delete {
                val auth = call.requireAuthContext()
                val pluginId = call.uuidParam("id")
                if (!Permissions.isPluginOwner(auth.profileId, pluginId)) {
                    throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", "Plugin not owned by user")
                }
                call.respondNotImplemented("DELETE /plugins/:id is not implemented yet")
            }
        }
    }

    authenticate("auth-jwt") {
        post("/plugins/{id}/subscribe") {
            call.requireAuthContext()
            call.respondNotImplemented("POST /plugins/:id/subscribe is not implemented yet")
        }
        delete("/plugins/{id}/subscribe") {
            call.requireAuthContext()
            call.respondNotImplemented("DELETE /plugins/:id/subscribe is not implemented yet")
        }
        post("/plugins/{id}/like") {
            call.requireAuthContext()
            call.respondNotImplemented("POST /plugins/:id/like is not implemented yet")
        }
        delete("/plugins/{id}/like") {
            call.requireAuthContext()
            call.respondNotImplemented("DELETE /plugins/:id/like is not implemented yet")
        }
    }
}

private fun Route.chatRoutes(): Unit {
    authenticate("auth-jwt") {
        route("/chats") {
            get {
                call.requireAuthContext()
                call.respondNotImplemented("GET /chats is not implemented yet")
            }
            post("/private") {
                call.requireAuthContext()
                call.respondNotImplemented("POST /chats/private is not implemented yet")
            }
        }

        route("/chats/{chatId}") {
            get {
                val auth = call.requireAuthContext()
                val chatId = call.uuidParam("chatId")
                if (!Permissions.isChatMemberOrCharacterOwner(auth.profileId, chatId)) {
                    throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", "Chat not accessible")
                }
                call.respondNotImplemented("GET /chats/:chatId is not implemented yet")
            }
            patch {
                val auth = call.requireAuthContext()
                val chatId = call.uuidParam("chatId")
                if (!Permissions.isChatOwner(auth.profileId, chatId)) {
                    throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", "Chat not owned by user")
                }
                call.respondNotImplemented("PATCH /chats/:chatId is not implemented yet")
            }
            delete {
                val auth = call.requireAuthContext()
                val chatId = call.uuidParam("chatId")
                if (!Permissions.isChatOwner(auth.profileId, chatId)) {
                    throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", "Chat not owned by user")
                }
                call.respondNotImplemented("DELETE /chats/:chatId is not implemented yet")
            }
        }

        get("/chats/{chatId}/messages") {
            val auth = call.requireAuthContext()
            val chatId = call.uuidParam("chatId")
            if (!Permissions.isChatMemberOrCharacterOwner(auth.profileId, chatId)) {
                throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", "Chat not accessible")
            }
            call.respondNotImplemented("GET /chats/:chatId/messages is not implemented yet")
        }

        post("/chats/{chatId}/messages/stream") {
            val auth = call.requireAuthContext()
            val chatId = call.uuidParam("chatId")
            if (!Permissions.isChatMemberOrCharacterOwner(auth.profileId, chatId)) {
                throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", "Chat not accessible")
            }
            call.respondNotImplemented("POST /chats/:chatId/messages/stream is not implemented yet")
        }
    }
}

private fun Route.knowledgeRoutes(): Unit {
    route("/knowledge/bases") {
        get {
            call.respondNotImplemented("GET /knowledge/bases is not implemented yet")
        }
        authenticate("auth-jwt") {
            post {
                call.requireAuthContext()
                call.respondNotImplemented("POST /knowledge/bases is not implemented yet")
            }
        }
    }

    route("/knowledge/bases/{id}") {
        get {
            val auth = call.principalOrNull()
            val kbId = call.uuidParam("id")
            if (!Permissions.canViewKnowledgeBase(auth?.profileId, kbId)) {
                throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", "Knowledge base not accessible")
            }
            call.respondNotImplemented("GET /knowledge/bases/:id is not implemented yet")
        }
        authenticate("auth-jwt") {
            patch {
                val auth = call.requireAuthContext()
                val kbId = call.uuidParam("id")
                if (!Permissions.isKnowledgeBaseOwner(auth.profileId, kbId)) {
                    throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", "Knowledge base not owned by user")
                }
                call.respondNotImplemented("PATCH /knowledge/bases/:id is not implemented yet")
            }
            delete {
                val auth = call.requireAuthContext()
                val kbId = call.uuidParam("id")
                if (!Permissions.isKnowledgeBaseOwner(auth.profileId, kbId)) {
                    throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", "Knowledge base not owned by user")
                }
                call.respondNotImplemented("DELETE /knowledge/bases/:id is not implemented yet")
            }
        }
    }

    route("/knowledge/bases/{id}/files") {
        get {
            val auth = call.principalOrNull()
            val kbId = call.uuidParam("id")
            if (!Permissions.canViewKnowledgeBase(auth?.profileId, kbId)) {
                throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", "Knowledge base not accessible")
            }
            call.respondNotImplemented("GET /knowledge/bases/:id/files is not implemented yet")
        }
    }

    authenticate("auth-jwt") {
        delete("/knowledge/bases/{id}/files/{fileId}") {
            val auth = call.requireAuthContext()
            val kbId = call.uuidParam("id")
            if (!Permissions.isKnowledgeBaseOwner(auth.profileId, kbId)) {
                throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", "Knowledge base not owned by user")
            }
            call.respondNotImplemented("DELETE /knowledge/bases/:id/files/:fileId is not implemented yet")
        }

        post("/knowledge/bases/{id}/documents") {
            val auth = call.requireAuthContext()
            val kbId = call.uuidParam("id")
            if (!Permissions.isKnowledgeBaseOwner(auth.profileId, kbId)) {
                throw ApiException(HttpStatusCode.Forbidden, "FORBIDDEN", "Knowledge base not owned by user")
            }
            call.respondNotImplemented("POST /knowledge/bases/:id/documents is not implemented yet")
        }
    }

    post("/knowledge/search") {
        call.respondNotImplemented("POST /knowledge/search is not implemented yet")
    }
}

suspend fun ApplicationCall.principalOrNull(): AuthContext? {
    val principal = principal<JWTPrincipal>() ?: return null
    val subject = principal.subject ?: return null
    val userId = runCatching { java.util.UUID.fromString(subject) }.getOrNull() ?: return null
    val profileId = dbQuery {
        ProfilesTable
            .selectAll()
            .where { ProfilesTable.uid eq EntityID(userId, UsersTable) }
            .limit(1)
            .map { it[ProfilesTable.id].value }
            .singleOrNull()
    } ?: return null
    val emailVerified = dbQuery {
        UsersTable
            .selectAll()
            .where { UsersTable.id eq userId }
            .limit(1)
            .map { extractEmailVerified(it[UsersTable.rawUserMetaData]) }
            .singleOrNull()
    } ?: false
    if (!emailVerified) {
        return null
    }
    return AuthContext(userId, profileId)
}

private fun extractEmailVerified(meta: JsonElement): Boolean {
    val obj = meta as? JsonObject ?: return false
    val value = obj["emailVerified"] as? JsonPrimitive ?: return false
    return value.booleanOrNull ?: false
}

private fun ApplicationConfig.readS3Config(): S3Config {
    return S3Config(
        endpoint = property("s3.endpoint").getString(),
        region = property("s3.region").getString(),
        bucket = property("s3.bucket").getString(),
        accessKey = property("s3.accessKey").getString(),
        secretKey = property("s3.secretKey").getString(),
        publicBaseUrl = property("s3.publicBaseUrl").getString(),
        forcePathStyle = property("s3.forcePathStyle").getString().toBoolean(),
    )
}

private const val MAX_AVATAR_SIZE: Long = 5L * 1024 * 1024

private val ALLOWED_AVATAR_TYPES = setOf(
    "image/jpeg",
    "image/png",
    "image/webp",
    "image/gif",
)

private fun validateProfileUpdate(request: tech.hanasaki.azusa.profile.UpdateProfileRequest): Unit {
    request.username?.let {
        if (it.isBlank() || it.length > 50) {
            throw ApiException(HttpStatusCode.BadRequest, "VALIDATION_ERROR", "Invalid username")
        }
    }
}

private fun makeSafeFileName(fileName: String): String {
    val trimmed = fileName.trim()
    val replacedSpaces = trimmed.replace(Regex("\\s+"), "_")
    val cleaned = replacedSpaces.replace(Regex("[^A-Za-z0-9._-]"), "")
    return if (cleaned.isNotEmpty()) cleaned else "avatar"
}

private suspend fun MultiPartData.readFilePart(): PartData.FileItem? {
    var result: PartData.FileItem? = null
    forEachPart { part ->
        val isTarget = part is PartData.FileItem && (part.name == "file" || part.name == "avatar")
        if (result == null && isTarget) {
            result = part
        } else {
            part.dispose()
        }
    }
    return result
}

private suspend fun PartData.FileItem.readBytesWithLimit(maxBytes: Long): ByteArray? {
    var total = 0L
    val output = ByteArrayOutputStream()
    val channel = provider()
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    while (true) {
        val read = channel.readAvailable(buffer, 0, buffer.size)
        if (read <= 0) break
        total += read
        if (total > maxBytes) {
            return null
        }
        output.write(buffer, 0, read)
    }
    return output.toByteArray()
}
