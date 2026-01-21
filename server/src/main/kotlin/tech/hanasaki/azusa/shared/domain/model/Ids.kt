package tech.hanasaki.azusa.shared.domain.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@JvmInline
@Serializable
value class UserId(@Contextual val value: UUID)

@JvmInline
@Serializable
value class ThemeId(@Contextual val value: UUID)

@JvmInline
@Serializable
value class CharacterId(@Contextual val value: UUID)
