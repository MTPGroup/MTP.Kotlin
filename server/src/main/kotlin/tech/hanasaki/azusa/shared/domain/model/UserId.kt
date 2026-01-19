package tech.hanasaki.azusa.shared.domain.model

import kotlinx.serialization.Serializable
import java.util.*

@JvmInline
@Serializable
value class UserId(val value: UUID)