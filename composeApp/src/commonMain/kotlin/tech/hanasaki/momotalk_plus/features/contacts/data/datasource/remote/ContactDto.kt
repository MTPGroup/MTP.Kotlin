package tech.hanasaki.momotalk_plus.features.contacts.data.datasource.remote

import kotlinx.serialization.Serializable
import tech.hanasaki.momotalk_plus.features.contacts.domain.model.Contact
import kotlin.time.Instant

@Serializable
data class AuthorDto(
    val id: String,
    val name: String,
    val avatar: String?,
)

@Serializable
data class ContactItemDto(
    val id: String,
    val name: String,
    val avatar: String?,
    val author: AuthorDto,
    val bio: String?,
    val originPrompt: String?,
    val isPublic: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

@Serializable
data class ContactListData(
    val contacts: List<Contact>,
)

@Serializable
data class ContactListResponse(
    val success: Boolean,
    val data: ContactListData,
)
