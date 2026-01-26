package tech.hanasaki.azusa.modules.contact.api.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.modules.contact.domain.model.Contact
import tech.hanasaki.azusa.shared.domain.model.PageResult
import java.util.*

@Serializable
data class ContactResponse(
    @Contextual
    val userId: UUID,
    @Contextual
    val characterId: UUID,
    val nickname: String?,
    val addedAt: String,
)


@Serializable
data class PagedContactResponse(
    val items: List<ContactResponse>,
    val total: Long,
    val page: Int,
    val limit: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
)

fun Contact.toResponse(): ContactResponse = ContactResponse(
    userId = userId.value,
    characterId = characterId.value,
    nickname = nickname,
    addedAt = addedAt.toString()
)

fun PageResult<Contact>.toResponse(): PagedContactResponse = PagedContactResponse(
    items = items.map { it.toResponse() },
    total = total,
    page = page,
    limit = limit,
    totalPages = totalPages,
    hasNext = hasNext,
    hasPrevious = hasPrevious,

    )