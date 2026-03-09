package tech.hanasaki.azusa.modules.character.adapter.`in`.web.dto

import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.modules.character.application.port.`in`.dto.CharacterAuthorView
import tech.hanasaki.azusa.modules.character.application.port.`in`.dto.CharacterExampleMessageView
import tech.hanasaki.azusa.modules.character.application.port.`in`.dto.CharacterFavoriteStatusView
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
    val tags: List<String>,
    val originPrompt: String?,
    val isPublic: Boolean,
    val favoriteCount: Int,
    val chatCount: Int,
    val createdAt: String,
    val updatedAt: String,
)

typealias PagedCharacterResponse = PagedResponse<CharacterResponse>

@Serializable
data class ExampleMessageResponse(
    val role: String,
    val content: String,
)

@Serializable
data class CharacterDetailResponse(
    val id: Uuid,
    val author: AuthorProfileResponse? = null,
    val name: String,
    val avatar: String?,
    val bio: String?,
    val tags: List<String>,
    val originPrompt: String?,
    val exampleMessages: List<ExampleMessageResponse>,
    val isPublic: Boolean,
    val favoriteCount: Int,
    val chatCount: Int,
    val isFavorited: Boolean = false,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class CharacterFavoriteStatusResponse(
    val characterId: Uuid,
    val isFavorited: Boolean,
    val favoritedAt: String? = null,
)

@Serializable
data class TrendingCharacterResponse(
    val id: Uuid,
    val author: AuthorProfileResponse? = null,
    val name: String,
    val avatar: String?,
    val bio: String?,
    val favoriteCount: Int,
    val chatCount: Int,
    val rank: Int,
)

@Serializable
data class TrendingCharactersDataResponse(
    val items: List<TrendingCharacterResponse>,
    val period: String,
)

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

fun CharacterExampleMessageView.toResponse(): ExampleMessageResponse = ExampleMessageResponse(
    role = role,
    content = content,
)

fun CharacterView.toResponse(): CharacterResponse = CharacterResponse(
    id = id,
    author = author?.toResponse(),
    name = name,
    avatar = avatar,
    bio = bio,
    tags = tags,
    originPrompt = originPrompt,
    isPublic = isPublic,
    favoriteCount = favoriteCount,
    chatCount = chatCount,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun CharacterView.toDetailResponse(): CharacterDetailResponse = CharacterDetailResponse(
    id = id,
    author = author?.toResponse(),
    name = name,
    avatar = avatar,
    bio = bio,
    tags = tags,
    originPrompt = originPrompt,
    exampleMessages = exampleMessages.map { it.toResponse() },
    isPublic = isPublic,
    favoriteCount = favoriteCount,
    chatCount = chatCount,
    isFavorited = isFavorited,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun CharacterFavoriteStatusView.toResponse(): CharacterFavoriteStatusResponse = CharacterFavoriteStatusResponse(
    characterId = characterId,
    isFavorited = isFavorited,
    favoritedAt = favoritedAt,
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

fun List<CharacterView>.toTrendingResponse(period: String): TrendingCharactersDataResponse =
    TrendingCharactersDataResponse(
        items = mapIndexed { index, it ->
            TrendingCharacterResponse(
                id = it.id,
                author = it.author?.toResponse(),
                name = it.name,
                avatar = it.avatar,
                bio = it.bio,
                favoriteCount = it.favoriteCount,
                chatCount = it.chatCount,
                rank = index + 1,
            )
        },
        period = period,
    )

fun Character.toResponse(author: AuthorProfileResponse? = null): CharacterResponse = CharacterResponse(
    id = id.value,
    author = author,
    name = name,
    avatar = avatar?.value,
    bio = bio,
    tags = emptyList(),
    originPrompt = originPrompt,
    isPublic = isPublic,
    favoriteCount = 0,
    chatCount = 0,
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
