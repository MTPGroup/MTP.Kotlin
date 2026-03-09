package tech.hanasaki.momotalk_plus.core.data.datasource.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CreateCharacterRequest(
    val name: String,
    val avatar: String? = null,
    val bio: String? = null,
    val originPrompt: String? = null,
    val isPublic: Boolean = false,
)

@Serializable
data class UpdateCharacterRequest(
    val name: String,
    val avatar: String? = null,
    val bio: String? = null,
    val originPrompt: String? = null,
    val isPublic: Boolean = false,
)

@Serializable
data class CharacterAuthorDto(
    val id: String,
    val name: String,
    val avatar: String? = null,
)

@Serializable
data class CharacterDto(
    val id: String,
    val author: CharacterAuthorDto? = null,
    val name: String,
    val avatar: String? = null,
    val bio: String? = null,
    val originPrompt: String? = null,
    val isPublic: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

@Serializable
data class UploadCharacterAvatarResponse(
    val avatar: String,
)
