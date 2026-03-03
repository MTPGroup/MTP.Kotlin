package tech.hanasaki.momotalk_plus.core.auth

import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PersistentTokenStore(
    private val settings: Settings = Settings(),
    private val json: Json = Json { ignoreUnknownKeys = true },
) : TokenStore {

    private val state = MutableStateFlow(loadFromStorage())

    override val tokensFlow: Flow<AuthTokens?> = state.asStateFlow()

    override suspend fun get(): AuthTokens? = state.value

    override suspend fun save(tokens: AuthTokens) {
        settings.putString(KEY_AUTH_TOKENS, json.encodeToString(tokens))
        state.value = tokens
    }

    override suspend fun clear() {
        settings.remove(KEY_AUTH_TOKENS)
        state.value = null
    }

    private fun loadFromStorage(): AuthTokens? {
        val raw = settings.getStringOrNull(KEY_AUTH_TOKENS) ?: return null
        return runCatching { json.decodeFromString<AuthTokens>(raw) }.getOrNull()
    }

    private companion object {
        const val KEY_AUTH_TOKENS = "auth_tokens"
    }
}
