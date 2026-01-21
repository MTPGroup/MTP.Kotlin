package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType

object OtpTable : Table("otp_codes") {
    val id = uuid("id")
    val email = text("email")
    val code = varchar("code", 10)
    val type = enumerationByName("type", 20, OtpType::class)
    val expiresAt = timestamp("expires_at")
    val isUsed = bool("is_used").default(false)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
