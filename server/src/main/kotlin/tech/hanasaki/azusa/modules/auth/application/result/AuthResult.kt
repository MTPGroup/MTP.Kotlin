package tech.hanasaki.azusa.modules.auth.application.result

import kotlinx.datetime.Instant
import tech.hanasaki.azusa.modules.auth.application.service.TokenPair
import tech.hanasaki.azusa.modules.auth.domain.model.AvatarUrl
import tech.hanasaki.azusa.modules.auth.domain.model.Email
import tech.hanasaki.azusa.modules.auth.domain.model.UserId
import tech.hanasaki.azusa.modules.auth.domain.model.Username


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