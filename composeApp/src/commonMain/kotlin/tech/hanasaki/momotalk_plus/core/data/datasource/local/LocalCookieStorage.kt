package tech.hanasaki.momotalk_plus.core.data.datasource.local

import io.ktor.http.Cookie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import tech.hanasaki.momotalk_plus.db.AppDatabase

class LocalCookieStorage(
    db: AppDatabase,
) {
    suspend fun saveCookie(cookie: Cookie, name: String) = Unit

    fun getCookie(name: String): Flow<Nothing?> = flowOf(null)

    suspend fun getAllCookie(): List<Nothing> = emptyList()

    suspend fun removeCookie(name: String) = Unit

    suspend fun clearAll() = Unit
}
