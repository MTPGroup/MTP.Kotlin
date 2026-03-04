@file:OptIn(kotlin.time.ExperimentalTime::class)

package tech.hanasaki.momotalk_plus.core.data.repository

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
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
    private val userRefreshSignal = MutableStateFlow(0L)

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeUser(): Flow<User?> =
        combine(
            tokenStore.tokensFlow
                .map { it?.accessToken }
                .distinctUntilChanged(),
            userRefreshSignal,
        ) { token, _ -> token }
            .mapLatest { token ->
                if (token == null) return@mapLatest null
                try {
                    fetchCurrentUser()
                } catch (ce: CancellationException) {
                    throw ce
                } catch (_: Throwable) {
                    tokenStore.clear()
                    null
                }
            }

    override fun observeLoginState(): Flow<Boolean> =
        tokenStore.tokensFlow
            .map { it != null }
            .distinctUntilChanged()

    override suspend fun refreshCurrentSession() {
        try {
            client.refreshAuthTokens(tokenStore)
            refreshCurrentUser()
        } catch (t: Throwable) {
            errorMapper.map(t)
            tokenStore.clear()
        }
    }

    override suspend fun refreshCurrentUser() {
        userRefreshSignal.value += 1
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
