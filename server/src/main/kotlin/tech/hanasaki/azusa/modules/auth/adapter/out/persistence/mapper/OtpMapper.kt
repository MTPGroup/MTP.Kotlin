package tech.hanasaki.azusa.modules.auth.adapter.out.persistence.mapper

import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import tech.hanasaki.azusa.shared.domain.model.vo.Email
import tech.hanasaki.azusa.modules.auth.adapter.out.persistence.table.OtpTable
import tech.hanasaki.azusa.modules.auth.domain.model.Otp

object OtpMapper {
    fun toDomain(row: ResultRow): Otp = Otp(
        id = row[OtpTable.id],
        email = Email(row[OtpTable.email]),
        codeHash = row[OtpTable.codeHash],
        type = row[OtpTable.type],
        expiresAt = row[OtpTable.expiresAt],
        isUsed = row[OtpTable.isUsed],
        usedAt = row[OtpTable.usedAt],
    )

    fun toEntity(domain: Otp, target: UpdateBuilder<*>) {
        target[OtpTable.id] = domain.id
        target[OtpTable.email] = domain.email.value
        target[OtpTable.codeHash] = domain.codeHash
        target[OtpTable.type] = domain.type
        target[OtpTable.expiresAt] = domain.expiresAt
        target[OtpTable.isUsed] = domain.isUsed
        target[OtpTable.usedAt] = domain.usedAt
    }
}
