package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.repository

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.modules.auth.domain.model.Email
import tech.hanasaki.azusa.modules.auth.domain.model.Otp
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import tech.hanasaki.azusa.modules.auth.domain.repository.OtpRepository
import tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table.OtpTable
import tech.hanasaki.azusa.shared.infrastructure.database.dbQuery
import kotlin.time.Clock

class ExposedOtpRepository : OtpRepository {
    override suspend fun save(otp: Otp): Unit = dbQuery {
        OtpTable.insert {
            it[id] = otp.id
            it[email] = otp.email.value
            it[code] = otp.code
            it[type] = otp.type
            it[expiresAt] = otp.expiresAt
            it[isUsed] = otp.isUsed
            it[createdAt] = Clock.System.now()
        }
    }

    override suspend fun findValidLatest(email: Email, type: OtpType): Otp? = dbQuery {
        OtpTable.selectAll()
            .where { (OtpTable.email eq email.value) and (OtpTable.type eq type) and (OtpTable.isUsed eq false) }
            .orderBy(OtpTable.createdAt, SortOrder.DESC)
            .limit(1)
            .map { row ->
                Otp(
                    id = row[OtpTable.id],
                    email = Email(row[OtpTable.email]),
                    code = row[OtpTable.code],
                    type = row[OtpTable.type],
                    expiresAt = row[OtpTable.expiresAt],
                    isUsed = row[OtpTable.isUsed]
                )
            }.singleOrNull()
    }

    override suspend fun markAsUsed(otp: Otp): Unit = dbQuery {
        OtpTable.update({ OtpTable.id eq otp.id }) {
            it[isUsed] = true
        }
    }
}
