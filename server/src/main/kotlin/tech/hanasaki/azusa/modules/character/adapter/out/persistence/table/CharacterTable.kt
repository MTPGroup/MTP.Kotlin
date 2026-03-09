package tech.hanasaki.azusa.modules.character.adapter.out.persistence.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock


object CharacterTable : Table("characters") {
    val id = uuid("id")
    val authorId = uuid("author_id")
    val name = varchar("name", 100)
    val avatar = text("avatar").nullable()
    val bio = text("bio").nullable()
    val tags = text("tags").default("")
    val originPrompt = text("origin_prompt").nullable()
    val isPublic = bool("is_public")
    val favoriteCount = integer("favorite_count").default(0)
    val chatCount = integer("chat_count").default(0)
    val createdAt = timestamp("created_at").default(Clock.System.now())
    val updatedAt = timestamp("updated_at").default(Clock.System.now())

    override val primaryKey = PrimaryKey(id)
}
