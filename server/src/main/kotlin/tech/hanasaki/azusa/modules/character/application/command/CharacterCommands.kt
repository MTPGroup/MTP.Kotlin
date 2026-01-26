package tech.hanasaki.azusa.modules.character.application.command

import tech.hanasaki.azusa.common.kernel.model.AvatarUrl

data class CreateCharacterCommand(
    val name: String,
    val avatar: AvatarUrl?,
    val bio: String?,
    val originPrompt: String?,
    val isPublic: Boolean,
)

data class UpdateCharacterCommand(
    val name: String,
    val avatar: AvatarUrl?,
    val bio: String?,
    val originPrompt: String?,
    val isPublic: Boolean,
)
