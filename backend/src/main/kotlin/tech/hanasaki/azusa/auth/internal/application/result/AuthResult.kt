package tech.hanasaki.azusa.auth.internal.application.result

import tech.hanasaki.azusa.auth.internal.application.service.TokenPair
import tech.hanasaki.azusa.auth.internal.domain.model.AvatarUrl
import tech.hanasaki.azusa.auth.internal.domain.model.Email
import tech.hanasaki.azusa.auth.internal.domain.model.UserId
import tech.hanasaki.azusa.auth.internal.domain.model.Username
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