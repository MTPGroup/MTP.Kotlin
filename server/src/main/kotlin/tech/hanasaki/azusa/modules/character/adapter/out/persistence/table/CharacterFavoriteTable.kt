package tech.hanasaki.azusa.modules.character.adapter.out.persistence.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock

object CharacterFavoriteTable : Table("character_favorites") {
    val userId = uuid("user_id")
    val characterId = uuid("character_id")
    val createdAt = timestamp("created_at").default(Clock.System.now())

    override val primaryKey = PrimaryKey(userId, characterId)
}

