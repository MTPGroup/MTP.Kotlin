package tech.hanasaki.azusa.auth.application.result

import tech.hanasaki.azusa.auth.application.port.TokenPair
import tech.hanasaki.azusa.auth.domain.model.AvatarUrl
import tech.hanasaki.azusa.auth.domain.model.Email
import tech.hanasaki.azusa.auth.domain.model.Username
import tech.hanasaki.azusa.shared.UserId
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