package tech.hanasaki.momotalk_plus.core.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
)
