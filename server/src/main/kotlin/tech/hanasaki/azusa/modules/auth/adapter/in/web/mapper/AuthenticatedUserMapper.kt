package tech.hanasaki.azusa.modules.auth.adapter.`in`.web.mapper

import tech.hanasaki.azusa.modules.auth.adapter.`in`.web.dto.LoginResponse
import tech.hanasaki.azusa.modules.auth.adapter.`in`.web.dto.TokenResponse
import tech.hanasaki.azusa.modules.auth.application.dto.AuthenticatedUser

fun AuthenticatedUser.toLoginResponse() = LoginResponse(
    user = user.toUserProfile(),
    tokens = TokenResponse(
        accessToken = tokens.accessToken,
        refreshToken = tokens.refreshToken,
        accessTokenExpiredIn = tokens.accessTokenExpiresAt.toEpochMilliseconds(),
        refreshTokenExpiredIn = tokens.refreshTokenExpiresAt.toEpochMilliseconds(),
    ),
)