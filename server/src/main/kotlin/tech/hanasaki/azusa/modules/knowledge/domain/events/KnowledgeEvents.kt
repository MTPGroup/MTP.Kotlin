package tech.hanasaki.azusa.modules.knowledge.domain.events

import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.common.domain.event.DomainEvent
import tech.hanasaki.azusa.common.domain.model.KnowledgeBaseId
import tech.hanasaki.azusa.common.domain.model.KnowledgeFileId
import tech.hanasaki.azusa.common.domain.model.UserId
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid


@Serializable
sealed class KnowledgeBaseEvent : DomainEvent {
    abstract val knowledgeBaseId: KnowledgeBaseId

    override val aggregateId: String get() = knowledgeBaseId.toString()
    override val aggregateType: String get() = "KnowledgeBase"
    override val occurredOn: Instant get() = Clock.System.now()

}

@Serializable
data class KnowledgeBaseCreated(
    override val knowledgeBaseId: KnowledgeBaseId,
    val authorId: UserId,
    val name: String,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "knowledgeBase.created",
) : KnowledgeBaseEvent()


@Serializable
data class KnowledgeBaseDeleted(
    override val knowledgeBaseId: KnowledgeBaseId,
    val authorId: UserId,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "knowledgeBase.deleted",
) : KnowledgeBaseEvent()


@Serializable
data class FileUploaded(
    val fileId: KnowledgeFileId,
    override val knowledgeBaseId: KnowledgeBaseId,
    val fileName: String,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "knowledgeBase.file.uploaded",
) : KnowledgeBaseEvent()


@Serializable
data class FileProcessed(
    val fileId: KnowledgeFileId,
    override val knowledgeBaseId: KnowledgeBaseId,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "knowledgeBase.file.processed",
) : KnowledgeBaseEvent()


@Serializable
data class FileProcessingFailed(
    val fileId: KnowledgeFileId,
    override val knowledgeBaseId: KnowledgeBaseId,
    val errorMessage: String,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "knowledgeBase.file.processingFailed",
) : KnowledgeBaseEvent()
