package tech.hanasaki.azusa.modules.character.domain.model

import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId

/**
 * 角色订阅的知识库
 */
data class KnowledgeSubscription(
    val characterId: CharacterId,
    val knowledgeBaseId: KnowledgeBaseId,
    val priority: Int = 0,
)
