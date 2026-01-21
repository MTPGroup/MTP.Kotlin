package tech.hanasaki.azusa.character.infrastructure.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.UUID

@Table("characters")
data class CharacterEntity(
    @Id
    @get:JvmName("getCharacterId")
    val id: UUID,
    @Column("author_id")
    val authorId: UUID,
    val name: String,
    val avatar: String?,
    val bio: String?,
    @Column("origin_prompt")
    val originPrompt: String?,
    @Column("is_public")
    val isPublic: Boolean,
    @Column("created_at")
    val createdAt: Instant,
    @Column("updated_at")
    val updatedAt: Instant,
) : Persistable<UUID> {
    @Transient
    var isNewRecord: Boolean = false

    override fun getId(): UUID = id
    override fun isNew(): Boolean = isNewRecord
}
