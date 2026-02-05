package tech.hanasaki.azusa.modules.auth.adapter.out.persistence.mapper

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.common.domain.model.UserId
import tech.hanasaki.azusa.modules.auth.adapter.out.persistence.table.RefreshTokensTable
import tech.hanasaki.azusa.modules.auth.domain.model.RefreshToken

object RefreshTokenMapper {
    fun toDomain(row: ResultRow): RefreshToken = RefreshToken(
        id = row[RefreshTokensTable.id],
        userId = UserId(row[RefreshTokensTable.userId]),
        tokenHash = row[RefreshTokensTable.tokenHash],
        expiresAt = row[RefreshTokensTable.expiresAt],
        isRevoked = row[RefreshTokensTable.isRevoked]
    )

    fun toEntity(domain: RefreshToken, target: UpdateBuilder<*>) {
        target[RefreshTokensTable.id] = domain.id
        target[RefreshTokensTable.userId] = domain.userId.value
        target[RefreshTokensTable.tokenHash] = domain.tokenHash
        target[RefreshTokensTable.expiresAt] = domain.expiresAt
        target[RefreshTokensTable.isRevoked] = domain.isRevoked
    }
}
