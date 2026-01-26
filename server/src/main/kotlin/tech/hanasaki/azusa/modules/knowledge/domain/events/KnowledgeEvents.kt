package tech.hanasaki.azusa.modules.knowledge.domain.events

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.shared.domain.event.DomainEvent
import tech.hanasaki.azusa.shared.domain.model.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.KnowledgeFileId
import tech.hanasaki.azusa.shared.domain.model.UserId
import java.util.*
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class KnowledgeBaseCreatedEvent(
    val knowledgeBaseId: KnowledgeBaseId,
    val authorId: UserId,
    val name: String,
    @Contextual
    override val eventId: UUID = UUID.randomUUID(),
    @Contextual
    override val occurredAt: Instant = Clock.System.now(),
) : DomainEvent

@Serializable
data class KnowledgeBaseDeletedEvent(
    val knowledgeBaseId: KnowledgeBaseId,
    val authorId: UserId,
    @Contextual
    override val eventId: UUID = UUID.randomUUID(),
    @Contextual
    override val occurredAt: Instant = Clock.System.now(),
) : DomainEvent

@Serializable
data class FileUploadedEvent(
    val fileId: KnowledgeFileId,
    val knowledgeBaseId: KnowledgeBaseId,
    val fileName: String,
    @Contextual
    override val eventId: UUID = UUID.randomUUID(),
    @Contextual
    override val occurredAt: Instant = Clock.System.now(),
) : DomainEvent

@Serializable
data class FileProcessedEvent(
    val fileId: KnowledgeFileId,
    val knowledgeBaseId: KnowledgeBaseId,
    @Contextual
    override val eventId: UUID = UUID.randomUUID(),
    @Contextual
    override val occurredAt: Instant = Clock.System.now(),
) : DomainEvent

@Serializable
data class FileProcessingFailedEvent(
    val fileId: KnowledgeFileId,
    val knowledgeBaseId: KnowledgeBaseId,
    val errorMessage: String,
    @Contextual
    override val eventId: UUID = UUID.randomUUID(),
    @Contextual
    override val occurredAt: Instant = Clock.System.now(),
) : DomainEvent
