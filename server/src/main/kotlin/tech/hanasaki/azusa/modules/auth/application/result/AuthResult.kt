package tech.hanasaki.azusa.modules.auth.application.result

import tech.hanasaki.azusa.common.domain.model.AvatarUrl
import tech.hanasaki.azusa.common.domain.model.Email
import tech.hanasaki.azusa.common.domain.model.UserId
import tech.hanasaki.azusa.modules.auth.domain.model.Username
import kotlin.time.Instant


data class LoginResult(
    val userId: UserId,
    val email: Email,
    val username: Username,
    val avatar: AvatarUrl?,
    val isEmailVerified: Boolean,
    val tokens: TokenPair,
    val createdAt: Instant,
    val updatedAt: Instant,
)
