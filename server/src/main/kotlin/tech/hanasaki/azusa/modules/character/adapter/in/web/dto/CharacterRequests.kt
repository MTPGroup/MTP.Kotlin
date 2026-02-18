package tech.hanasaki.azusa.modules.character.adapter.`in`.web.dto

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

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
data class UploadCharacterAvatarResponse(
    val avatar: String,
)


@Serializable
data class SubscribeKnowledgeBaseRequest(
    val knowledgeBaseId: Uuid,
    val priority: Int = 0,
)