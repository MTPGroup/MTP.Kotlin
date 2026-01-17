package tech.hanasaki.azusa.auth.internal.api.mapper

import tech.hanasaki.azusa.auth.internal.api.dto.UserProfile
import tech.hanasaki.azusa.auth.internal.application.result.LoginResult
import tech.hanasaki.azusa.auth.internal.domain.model.User

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
