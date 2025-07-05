package tech.hanasaki.momotalk_plus.core.data.datasource.local

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import io.ktor.client.plugins.auth.providers.*
import kotlinx.coroutines.flow.Flow

class TokenStorage(private val settings: ObservableSettings) {
    fun saveTokens(tokens: BearerTokens) {
        settings.putString(KEY_ACCESS_TOKEN, tokens.accessToken)
        settings.putString(KEY_REFRESH_TOKEN, tokens.refreshToken ?: "")
    }

    fun getTokens(): BearerTokens? {
        val accessToken = settings.getStringOrNull(KEY_ACCESS_TOKEN)
        val refreshToken = settings.getStringOrNull(KEY_REFRESH_TOKEN)

        return if (accessToken != null && refreshToken != null) {
            BearerTokens(accessToken, refreshToken)
        } else {
            null
        }
    }

    @OptIn(ExperimentalSettingsApi::class)
    fun getAccessTokenFlow(): Flow<String?> {
        return settings.getStringOrNullFlow(KEY_ACCESS_TOKEN)
    }

    fun clear() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}