package tech.hanasaki.azusa.modules.auth.api.mapper

import tech.hanasaki.azusa.modules.auth.api.dto.UserProfile
import tech.hanasaki.azusa.modules.auth.application.result.LoginResult
import tech.hanasaki.azusa.modules.auth.domain.model.User

fun LoginResult.toUserProfile(): UserProfile = UserProfile(
    id = userId.value.toString(),
    email = email.value,
    name = username.value,
    avatar = avatar?.value,
    isEmailVerified = isEmailVerified,
    createdAt = createdAt.toString(),
    updatedAt = updatedAt.toString(),
)

fun User.toUserProfile(): UserProfile = UserProfile(
    id = id.value.toString(),
    email = email?.value ?: "",
    name = profile.username.value,
    avatar = profile.avatar?.value,
    isEmailVerified = isEmailVerified,
    createdAt = profile.createdAt.toString(),
    updatedAt = profile.updatedAt.toString(),
)
