package tech.hanasaki.azusa.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.selectAll
import tech.hanasaki.azusa.app.healthRoutes
import tech.hanasaki.azusa.auth.AuthContext
import tech.hanasaki.azusa.auth.UserPrincipal
import tech.hanasaki.azusa.auth.requireAuthContext
import tech.hanasaki.azusa.common.ApiException
import tech.hanasaki.azusa.db.ProfilesTable
import tech.hanasaki.azusa.db.UsersTable
import tech.hanasaki.azusa.db.dbQuery
import tech.hanasaki.azusa.permissions.Permissions

fun Application.configureRouting(): Unit {
    routing {
        healthRoutes()
        profileRoutes()
        settingsRoutes()
        characterRoutes()
        contactRoutes()
        pluginRoutes()
        chatRoutes()
        knowledgeRoutes()
    }
}

private fun Route.profileRoutes(): Unit {
    authenticate("auth-jwt") {
        route("/profiles") {
            get {
                call.requireAuthContext()
                call.respondNotImplemented("GET /profiles is not implemented yet")
            }
            put {
                call.requireAuthContext()
                call.respondNotImplemented("PUT /profiles is not implemented yet")
            }
            post("/avatar") {
                call.requireAuthContext()
                call.respondNotImplemented("POST /profiles/avatar is not implemented yet")
            }
        }
    }
}

private fun Route.settingsRoutes(): Unit {
    authenticate("auth-jwt") {
        route("/settings") {
            get {
                call.requireAuthContext()
                call.respondNotImplemented("GET /settings is not implemented yet")
            }
            patch {
                call.requireAuthContext()
                call.respondNotImplemented("PATCH /settings is not implemented yet")
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
    val principal = principal<UserPrincipal>() ?: return null
    val profileId = dbQuery {
        ProfilesTable
            .selectAll()
            .where { ProfilesTable.uid eq EntityID(principal.userId, UsersTable) }
            .limit(1)
            .map { it[ProfilesTable.id].value }
            .singleOrNull()
    } ?: return null
    return AuthContext(principal.userId, profileId)
}
