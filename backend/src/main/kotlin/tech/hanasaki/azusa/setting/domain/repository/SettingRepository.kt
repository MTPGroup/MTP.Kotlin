package tech.hanasaki.azusa.setting.domain.repository

import tech.hanasaki.azusa.setting.domain.model.Setting
import tech.hanasaki.azusa.shared.UserId

interface SettingRepository {
    fun findByUserId(userId: UserId): Setting?
    fun save(setting: Setting)
}