package tech.hanasaki.azusa.modules.chat.adapter.out.persistence.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi


object ChatMemberTable : Table("chat_members") {
    val id = uuid("id")
    val chatId = uuid("chat_id")
    val memberType = varchar("member_type", 20)
    val profileId = uuid("profile_id").nullable()
    val characterId = uuid("character_id").nullable()
    val role = varchar("role", 20).default("member")
    val joinedAt = timestamp("joined_at").default(Clock.System.now())
    val updatedAt = timestamp("updated_at").default(Clock.System.now())

    override val primaryKey = PrimaryKey(id)
}
