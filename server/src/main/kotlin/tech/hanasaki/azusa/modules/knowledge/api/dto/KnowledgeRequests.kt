package tech.hanasaki.azusa.modules.knowledge.api.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.modules.knowledge.application.command.CreateKnowledgeBaseCommand
import tech.hanasaki.azusa.modules.knowledge.application.command.UpdateKnowledgeBaseCommand
import java.util.*

@Serializable
data class CreateKnowledgeBaseRequest(
    val name: String,
    val description: String?,
    val isPublic: Boolean = false,
) {
    fun toCommand(): CreateKnowledgeBaseCommand = CreateKnowledgeBaseCommand(
        name = name,
        description = description,
        isPublic = isPublic,
    )
}

@Serializable
data class UpdateKnowledgeBaseRequest(
    val name: String,
    val description: String?,
    val isPublic: Boolean,
) {
    fun toCommand(): UpdateKnowledgeBaseCommand = UpdateKnowledgeBaseCommand(
        name = name,
        description = description,
        isPublic = isPublic,
    )
}

@Serializable
data class UploadFileRequest(
    val fileName: String,
    val filePath: String,
    val fileSize: Long?,
    val fileType: String?,
)

@Serializable
data class SearchKnowledgeRequest(
    val query: String,
    val knowledgeBaseIds: List<@Contextual UUID>? = null,
    val threshold: Float = 0.7f,
    val limit: Int = 10,
)
