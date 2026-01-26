package tech.hanasaki.azusa.modules.character.api.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.modules.character.domain.model.Character
import tech.hanasaki.azusa.modules.character.domain.model.KnowledgeSubscription
import tech.hanasaki.azusa.shared.domain.model.PageResult
import java.util.UUID

@Serializable
data class CharacterResponse(
    @Contextual val id: UUID,
    @Contextual val authorId: UUID,
    val name: String,
    val avatar: String?,
    val bio: String?,
    val originPrompt: String?,
    val isPublic: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class PagedCharacterResponse(
    val items: List<CharacterResponse>,
    val total: Long,
    val page: Int,
    val limit: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
)

@Serializable
data class KnowledgeSubscriptionResponse(
    @Contextual val knowledgeBaseId: UUID,
    val priority: Int,
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
