package tech.hanasaki.azusa.modules.chat.domain.model

import kotlinx.serialization.Contextual
import java.util.*

@JvmInline
value class MessageId(@Contextual val value: UUID)
