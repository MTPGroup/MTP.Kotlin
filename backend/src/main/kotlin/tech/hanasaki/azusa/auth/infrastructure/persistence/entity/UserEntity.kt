package tech.hanasaki.azusa.auth.infrastructure.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table
import tech.hanasaki.azusa.auth.domain.model.UserStatus
import java.time.Instant
import java.util.*

@Table("users")
data class UserEntity(
    @Id
    @get:JvmName("getUserId")
    val id: UUID,
    val email: String?,
    @Column("password_hash")
    val passwordHash: String,
    val status: UserStatus,
    @Column("email_verified")
    val emailVerified: Boolean,
    @Column("banned_until")
    val bannedUntil: Instant?,
    @Column("created_at")
    val createdAt: Instant,
    @Column("updated_at")
    val updatedAt: Instant,
    @MappedCollection(idColumn = "uid")
    val profile: UserProfileEntity,
) : Persistable<UUID> {
    @Transient
    var isNewRecord: Boolean = false

    override fun getId(): UUID = id
    override fun isNew(): Boolean = isNewRecord
}

@Table("profiles")
data class UserProfileEntity(
    @Id
    val uid: UUID,
    val username: String,
    val avatar: String?,
    @Column("created_at")
    val createdAt: Instant,
    @Column("updated_at")
    val updatedAt: Instant,
)