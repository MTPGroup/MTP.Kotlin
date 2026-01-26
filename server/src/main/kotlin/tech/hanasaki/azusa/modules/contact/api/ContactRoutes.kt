package tech.hanasaki.azusa.modules.contact.api

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import tech.hanasaki.azusa.common.platform.api.respondOk
import tech.hanasaki.azusa.common.kernel.model.CharacterId
import tech.hanasaki.azusa.common.platform.api.parseLimitParam
import tech.hanasaki.azusa.common.platform.api.parsePageParam
import tech.hanasaki.azusa.common.platform.api.requireUserId
import tech.hanasaki.azusa.common.platform.api.uuidParam
import tech.hanasaki.azusa.modules.contact.api.dto.AddContactRequest
import tech.hanasaki.azusa.modules.contact.api.dto.UpdateContactRequest
import tech.hanasaki.azusa.modules.contact.api.dto.toResponse
import tech.hanasaki.azusa.modules.contact.application.service.ContactService

fun Route.contactRoutes() {
    val contactService: ContactService by inject()

    authenticate("auth-jwt") {
        route("/contacts") {
            // 添加联系人
            post {
                val userId = call.requireUserId()
                val request = call.receive<AddContactRequest>()
                val contact = contactService.addContact(request.toCommand(userId))
                call.respondOk(contact.toResponse())
            }

            // 获取联系人列表
            get {
                val userId = call.requireUserId()
                val page = parsePageParam(call.request.queryParameters["page"])
                val limit = parseLimitParam(call.request.queryParameters["limit"])
                val result = contactService.listMyContacts(userId, page, limit)
                call.respondOk(result.toResponse())
            }

            // 获取联系人详情
            get("/{characterId}") {
                val userId = call.requireUserId()
                val characterId = CharacterId(call.uuidParam("characterId"))
                val contact = contactService.getContact(userId, characterId)
                call.respondOk(contact.toResponse())
            }

            // 更新联系人昵称
            put("/{characterId}") {
                val userId = call.requireUserId()
                val characterId = CharacterId(call.uuidParam("characterId"))
                val request = call.receive<UpdateContactRequest>()
                val contact = contactService.updateContactNickname(request.toCommand(userId, characterId))
                call.respondOk(contact.toResponse())
            }

            // 删除联系人
            delete("/{characterId}") {
                val userId = call.requireUserId()
                val characterId = CharacterId(call.uuidParam("characterId"))
                contactService.deleteContact(userId, characterId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}