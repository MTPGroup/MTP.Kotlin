package tech.hanasaki.momotalk_plus.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@Serializable
data class User @OptIn(ExperimentalTime::class) constructor(
    val id: String,
    val name: String,
    val avatar: String,
    val email: String,
    val theme: String,
    @SerialName("notifications_enabled")
    val notificationsEnabled: Boolean,
    @SerialName("created_at")
    val createdAt: Instant?,
    @SerialName("updated_at")
    val updatedAt: Instant?,
)
