package tech.hanasaki.momotalk_plus.core.data.datasource.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.core.data.datasource.local.entity.UserEntity

@Dao
interface UserDao {
    @Query("SELECT * FROM UserEntity WHERE id = :id")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM UserEntity WHERE id = :id")
    fun getUserByIdAsFlow(id: String): Flow<UserEntity?>

    @Update
    suspend fun update(userEntity: UserEntity)

    @Upsert
    suspend fun upsert(userEntity: UserEntity)

    @Delete
    suspend fun delete(userEntity: UserEntity)
}