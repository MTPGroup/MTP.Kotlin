package tech.hanasaki.azusa.modules.character.api.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.modules.character.domain.model.Character
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

fun Character.toResponse(): CharacterResponse = CharacterResponse(
    id = id.value,
    authorId = authorId.value,
    name = name,
    avatar = avatar,
    bio = bio,
    originPrompt = originPrompt,
    isPublic = isPublic,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)
