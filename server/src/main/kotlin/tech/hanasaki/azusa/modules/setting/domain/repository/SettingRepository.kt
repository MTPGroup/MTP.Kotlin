package tech.hanasaki.azusa.modules.setting.domain.repository

import tech.hanasaki.azusa.modules.setting.domain.model.Setting
import tech.hanasaki.azusa.common.kernel.model.UserId

interface SettingRepository {
    suspend fun findByUserId(userId: UserId): Setting?
    suspend fun save(setting: Setting)
}
