package tech.hanasaki.momotalk_plus.core.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.core.data.datasource.local.entity.SessionEntity

@Dao
interface SessionDao {
    @Query("SELECT * FROM SessionEntity LIMIT 1")
    suspend fun get(): List<SessionEntity>

    @Query("SELECT * FROM SessionEntity LIMIT 1")
    suspend fun getCurrentSession(): SessionEntity?

    @Query("SELECT * FROM SessionEntity LIMIT 1")
    fun getCurrentSessionAsFlow(): Flow<SessionEntity?>

    @Query("DELETE FROM SessionEntity")
    suspend fun deleteAllSession()

    @Update
    suspend fun update(session: SessionEntity)

    @Upsert
    suspend fun upsert(session: SessionEntity)
}