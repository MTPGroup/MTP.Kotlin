package tech.hanasaki.azusa.modules.contact.application.command

import tech.hanasaki.azusa.common.kernel.model.CharacterId
import tech.hanasaki.azusa.common.kernel.model.UserId

data class AddContactCommand(
    val userId: UserId,
    val characterId: CharacterId,
    val nickname: String? = null,
)

data class UpdateContactCommand(
    val userId: UserId,
    val characterId: CharacterId,
    val nickname: String? = null,
)