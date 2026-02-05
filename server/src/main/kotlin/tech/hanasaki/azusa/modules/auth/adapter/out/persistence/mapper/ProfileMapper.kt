package tech.hanasaki.azusa.modules.auth.adapter.out.persistence.mapper

import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.modules.auth.adapter.out.persistence.table.ProfileTable
import tech.hanasaki.azusa.modules.auth.domain.model.UserProfile

object ProfileMapper {
    fun toEntity(domain: UserProfile, target: UpdateBuilder<*>) {
        target[ProfileTable.uid] = domain.userId.value
        target[ProfileTable.username] = domain.username.value
        target[ProfileTable.avatar] = domain.avatar?.value
    }
}
