package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import kotlin.uuid.ExperimentalUuidApi


object OtpTable : Table("email_otps") {
    val id = uuid("id")
    val email = text("email")
    val type = enumerationByName<OtpType>("type", 30)
    val codeHash = text("code_hash")
    val usedAt = timestamp("used_at").nullable()
    val isUsed = bool("is_used")
    val expiresAt = timestamp("expires_at")
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
}
