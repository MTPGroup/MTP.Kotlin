package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.mapper

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.common.kernel.model.Email
import tech.hanasaki.azusa.modules.auth.domain.model.Otp
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.OtpTable.codeHash
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.OtpTable.email
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.OtpTable.expiresAt
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.OtpTable.id
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.OtpTable.isUsed
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.OtpTable.type
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.OtpTable.usedAt

object OtpMapper {
    fun toDomain(row: ResultRow): Otp = Otp(
        id = row[id],
        email = Email(row[email]),
        codeHash = row[codeHash],
        type = row[type],
        expiresAt = row[expiresAt],
        isUsed = row[isUsed],
        usedAt = row[usedAt],
    )

    fun toEntity(domain: Otp, target: UpdateBuilder<*>) {
        target[id] = domain.id
        target[email] = domain.email.value
        target[codeHash] = domain.codeHash
        target[type] = domain.type
        target[expiresAt] = domain.expiresAt
        target[isUsed] = domain.isUsed
        target[usedAt] = domain.usedAt
    }
}