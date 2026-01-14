package tech.hanasaki.azusa.modules.auth.api.mapper

import tech.hanasaki.azusa.modules.auth.api.dto.UserProfile
import tech.hanasaki.azusa.modules.auth.application.result.LoginResult

fun LoginResult.toUserProfile(): UserProfile = UserProfile(
    id = userId.toString(),
    email = email.value,
    name = username.value,
    avatar = avatar?.value,
    isEmailVerified = isEmailVerified,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)