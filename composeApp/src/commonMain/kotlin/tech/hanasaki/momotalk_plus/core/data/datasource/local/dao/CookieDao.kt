package tech.hanasaki.momotalk_plus.core.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.core.data.datasource.local.entity.CookieEntity

/**
 * Cookie DAO - 数据库访问对象
 * 封装所有 Cookie 相关的数据库操作
 */
@Dao
interface CookieDao {
    @Query("SELECT * FROM CookieEntity")
    suspend fun getAllCookie(): List<CookieEntity>

    @Query("SELECT * FROM CookieEntity WHERE name = :name")
    fun getByNameAsFlow(name: String): Flow<CookieEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(cookie: CookieEntity)

    @Query("DELETE FROM CookieEntity WHERE name = :name")
    suspend fun deleteByName(name: String)

    @Query("DELETE FROM CookieEntity")
    suspend fun deleteAll()
}