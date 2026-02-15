package tech.hanasaki.azusa.modules.character.adapter.`in`.web.dto

import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.modules.character.domain.model.Character
import tech.hanasaki.azusa.modules.character.domain.model.KnowledgeSubscription
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.infrastructure.web.response.PagedResponse
import kotlin.uuid.Uuid


@Serializable
data class CharacterResponse(
    val id: Uuid,
    val authorId: Uuid,
    val name: String,
    val avatar: String?,
    val bio: String?,
    val originPrompt: String?,
    val isPublic: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

typealias PagedCharacterResponse = PagedResponse<CharacterResponse>

@Serializable
data class KnowledgeSubscriptionResponse(
    val knowledgeBaseId: Uuid,
    val priority: Int,
)

@Serializable
data class SuccessResponse(
    val success: Boolean = true,
)

fun Character.toResponse(): CharacterResponse = CharacterResponse(
    id = id.value,
    authorId = authorId.value,
    name = name,
    avatar = avatar?.value,
    bio = bio,
    originPrompt = originPrompt,
    isPublic = isPublic,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)

fun PageResult<Character>.toResponse(): PagedCharacterResponse = PagedCharacterResponse(
    items = items.map { it.toResponse() },
    total = total,
    page = page,
    limit = limit,
    totalPages = totalPages,
    hasNext = hasNext,
    hasPrevious = hasPrevious,
)


fun KnowledgeSubscription.toResponse(): KnowledgeSubscriptionResponse = KnowledgeSubscriptionResponse(
    knowledgeBaseId = knowledgeBaseId.value,
    priority = priority,
)

fun List<KnowledgeSubscription>.toResponse(): List<KnowledgeSubscriptionResponse> =
    map { it.toResponse() }
