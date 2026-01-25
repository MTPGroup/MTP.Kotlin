package tech.hanasaki.azusa.modules.auth.application.result

import tech.hanasaki.azusa.modules.auth.domain.port.TokenPair
import tech.hanasaki.azusa.modules.auth.domain.model.AvatarUrl
import tech.hanasaki.azusa.modules.auth.domain.model.Email
import tech.hanasaki.azusa.modules.auth.domain.model.Username
import tech.hanasaki.azusa.shared.domain.model.UserId
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