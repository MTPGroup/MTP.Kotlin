package tech.hanasaki.azusa.modules.setting.domain.port

import tech.hanasaki.azusa.modules.setting.domain.model.Setting
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface SettingRepositoryPort {
    suspend fun findByUserId(userId: UserId): Setting?
    suspend fun save(setting: Setting)
}
