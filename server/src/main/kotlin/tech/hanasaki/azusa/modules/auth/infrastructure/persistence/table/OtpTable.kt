package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType

object OtpTable : UUIDTable("otp_codes") {
    val email = text("email")
    val code = varchar("code", 10)
    val type = enumerationByName("type", 20, OtpType::class)
    val expiresAt = timestamp("expires_at")
    val isUsed = bool("is_used").default(false)
    val createdAt = timestamp("created_at")
}