package tech.hanasaki.azusa.modules.chat.infrastructure.persistence.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi


object ChatTable : Table("chats") {
    val id = uuid("id")
    val ownerId = uuid("owner_id")
    val name = text("name").nullable()
    val lastMessage = text("last_message").nullable()
    val isGroup = bool("is_group").default(false)
    val createdAt = timestamp("created_at").default(Clock.System.now())
    val updatedAt = timestamp("updated_at").default(Clock.System.now())

    override val primaryKey = PrimaryKey(id)
}
