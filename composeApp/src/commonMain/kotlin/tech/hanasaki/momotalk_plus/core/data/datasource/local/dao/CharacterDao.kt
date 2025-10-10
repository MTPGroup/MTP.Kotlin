package tech.hanasaki.momotalk_plus.core.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.core.data.datasource.local.entity.CharacterEntity

/**
 * Character DAO - 数据库访问对象
 * 封装所有 Character 相关的数据库操作
 */
@Dao
interface CharacterDao {
    @Query("SELECT * FROM CharacterEntity")
    fun getAllCharacter(): Flow<List<CharacterEntity>>

    @Query("SELECT * FROM CharacterEntity WHERE id = :id")
    fun getCharacterById(id: String): Flow<CharacterEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(character: CharacterEntity)

    @Query("DELETE FROM CharacterEntity WHERE id = :id")
    suspend fun deleteCharacter(id: String)

    @Query("DELETE FROM CharacterEntity")
    suspend fun deleteAllCharacter()
}
