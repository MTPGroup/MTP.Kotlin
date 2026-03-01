package tech.hanasaki.momotalk_plus.core.auth

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryTokenStore : TokenStore {
    private val state = MutableStateFlow<AuthTokens?>(null)

    override val tokensFlow: Flow<AuthTokens?> = state.asStateFlow()

    override suspend fun get(): AuthTokens? = state.value

    override suspend fun save(tokens: AuthTokens) {
        state.value = tokens
    }

    override suspend fun clear() {
        state.value = null
    }
}
