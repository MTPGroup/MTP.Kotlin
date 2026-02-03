package tech.hanasaki.azusa.modules.character.infrastructure.persistence.table

import org.jetbrains.exposed.v1.core.Table
import kotlin.uuid.ExperimentalUuidApi


object KnowledgeSubscriptionTable : Table("knowledge_subscriptions") {
    val characterId = uuid("character_id")
    val knowledgeBaseId = uuid("knowledge_base_id")
    val priority = integer("priority").default(0)

    override val primaryKey = PrimaryKey(characterId, knowledgeBaseId)
}
