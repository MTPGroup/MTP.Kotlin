package tech.hanasaki.azusa.modules.chat.adapter.out.persistence.table

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.json.jsonb
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi


object MessageTable : Table("messages") {
    val id = uuid("id")
    val chatId = uuid("chat_id")
    val senderType = varchar("sender_type", 20)
    val senderProfileId = uuid("sender_profile_id").nullable()
    val senderCharacterId = uuid("sender_character_id").nullable()
    val content = jsonb<JsonElement>("content", Json { prettyPrint = true })
    val metadata = jsonb<JsonObject>("metadata", Json { prettyPrint = true })
    val createdAt = timestamp("created_at").default(Clock.System.now())
    val updatedAt = timestamp("updated_at").default(Clock.System.now())

    override val primaryKey = PrimaryKey(id)
}
