package tech.hanasaki.azusa.modules.knowledge.adapter.`in`.event

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.KnowledgeFileUseCasePort
import tech.hanasaki.azusa.modules.knowledge.domain.events.FileUploaded
import tech.hanasaki.azusa.shared.port.`in`.DomainEventHandlerPort

class FileUploadedHandler(
    private val fileService: KnowledgeFileUseCasePort,
) : DomainEventHandlerPort<FileUploaded> {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override suspend fun invoke(event: FileUploaded) {
        scope.launch {
            fileService.processFile(event.fileId)
        }
    }
}
