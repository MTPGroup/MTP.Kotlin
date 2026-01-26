package tech.hanasaki.azusa.modules.knowledge.application.command

import kotlinx.serialization.json.JsonObject

data class CreateKnowledgeBaseCommand(
    val name: String,
    val description: String?,
    val isPublic: Boolean,
)

data class UpdateKnowledgeBaseCommand(
    val name: String,
    val description: String?,
    val isPublic: Boolean,
)

data class CreateDocumentCommand(
    val content: String,
    val metadata: JsonObject,
)
