package tech.hanasaki.momotalk_plus.core.data.datasource.local.dao

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import tech.hanasaki.momotalk_plus.core.data.datasource.local.entity.SettingsEntity

class SettingsDao {
    private val settings = MutableStateFlow<SettingsEntity?>(null)

    fun getSettings(): Flow<SettingsEntity?> = settings

    suspend fun upsert(settingsEntity: SettingsEntity) {
        settings.value = settingsEntity
    }

    suspend fun deleteAll() {
        settings.value = null
    }
}
