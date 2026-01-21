package tech.hanasaki.azusa.modules.character.api.dto

import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.modules.character.application.command.CreateCharacterCommand
import tech.hanasaki.azusa.modules.character.application.command.UpdateCharacterCommand

@Serializable
data class CreateCharacterRequest(
    val name: String,
    val avatar: String?,
    val bio: String?,
    val originPrompt: String?,
    val isPublic: Boolean,
) {
    fun toCommand(): CreateCharacterCommand = CreateCharacterCommand(
        name = name,
        avatar = avatar,
        bio = bio,
        originPrompt = originPrompt,
        isPublic = isPublic,
    )
}

@Serializable
data class UpdateCharacterRequest(
    val name: String,
    val avatar: String?,
    val bio: String?,
    val originPrompt: String?,
    val isPublic: Boolean,
) {
    fun toCommand(): UpdateCharacterCommand = UpdateCharacterCommand(
        name = name,
        avatar = avatar,
        bio = bio,
        originPrompt = originPrompt,
        isPublic = isPublic,
    )
}
