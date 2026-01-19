package tech.hanasaki.azusa.auth.infrastructure.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("refresh_tokens")
data class RefreshTokenEntity(
    @Id
    @get:JvmName("getRefreshTokenId")
    val id: UUID,
    @Column("user_id")
    val userId: UUID,
    @Column("token_hash")
    val tokenHash: String,
    @Column("expires_at")
    val expiresAt: Instant,
    @Column("created_at")
    val createdAt: Instant,
    @Column("is_revoked")
    val isRevoked: Boolean,
) : Persistable<UUID> {
    @Transient
    var isNewRecord: Boolean = false

    override fun getId(): UUID = id
    override fun isNew(): Boolean = isNewRecord
}