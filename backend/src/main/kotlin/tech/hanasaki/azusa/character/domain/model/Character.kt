package tech.hanasaki.azusa.character.domain.model

import tech.hanasaki.azusa.common.CharacterId
import tech.hanasaki.azusa.common.UserId
import kotlin.time.Instant

data class Character(
    val id: CharacterId,
    val authorId: UserId,
    val name: String,
    val avatar: String?,
    val bio: String?,
    val originPrompt: String?,
    val isPublic: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
