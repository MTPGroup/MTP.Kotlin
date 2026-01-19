package tech.hanasaki.azusa.setting.infrastructure.persistence.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import tech.hanasaki.azusa.setting.infrastructure.persistence.entity.SettingEntity
import java.util.*

@Repository
interface SpringDataSettingEntityRepository : CrudRepository<SettingEntity, UUID> {
    fun findByUid(uid: UUID): SettingEntity?
}
