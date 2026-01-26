package tech.hanasaki.azusa.modules.contact.infrastructure.persistence.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp

object ContactTable : Table("contacts") {
    val userId = uuid("uid")
    val characterId = uuid("character_id")
    val nickname = text("nickname").nullable()
    val addedAt = timestamp("added_at")

    override val primaryKey = PrimaryKey(userId, characterId)
}