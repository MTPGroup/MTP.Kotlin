package tech.hanasaki.azusa.modules.character.infrastructure.persistence.table

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.datetime

object CharacterTable : Table("characters") {
    val id = uuid("id")
    val authorId = uuid("author_id")
    val name = varchar("name", 100)
    val avatar = text("avatar").nullable()
    val bio = text("bio").nullable()
    val originPrompt = text("origin_prompt").nullable()
    val isPublic = bool("is_public")
    val createdAt = datetime("created_at").default(Clock.System.now().toLocalDateTime(TimeZone.UTC))
    val updatedAt = datetime("updated_at").default(Clock.System.now().toLocalDateTime(TimeZone.UTC))

    override val primaryKey = PrimaryKey(id)
}
