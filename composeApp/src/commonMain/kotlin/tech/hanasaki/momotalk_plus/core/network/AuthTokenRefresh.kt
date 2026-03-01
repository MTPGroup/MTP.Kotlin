package tech.hanasaki.momotalk_plus.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import kotlinx.serialization.Serializable
import tech.hanasaki.momotalk_plus.core.auth.AuthTokens
import tech.hanasaki.momotalk_plus.core.auth.TokenStore

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String,
)

@Serializable
private data class RefreshLoginPayload(
    val tokens: RefreshTokenPayload,
)

@Serializable
private data class RefreshTokenPayload(
    val accessToken: String,
    val refreshToken: String,
)

suspend fun HttpClient.refreshAuthTokens(
    tokenStore: TokenStore,
): AuthTokens? {
    val current = tokenStore.get() ?: return null

    val refreshed = runCatching {
        post("auth/refresh") {
            setBody(RefreshTokenRequest(current.refreshToken))
        }.body<ApiEnvelope<RefreshLoginPayload>>()
    }.getOrNull()

    val data = refreshed?.data ?: run {
        tokenStore.clear()
        return null
    }

    val tokens = AuthTokens(
        accessToken = data.tokens.accessToken,
        refreshToken = data.tokens.refreshToken,
    )
    tokenStore.save(tokens)
    return tokens
}
