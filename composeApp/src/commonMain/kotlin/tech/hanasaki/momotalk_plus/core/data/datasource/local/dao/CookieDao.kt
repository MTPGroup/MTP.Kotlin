package tech.hanasaki.momotalk_plus.core.data.datasource.local.dao

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import tech.hanasaki.momotalk_plus.core.data.datasource.local.entity.CookieEntity

class CookieDao {
    private val cookies = MutableStateFlow<List<CookieEntity>>(emptyList())

    suspend fun getAllCookie(): List<CookieEntity> = cookies.value

    fun getByNameAsFlow(name: String): Flow<CookieEntity?> =
        cookies.map { list -> list.firstOrNull { it.name == name } }

    suspend fun insertOrReplace(cookie: CookieEntity) {
        cookies.value = cookies.value.filterNot { it.name == cookie.name && it.domain == cookie.domain && it.path == cookie.path } + cookie
    }

    suspend fun deleteByName(name: String) {
        cookies.value = cookies.value.filterNot { it.name == name }
    }

    suspend fun deleteAll() {
        cookies.value = emptyList()
    }
}
