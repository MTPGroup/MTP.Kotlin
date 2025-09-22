package tech.hanasaki.momotalk_plus.core.data.datasource.local

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import kotlinx.coroutines.flow.Flow

class TokenStorage(private val settings: ObservableSettings) {
    fun saveToken(tokens: String) {
        settings.putString(KEY_TOKEN, tokens)
    }

    fun getToken(): String? {
        val token = settings.getStringOrNull(KEY_TOKEN)

        return token
    }

    @OptIn(ExperimentalSettingsApi::class)
    fun getTokenFlow(): Flow<String?> {
        return settings.getStringOrNullFlow(KEY_TOKEN)
    }

    fun clear() {
        settings.remove(KEY_TOKEN)
    }

    companion object {
        private const val KEY_TOKEN = "token"
    }
}