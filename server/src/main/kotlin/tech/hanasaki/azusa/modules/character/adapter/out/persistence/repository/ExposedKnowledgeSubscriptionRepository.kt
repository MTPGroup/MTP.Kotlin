package tech.hanasaki.azusa.modules.character.adapter.out.persistence.repository

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import tech.hanasaki.azusa.modules.character.adapter.out.persistence.table.KnowledgeSubscriptionTable
import tech.hanasaki.azusa.modules.character.domain.model.KnowledgeSubscription
import tech.hanasaki.azusa.modules.character.domain.port.KnowledgeSubscriptionRepositoryPort
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId

class ExposedKnowledgeSubscriptionRepository : KnowledgeSubscriptionRepositoryPort {
    override suspend fun findByCharacterId(characterId: CharacterId): List<KnowledgeSubscription> =
        KnowledgeSubscriptionTable.selectAll()
            .where { KnowledgeSubscriptionTable.characterId eq characterId.value }
            .map { row ->
                KnowledgeSubscription(
                    characterId = CharacterId(row[KnowledgeSubscriptionTable.characterId]),
                    knowledgeBaseId = KnowledgeBaseId(row[KnowledgeSubscriptionTable.knowledgeBaseId]),
                    priority = row[KnowledgeSubscriptionTable.priority],
                )
            }

    override suspend fun subscribe(characterId: CharacterId, knowledgeBaseId: KnowledgeBaseId, priority: Int) {
        KnowledgeSubscriptionTable.upsert {
            it[KnowledgeSubscriptionTable.characterId] = characterId.value
            it[KnowledgeSubscriptionTable.knowledgeBaseId] = knowledgeBaseId.value
            it[KnowledgeSubscriptionTable.priority] = priority
        }
    }

    override suspend fun unsubscribe(characterId: CharacterId, knowledgeBaseId: KnowledgeBaseId) {
        KnowledgeSubscriptionTable.deleteWhere {
            (KnowledgeSubscriptionTable.characterId eq characterId.value) and
                    (KnowledgeSubscriptionTable.knowledgeBaseId eq knowledgeBaseId.value)
        }
    }

    override suspend fun unsubscribeAll(characterId: CharacterId) {
        KnowledgeSubscriptionTable.deleteWhere {
            KnowledgeSubscriptionTable.characterId eq characterId.value
        }
    }
}
