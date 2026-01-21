package tech.hanasaki.azusa.character.api.dto

import tech.hanasaki.azusa.character.application.command.CreateCharacterCommand
import tech.hanasaki.azusa.character.application.command.UpdateCharacterCommand

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
