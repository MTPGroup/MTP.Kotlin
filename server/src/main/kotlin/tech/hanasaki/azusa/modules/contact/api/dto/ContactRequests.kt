package tech.hanasaki.azusa.modules.contact.api.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.modules.contact.application.command.AddContactCommand
import tech.hanasaki.azusa.modules.contact.application.command.UpdateContactCommand
import tech.hanasaki.azusa.common.kernel.model.CharacterId
import tech.hanasaki.azusa.common.kernel.model.UserId
import java.util.*

@Serializable
data class AddContactRequest(
    @Contextual
    val characterId: UUID,
    val nickname: String? = null,
) {
    fun toCommand(userId: UserId): AddContactCommand = AddContactCommand(
        userId = userId,
        characterId = CharacterId(characterId),
        nickname = nickname,
    )
}

@Serializable
data class UpdateContactRequest(
    val nickname: String? = null,
) {
    fun toCommand(userId: UserId, characterId: CharacterId): UpdateContactCommand = UpdateContactCommand(
        userId = userId,
        characterId = characterId,
        nickname = nickname,
    )
}