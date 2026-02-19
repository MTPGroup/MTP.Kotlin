package tech.hanasaki.azusa.modules.chat.adapter.out.persistence.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock


object ChatTable : Table("chats") {
    val id = uuid("id")
    val ownerId = uuid("owner_id")
    val name = text("name").nullable()
    val lastMessage = text("last_message").nullable()
    val isGroup = bool("is_group").default(false)
    val createdAt = timestamp("created_at").default(Clock.System.now())
    val updatedAt = timestamp("updated_at").default(Clock.System.now())
    val temporary = bool("temporary").default(false)
    val expiresAt = timestamp("expires_at").nullable()

    override val primaryKey = PrimaryKey(id)
}
