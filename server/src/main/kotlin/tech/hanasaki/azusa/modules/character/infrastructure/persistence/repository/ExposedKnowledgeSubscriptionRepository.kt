package tech.hanasaki.azusa.modules.character.infrastructure.persistence.repository

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import tech.hanasaki.azusa.common.adapter.out.persistence.dbQuery
import tech.hanasaki.azusa.common.domain.model.CharacterId
import tech.hanasaki.azusa.common.domain.model.KnowledgeBaseId
import tech.hanasaki.azusa.modules.character.domain.model.KnowledgeSubscription
import tech.hanasaki.azusa.modules.character.domain.repository.KnowledgeSubscriptionRepository
import tech.hanasaki.azusa.modules.character.infrastructure.persistence.table.KnowledgeSubscriptionTable

class ExposedKnowledgeSubscriptionRepository : KnowledgeSubscriptionRepository {
    override suspend fun findByCharacterId(characterId: CharacterId): List<KnowledgeSubscription> = dbQuery {
        KnowledgeSubscriptionTable.selectAll()
            .where { KnowledgeSubscriptionTable.characterId eq characterId.value }
            .map { row ->
                KnowledgeSubscription(
                    characterId = CharacterId(row[KnowledgeSubscriptionTable.characterId]),
                    knowledgeBaseId = KnowledgeBaseId(row[KnowledgeSubscriptionTable.knowledgeBaseId]),
                    priority = row[KnowledgeSubscriptionTable.priority],
                )
            }
    }

    override suspend fun subscribe(characterId: CharacterId, knowledgeBaseId: KnowledgeBaseId, priority: Int): Unit =
        dbQuery {
            KnowledgeSubscriptionTable.upsert {
                it[KnowledgeSubscriptionTable.characterId] = characterId.value
                it[KnowledgeSubscriptionTable.knowledgeBaseId] = knowledgeBaseId.value
                it[KnowledgeSubscriptionTable.priority] = priority
            }
        }

    override suspend fun unsubscribe(characterId: CharacterId, knowledgeBaseId: KnowledgeBaseId): Unit = dbQuery {
        KnowledgeSubscriptionTable.deleteWhere {
            (KnowledgeSubscriptionTable.characterId eq characterId.value) and
                    (KnowledgeSubscriptionTable.knowledgeBaseId eq knowledgeBaseId.value)
        }
    }

    override suspend fun unsubscribeAll(characterId: CharacterId): Unit = dbQuery {
        KnowledgeSubscriptionTable.deleteWhere {
            KnowledgeSubscriptionTable.characterId eq characterId.value
        }
    }
}
