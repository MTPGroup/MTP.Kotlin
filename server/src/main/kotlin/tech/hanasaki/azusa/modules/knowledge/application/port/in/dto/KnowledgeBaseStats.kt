package tech.hanasaki.azusa.modules.knowledge.application.port.`in`.dto

import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId

data class KnowledgeBaseStats(
    val knowledgeBaseId: KnowledgeBaseId,
    val fileCount: Int,
    val documentCount: Long,
)
