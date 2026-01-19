package tech.hanasaki.azusa.auth.infrastructure.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("email_otps")
data class OtpEntity(
    @Id
    @get:JvmName("getOtpId")
    val id: UUID,
    val email: String,
    val type: String,
    @Column("code_hash")
    val codeHash: String,
    @Column("is_used")
    val isUsed: Boolean,
    @Column("expires_at")
    val expiresAt: Instant,
    @Column("created_at")
    val createdAt: Instant,
    @Column("used_at")
    val usedAt: Instant?,
) : Persistable<UUID> {
    @Transient
    var isNewRecord: Boolean = false

    override fun getId(): UUID = id
    override fun isNew(): Boolean = isNewRecord
}
