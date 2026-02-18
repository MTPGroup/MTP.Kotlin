package tech.hanasaki.azusa.modules.auth.adapter.`in`.web.mapper

import tech.hanasaki.azusa.modules.auth.adapter.`in`.web.dto.UserProfile
import tech.hanasaki.azusa.modules.auth.application.dto.UserProfileDto

fun UserProfileDto.toUserProfile(): UserProfile = UserProfile(
    userId = userId.value,
    email = email?.value ?: "",
    username = username.value,
    avatar = avatar?.value,
    isEmailVerified = isEmailVerified,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
