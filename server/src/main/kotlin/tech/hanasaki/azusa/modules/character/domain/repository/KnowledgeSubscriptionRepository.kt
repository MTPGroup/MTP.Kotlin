package tech.hanasaki.azusa.modules.character.domain.repository

import tech.hanasaki.azusa.modules.character.domain.model.KnowledgeSubscription
import tech.hanasaki.azusa.common.kernel.model.CharacterId
import tech.hanasaki.azusa.common.kernel.model.KnowledgeBaseId

interface KnowledgeSubscriptionRepository {
    /**
     * 查询角色订阅的所有知识库
     */
    suspend fun findByCharacterId(characterId: CharacterId): List<KnowledgeSubscription>

    /**
     * 订阅知识库到角色
     */
    suspend fun subscribe(characterId: CharacterId, knowledgeBaseId: KnowledgeBaseId, priority: Int = 0)

    /**
     * 解除角色与知识库的订阅
     */
    suspend fun unsubscribe(characterId: CharacterId, knowledgeBaseId: KnowledgeBaseId)

    /**
     * 解除角色的所有知识库订阅
     */
    suspend fun unsubscribeAll(characterId: CharacterId)
}
