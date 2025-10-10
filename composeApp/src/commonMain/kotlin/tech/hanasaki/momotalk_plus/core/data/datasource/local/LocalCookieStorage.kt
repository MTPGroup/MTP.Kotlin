package tech.hanasaki.momotalk_plus.core.data.datasource.local

import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.hanasaki.momotalk_plus.core.data.datasource.mapper.CookieMapper.toDomain
import tech.hanasaki.momotalk_plus.core.data.datasource.mapper.CookieMapper.toEntity
import tech.hanasaki.momotalk_plus.core.domain.model.SerializableCookie
import tech.hanasaki.momotalk_plus.db.AppDatabase


class LocalCookieStorage(
    db: AppDatabase,
) {
    private val cookieDao = db.cookieDao()

    suspend fun saveCookie(cookie: Cookie, name: String) {
        try {
            val serializableCookie = SerializableCookie(
                name = cookie.name,
                value = cookie.value,
                maxAge = cookie.maxAge,
                expires = cookie.expires?.toString(),
                domain = cookie.domain,
                path = cookie.path,
                secure = cookie.secure,
                httpOnly = cookie.httpOnly,
                extensions = cookie.extensions
            )
            cookieDao.insertOrReplace(serializableCookie.toEntity())
        } catch (e: Exception) {
            println("Error saving cookie: ${e.message}")
        }
    }

    fun getCookie(name: String): Flow<SerializableCookie?> =
        cookieDao.getByNameAsFlow(name).map { it?.toDomain() }

    suspend fun getAllCookie(): List<SerializableCookie> =
        cookieDao.getAllCookie().map { it.toDomain() }

    suspend fun removeCookie(name: String) =
        cookieDao.deleteByName(name)

    suspend fun clearAll() =
        cookieDao.deleteAll()
}