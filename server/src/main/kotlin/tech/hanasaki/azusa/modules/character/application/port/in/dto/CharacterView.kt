package tech.hanasaki.azusa.modules.character.application.port.`in`.dto

import kotlin.uuid.Uuid


data class CharacterAuthorView(
    val id: Uuid,
    val name: String,
    val avatar: String?,
)

data class CharacterExampleMessageView(
    val role: String,
    val content: String,
)

data class CharacterFavoriteStatusView(
    val characterId: Uuid,
    val isFavorited: Boolean,
    val favoritedAt: String? = null,
)

data class CharacterView(
    val id: Uuid,
    val authorId: Uuid,
    val author: CharacterAuthorView?,
    val name: String,
    val avatar: String?,
    val bio: String?,
    val tags: List<String>,
    val exampleMessages: List<CharacterExampleMessageView>,
    val originPrompt: String?,
    val isPublic: Boolean,
    val favoriteCount: Int,
    val chatCount: Int,
    val isFavorited: Boolean? = null,
    val createdAt: String,
    val updatedAt: String,
)
