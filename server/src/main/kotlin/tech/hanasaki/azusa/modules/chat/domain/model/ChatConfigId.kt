package tech.hanasaki.azusa.modules.chat.domain.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@JvmInline
@Serializable
value class ChatConfigId(@Contextual val value: UUID)
