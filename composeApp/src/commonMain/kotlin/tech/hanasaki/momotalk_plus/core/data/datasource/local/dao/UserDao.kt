package tech.hanasaki.momotalk_plus.core.data.datasource.local.dao

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import tech.hanasaki.momotalk_plus.core.data.datasource.local.entity.UserEntity

class UserDao {
    private val users = MutableStateFlow<List<UserEntity>>(emptyList())

    suspend fun getUserById(id: String): UserEntity? = users.value.firstOrNull { it.id == id }

    fun getUserByIdAsFlow(id: String): Flow<UserEntity?> =
        users.map { list -> list.firstOrNull { it.id == id } }

    suspend fun update(userEntity: UserEntity) {
        users.value = users.value.map { if (it.id == userEntity.id) userEntity else it }
    }

    suspend fun upsert(userEntity: UserEntity) {
        val existing = users.value.firstOrNull { it.id == userEntity.id }
        users.value = if (existing == null) users.value + userEntity else users.value.map { if (it.id == userEntity.id) userEntity else it }
    }

    suspend fun delete(userEntity: UserEntity) {
        users.value = users.value.filterNot { it.id == userEntity.id }
    }
}
