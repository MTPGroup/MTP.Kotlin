package tech.hanasaki.azusa.modules.character.domain.model

import tech.hanasaki.azusa.common.domain.model.CharacterId
import tech.hanasaki.azusa.common.domain.model.KnowledgeBaseId

/**
 * 角色订阅的知识库
 */
data class KnowledgeSubscription(
    val characterId: CharacterId,
    val knowledgeBaseId: KnowledgeBaseId,
    val priority: Int = 0,
)
