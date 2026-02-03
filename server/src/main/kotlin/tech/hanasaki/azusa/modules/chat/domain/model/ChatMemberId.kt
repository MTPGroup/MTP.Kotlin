package tech.hanasaki.azusa.modules.chat.domain.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@JvmInline
@Serializable
value class ChatMemberId(@Contextual val value: Uuid)
