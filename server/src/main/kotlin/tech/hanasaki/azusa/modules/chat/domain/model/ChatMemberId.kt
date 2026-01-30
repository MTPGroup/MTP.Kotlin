package tech.hanasaki.azusa.modules.chat.domain.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@JvmInline
@Serializable
value class ChatMemberId(@Contextual val value: UUID)
