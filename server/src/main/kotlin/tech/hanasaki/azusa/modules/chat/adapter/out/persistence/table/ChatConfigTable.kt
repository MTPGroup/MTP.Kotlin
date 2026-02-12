package tech.hanasaki.azusa.modules.chat.adapter.out.persistence.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi


object ChatConfigTable : Table("chat_configs") {
    val id = uuid("id")
    val chatId = uuid("chat_id")
    val temperature = decimal("temperature", 3, 2).nullable()
    val maxTokens = integer("max_tokens").nullable()
    val topP = decimal("top_p", 3, 2).nullable()
    val systemPrompt = text("system_prompt").nullable()
    val createdAt = timestamp("created_at").default(Clock.System.now())
    val updatedAt = timestamp("updated_at").default(Clock.System.now())

    override val primaryKey = PrimaryKey(id)
}
