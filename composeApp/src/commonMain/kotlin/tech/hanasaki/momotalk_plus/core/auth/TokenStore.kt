package tech.hanasaki.momotalk_plus.core.auth

import kotlinx.coroutines.flow.Flow

interface TokenStore {
    val tokensFlow: Flow<AuthTokens?>

    suspend fun get(): AuthTokens?

    suspend fun save(tokens: AuthTokens)

    suspend fun clear()
}
