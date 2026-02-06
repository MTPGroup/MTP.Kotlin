package tech.hanasaki.azusa.modules.auth.adapter.out.persistence.mapper

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.modules.auth.adapter.out.persistence.table.RefreshTokenTable
import tech.hanasaki.azusa.modules.auth.domain.model.RefreshToken
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

object RefreshTokenMapper {
    fun toDomain(row: ResultRow): RefreshToken = RefreshToken(
        id = row[RefreshTokenTable.id],
        userId = UserId(row[RefreshTokenTable.userId]),
        tokenHash = row[RefreshTokenTable.tokenHash],
        expiresAt = row[RefreshTokenTable.expiresAt],
        isRevoked = row[RefreshTokenTable.isRevoked]
    )

    fun toEntity(domain: RefreshToken, target: UpdateBuilder<*>) {
        target[RefreshTokenTable.id] = domain.id
        target[RefreshTokenTable.userId] = domain.userId.value
        target[RefreshTokenTable.tokenHash] = domain.tokenHash
        target[RefreshTokenTable.expiresAt] = domain.expiresAt
        target[RefreshTokenTable.isRevoked] = domain.isRevoked
    }
}
