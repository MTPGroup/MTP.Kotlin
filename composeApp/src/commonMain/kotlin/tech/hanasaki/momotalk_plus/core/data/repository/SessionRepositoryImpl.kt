@file:OptIn(kotlin.time.ExperimentalTime::class)

package tech.hanasaki.momotalk_plus.core.data.repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Serializable
import tech.hanasaki.momotalk_plus.core.auth.TokenStore
import tech.hanasaki.momotalk_plus.core.domain.model.User
import tech.hanasaki.momotalk_plus.core.domain.repository.SessionRepository
import tech.hanasaki.momotalk_plus.core.network.ApiEnvelope
import tech.hanasaki.momotalk_plus.core.network.NetworkErrorMapper
import tech.hanasaki.momotalk_plus.core.network.RefreshTokenRequest
import tech.hanasaki.momotalk_plus.core.network.refreshAuthTokens

class SessionRepositoryImpl(
    private val client: HttpClient,
    private val tokenStore: TokenStore,
    private val errorMapper: NetworkErrorMapper,
) : SessionRepository {

    override fun obverseUser(): Flow<User?> {
        return tokenStore.tokensFlow
            .map { it?.accessToken }
            .onEach { token ->
                if (token == null) return@onEach
                val me = runCatching { fetchCurrentUser() }.getOrNull()
                if (me == null) {
                    tokenStore.clear()
                }
            }
            .map { token ->
                if (token == null) null else runCatching { fetchCurrentUser() }.getOrNull()
            }
    }

    override fun obverseLoginState(): Flow<Boolean> {
        return tokenStore.tokensFlow.map { it != null }
    }

    override suspend fun refreshCurrentSession() {
        try {
            client.refreshAuthTokens(tokenStore)
        } catch (t: Throwable) {
            errorMapper.map(t)
            tokenStore.clear()
        }
    }

    override suspend fun logout() {
        val refreshToken = tokenStore.get()?.refreshToken
        runCatching {
            client.post("auth/sign-out") {
                if (refreshToken != null) {
                    setBody(RefreshTokenRequest(refreshToken))
                }
            }
        }
        tokenStore.clear()
    }

    private suspend fun fetchCurrentUser(): User {
        val envelope = client.get("auth/me").body<ApiEnvelope<UserPayload>>()
        if (!envelope.success || envelope.data == null) {
            throw IllegalStateException(envelope.message)
        }

        val data = envelope.data
        return User(
            id = data.userId,
            username = data.username,
            avatar = data.avatar,
            createdAt = data.createdAt,
            updatedAt = data.updatedAt,
        )
    }
}

@Serializable
private data class UserPayload(
    val userId: String,
    val email: String,
    val username: String,
    val avatar: String? = null,
    val isEmailVerified: Boolean,
    val createdAt: String,
    val updatedAt: String,
)
