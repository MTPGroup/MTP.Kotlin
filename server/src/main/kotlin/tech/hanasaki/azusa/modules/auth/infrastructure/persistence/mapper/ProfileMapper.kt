package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.mapper

import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.modules.auth.domain.model.UserProfile
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.ProfileTable

object ProfileMapper {
    fun toEntity(domain: UserProfile, target: UpdateBuilder<*>) {
        target[ProfileTable.uid] = domain.userId.value
        target[ProfileTable.username] = domain.username.value
        target[ProfileTable.avatar] = domain.avatar?.value
    }
}