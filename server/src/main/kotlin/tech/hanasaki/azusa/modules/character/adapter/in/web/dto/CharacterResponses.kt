package tech.hanasaki.azusa.modules.character.adapter.`in`.web.dto

import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.modules.character.application.port.`in`.dto.CharacterAuthorView
import tech.hanasaki.azusa.modules.character.application.port.`in`.dto.CharacterView
import tech.hanasaki.azusa.modules.character.domain.model.Character
import tech.hanasaki.azusa.modules.character.domain.model.KnowledgeSubscription
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.infrastructure.web.response.PagedResponse
import kotlin.uuid.Uuid


@Serializable
data class AuthorProfileResponse(
    val id: Uuid,
    val name: String,
    val avatar: String?,
)

@Serializable
data class CharacterResponse(
    val id: Uuid,
    val author: AuthorProfileResponse? = null,
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

fun CharacterAuthorView.toResponse(): AuthorProfileResponse = AuthorProfileResponse(
    id = id,
    name = name,
    avatar = avatar,
)

fun CharacterView.toResponse(): CharacterResponse = CharacterResponse(
    id = id,
    author = author?.toResponse(),
    name = name,
    avatar = avatar,
    bio = bio,
    originPrompt = originPrompt,
    isPublic = isPublic,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun PageResult<CharacterView>.toResponse(): PagedCharacterResponse = PagedCharacterResponse(
    items = items.map { it.toResponse() },
    total = total,
    page = page,
    limit = limit,
    totalPages = totalPages,
    hasNext = hasNext,
    hasPrevious = hasPrevious,
)

fun Character.toResponse(author: AuthorProfileResponse? = null): CharacterResponse = CharacterResponse(
    id = id.value,
    author = author,
    name = name,
    avatar = avatar?.value,
    bio = bio,
    originPrompt = originPrompt,
    isPublic = isPublic,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)

fun PageResult<Character>.toResponse(authors: Map<Uuid, AuthorProfileResponse> = emptyMap()): PagedCharacterResponse =
    PagedCharacterResponse(
        items = items.map { character -> character.toResponse(authors[character.authorId.value]) },
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
