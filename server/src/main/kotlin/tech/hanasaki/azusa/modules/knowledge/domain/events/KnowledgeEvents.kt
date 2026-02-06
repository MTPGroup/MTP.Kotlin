package tech.hanasaki.azusa.modules.knowledge.domain.events

import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeFileId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

sealed class KnowledgeBaseEvent : DomainEvent {
    abstract val knowledgeBaseId: KnowledgeBaseId

    override val aggregateId: String get() = knowledgeBaseId.toString()
    override val aggregateType: String get() = "KnowledgeBase"
    override val occurredOn: Instant get() = Clock.System.now()
}

data class KnowledgeBaseCreated(
    override val knowledgeBaseId: KnowledgeBaseId,
    val authorId: UserId,
    val name: String,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "knowledgeBase.created",
) : KnowledgeBaseEvent()

data class KnowledgeBaseDeleted(
    override val knowledgeBaseId: KnowledgeBaseId,
    val authorId: UserId,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "knowledgeBase.deleted",
) : KnowledgeBaseEvent()

data class FileUploaded(
    val fileId: KnowledgeFileId,
    override val knowledgeBaseId: KnowledgeBaseId,
    val fileName: String,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "knowledgeBase.file.uploaded",
) : KnowledgeBaseEvent()

data class FileProcessed(
    val fileId: KnowledgeFileId,
    override val knowledgeBaseId: KnowledgeBaseId,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "knowledgeBase.file.processed",
) : KnowledgeBaseEvent()

data class FileProcessingFailed(
    val fileId: KnowledgeFileId,
    override val knowledgeBaseId: KnowledgeBaseId,
    val errorMessage: String,
    override val eventId: Uuid = Uuid.random(),
    override val eventType: String = "knowledgeBase.file.processingFailed",
) : KnowledgeBaseEvent()
