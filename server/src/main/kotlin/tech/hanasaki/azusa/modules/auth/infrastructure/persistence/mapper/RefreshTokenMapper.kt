package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.mapper

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.modules.auth.domain.model.RefreshToken
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.RefreshTokensTable.expiresAt
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.RefreshTokensTable.id
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.RefreshTokensTable.isRevoked
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.RefreshTokensTable.tokenHash
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.RefreshTokensTable.userId
import tech.hanasaki.azusa.shared.domain.model.UserId

object RefreshTokenMapper {
    fun toDomain(row: ResultRow): RefreshToken = RefreshToken(
        id = row[id],
        userId = UserId(row[userId]),
        tokenHash = row[tokenHash],
        expiresAt = row[expiresAt],
        isRevoked = row[isRevoked]
    )

    fun toEntity(domain: RefreshToken, target: UpdateBuilder<*>) {
        target[id] = domain.id
        target[userId] = domain.userId.value
        target[tokenHash] = domain.tokenHash
        target[expiresAt] = domain.expiresAt
        target[isRevoked] = domain.isRevoked
    }
}