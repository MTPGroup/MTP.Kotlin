package tech.hanasaki.azusa.modules.contact.application.command

import tech.hanasaki.azusa.shared.domain.model.CharacterId
import tech.hanasaki.azusa.shared.domain.model.UserId

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