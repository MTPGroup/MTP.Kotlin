package tech.hanasaki.azusa.modules.knowledge.application.port.`in`.dto

import kotlin.uuid.Uuid


data class KnowledgeBaseAuthorView(
    val id: Uuid,
    val name: String,
    val avatar: String?,
)

data class KnowledgeBaseView(
    val id: Uuid,
    val name: String,
    val description: String?,
    val authorId: Uuid,
    val author: KnowledgeBaseAuthorView?,
    val isPublic: Boolean,
    val createdAt: String,
    val updatedAt: String,
)
