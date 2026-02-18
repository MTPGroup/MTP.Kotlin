package tech.hanasaki.azusa.modules.auth.application.dto

import tech.hanasaki.azusa.modules.auth.domain.model.User
import tech.hanasaki.azusa.modules.auth.domain.model.Username
import tech.hanasaki.azusa.shared.domain.model.vo.AvatarUrl
import tech.hanasaki.azusa.shared.domain.model.vo.Email
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import kotlin.time.Instant

data class UserProfileDto(
    val userId: UserId,
    val username: Username,
    val email: Email?,
    val avatar: AvatarUrl?,
    val isEmailVerified: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

fun User.toUserProfileDto(): UserProfileDto = UserProfileDto(
    userId = id,
    username = profile.username,
    email = email,
    avatar = profile.avatar,
    isEmailVerified = isEmailVerified,
    createdAt = profile.createdAt,
    updatedAt = profile.updatedAt
)
