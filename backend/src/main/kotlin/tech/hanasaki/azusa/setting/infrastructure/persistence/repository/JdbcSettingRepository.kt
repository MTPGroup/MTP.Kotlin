package tech.hanasaki.azusa.setting.infrastructure.persistence.repository

import org.springframework.stereotype.Repository
import tech.hanasaki.azusa.setting.domain.model.Setting
import tech.hanasaki.azusa.setting.domain.repository.SettingRepository
import tech.hanasaki.azusa.setting.infrastructure.persistence.mapper.SettingMapper
import tech.hanasaki.azusa.common.UserId

@Repository
class JdbcSettingRepository(
    private val settingRepository: SpringDataSettingEntityRepository,
    private val mapper: SettingMapper,
) : SettingRepository {
    override fun findByUserId(userId: UserId): Setting? =
        settingRepository.findByUid(userId.value)?.let(mapper::toDomain)

    override fun save(setting: Setting) {
        val exists = settingRepository.existsById(setting.uid.value)
        val entity = mapper.toEntity(setting, !exists)
        settingRepository.save(entity)
    }
}