package tech.hanasaki.azusa.modules.auth.adapter.out.persistence.repository

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.greaterEq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.common.adapter.out.persistence.dbQuery
import tech.hanasaki.azusa.common.domain.model.Email
import tech.hanasaki.azusa.modules.auth.adapter.out.persistence.mapper.OtpMapper
import tech.hanasaki.azusa.modules.auth.adapter.out.persistence.table.OtpTable
import tech.hanasaki.azusa.modules.auth.domain.model.Otp
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import tech.hanasaki.azusa.modules.auth.application.port.out.OtpRepository
import kotlin.time.Clock
import kotlin.time.Instant

class ExposedOtpRepository : OtpRepository {
    override suspend fun save(otp: Otp): Unit = dbQuery {
        val updatedOtp = OtpTable.update({ OtpTable.id eq otp.id }) {
            OtpMapper.toEntity(otp, it)
        }
        if (updatedOtp == 0) {
            OtpTable.insert {
                OtpMapper.toEntity(otp, it)
                it[createdAt] = Clock.System.now()
            }
        }
    }

    override suspend fun findValidLatest(email: Email, type: OtpType): Otp? = dbQuery {
        OtpTable.selectAll()
            .where { (OtpTable.email eq email.value) and (OtpTable.type eq type) and (OtpTable.isUsed eq false) }
            .orderBy(OtpTable.createdAt, SortOrder.DESC)
            .limit(1)
            .map(OtpMapper::toDomain)
            .singleOrNull()
    }

    override suspend fun markAsUsed(otp: Otp): Unit = dbQuery {
        OtpTable.update({ OtpTable.id eq otp.id }) {
            it[isUsed] = true
        }
    }


    override suspend fun countSentAfter(email: Email, type: OtpType, after: Instant): Int = dbQuery {
        OtpTable.selectAll()
            .where {
                (OtpTable.email eq email.value) and
                        (OtpTable.type eq type) and
                        (OtpTable.createdAt greaterEq after)
            }
            .count()
            .toInt()
    }
}
