package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.modules.auth.domain.model.UserStatus
import kotlin.time.Instant

@Serializable
data class UserMetaData(
    val status: UserStatus,
    val emailVerified: Boolean,
    @Contextual
    val bannedUntil: Instant?,
)
