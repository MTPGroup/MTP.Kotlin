package tech.hanasaki.momotalk_plus.core.data.datasource.local

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.getStringOrNullFlow
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class AuthSettings(private val settings: ObservableSettings) {

    fun getIdToken(): String? =
        settings.getStringOrNull(KEY_ID_TOKEN)

    fun getRefreshToken(): String? =
        settings.getStringOrNull(KEY_REFRESH_TOKEN)

    fun getExpiresAt(): Long? =
        settings.getLongOrNull(KEY_EXPIRES_AT)

    @OptIn(ExperimentalSettingsApi::class)
    fun getLoggedInUserUidFlow(): Flow<String?> =
        settings.getStringOrNullFlow(KEY_ID_TOKEN)

    @OptIn(ExperimentalTime::class)
    suspend fun saveAuthTokens(
        uid: String,
        idToken: String,
        refreshToken: String,
        expiresIn: Long
    ) {
        val expiresAt = Clock.System.now().epochSeconds + expiresIn
        settings.putString(KEY_LOGGED_IN_USER_UID, uid)
        settings.putString(KEY_ID_TOKEN, idToken)
        settings.putString(KEY_REFRESH_TOKEN, refreshToken)
        settings.putLong(KEY_EXPIRES_AT, expiresAt)
    }

    suspend fun clear() {
        settings.remove(KEY_LOGGED_IN_USER_UID)
        settings.remove(KEY_ID_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        settings.remove(KEY_EXPIRES_AT)
    }

    companion object {
        private const val KEY_LOGGED_IN_USER_UID = "logged_in_user_uid"
        private const val KEY_ID_TOKEN = "auth_id_token"
        private const val KEY_REFRESH_TOKEN = "auth_refresh_token"
        private const val KEY_EXPIRES_AT = "auth_expires_at"
    }
}