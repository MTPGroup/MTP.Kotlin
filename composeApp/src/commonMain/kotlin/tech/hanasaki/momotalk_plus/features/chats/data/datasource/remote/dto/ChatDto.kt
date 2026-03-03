package tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateChatRequest(
    val characterId: String,
    val name: String? = null,
    val temporary: Boolean = false,
)

@Serializable
data class UpdateChatNameRequest(
    val name: String?,
)

@Serializable
data class ChatResponseDto(
    val id: String,
    val name: String? = null,
    val lastMessage: String? = null,
    val characterId: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val temporary: Boolean = false,
)

@Serializable
data class PagedResponseDto<T>(
    val items: List<T>,
    val total: Long,
    val page: Int,
    val limit: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
)
