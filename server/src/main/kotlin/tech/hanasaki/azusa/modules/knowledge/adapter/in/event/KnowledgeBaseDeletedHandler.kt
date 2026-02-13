package tech.hanasaki.azusa.modules.knowledge.adapter.`in`.event

import tech.hanasaki.azusa.modules.knowledge.domain.events.KnowledgeBaseDeleted
import tech.hanasaki.azusa.shared.port.`in`.DomainEventHandlerPort
import tech.hanasaki.azusa.shared.port.out.FileStoragePort

class KnowledgeBaseDeletedHandler(
    private val s3Storage: FileStoragePort,
) : DomainEventHandlerPort<KnowledgeBaseDeleted> {
    override suspend fun invoke(event: KnowledgeBaseDeleted) {
        s3Storage.deleteDirectory("knowledge-base", event.knowledgeBaseId.value.toString())
    }
}