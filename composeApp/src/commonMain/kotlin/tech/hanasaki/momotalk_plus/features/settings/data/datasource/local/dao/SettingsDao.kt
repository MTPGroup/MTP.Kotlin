package tech.hanasaki.momotalk_plus.features.settings.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.features.settings.data.datasource.local.entity.SettingsEntity

@Dao
interface SettingsDao {
    /**
     * 获取用户设置（Flow，自动监听变化）
     */
    @Query("SELECT * FROM SettingsEntity WHERE id = 1")
    fun getSettings(): Flow<SettingsEntity?>

    /**
     * 插入或更新设置
     */
    @Upsert
    suspend fun upsert(settings: SettingsEntity)

    /**
     * 清空所有设置
     */
    @Query("DELETE FROM SessionEntity")
    suspend fun deleteAll()
}

