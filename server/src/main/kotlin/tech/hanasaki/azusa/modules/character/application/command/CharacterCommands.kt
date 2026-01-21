package tech.hanasaki.azusa.modules.character.application.command

data class CreateCharacterCommand(
    val name: String,
    val avatar: String?,
    val bio: String?,
    val originPrompt: String?,
    val isPublic: Boolean,
)

data class UpdateCharacterCommand(
    val name: String,
    val avatar: String?,
    val bio: String?,
    val originPrompt: String?,
    val isPublic: Boolean,
)
