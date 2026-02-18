package tech.hanasaki.azusa.modules.knowledge

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module
import tech.hanasaki.azusa.modules.knowledge.adapter.`in`.event.FileUploadedHandler
import tech.hanasaki.azusa.modules.knowledge.adapter.`in`.event.KnowledgeBaseDeletedHandler
import tech.hanasaki.azusa.modules.knowledge.adapter.out.embedding.Lc4jEmbeddingService
import tech.hanasaki.azusa.modules.knowledge.adapter.out.ingestor.Lc4jDocumentIngestorAdapter
import tech.hanasaki.azusa.modules.knowledge.adapter.out.parser.Lc4jDocumentParser
import tech.hanasaki.azusa.modules.knowledge.adapter.out.persistence.repository.ExposedKnowledgeBaseRepository
import tech.hanasaki.azusa.modules.knowledge.adapter.out.persistence.repository.ExposedKnowledgeDocumentRepository
import tech.hanasaki.azusa.modules.knowledge.adapter.out.persistence.repository.ExposedKnowledgeFileRepository
import tech.hanasaki.azusa.modules.knowledge.adapter.out.vector.Lc4jVectorStore
import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.KnowledgeBaseUseCasePort
import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.KnowledgeFileUseCasePort
import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.KnowledgeSearchUseCasePort
import tech.hanasaki.azusa.modules.knowledge.application.port.out.DocumentIngestorPort
import tech.hanasaki.azusa.modules.knowledge.application.port.out.DocumentParserPort
import tech.hanasaki.azusa.modules.knowledge.application.port.out.EmbeddingServicePort
import tech.hanasaki.azusa.modules.knowledge.application.port.out.VectorStore
import tech.hanasaki.azusa.modules.knowledge.application.service.KnowledgeBaseService
import tech.hanasaki.azusa.modules.knowledge.application.service.KnowledgeFileService
import tech.hanasaki.azusa.modules.knowledge.application.service.KnowledgeSearchService
import tech.hanasaki.azusa.modules.knowledge.application.service.PendingFileProcessor
import tech.hanasaki.azusa.modules.knowledge.domain.events.FileUploaded
import tech.hanasaki.azusa.modules.knowledge.domain.events.KnowledgeBaseDeleted
import tech.hanasaki.azusa.modules.knowledge.domain.port.KnowledgeBaseRepositoryPort
import tech.hanasaki.azusa.modules.knowledge.domain.port.KnowledgeDocumentRepositoryPort
import tech.hanasaki.azusa.modules.knowledge.domain.port.KnowledgeFileRepositoryPort
import tech.hanasaki.azusa.shared.infrastructure.event.onDomainEvent

fun knowledgeModule() = module {
    single<KnowledgeBaseRepositoryPort> { ExposedKnowledgeBaseRepository() }
    single<KnowledgeFileRepositoryPort> { ExposedKnowledgeFileRepository() }
    single<KnowledgeDocumentRepositoryPort> { ExposedKnowledgeDocumentRepository() }

    single<EmbeddingServicePort> { Lc4jEmbeddingService(get<EmbeddingModel>()) }

    single<DocumentParserPort> { Lc4jDocumentParser(get()) }

    single<VectorStore> {
        Lc4jVectorStore(get<EmbeddingStore<TextSegment>>())
    }

    single<DocumentIngestorPort> {
        Lc4jDocumentIngestorAdapter(
            embeddingModel = get<EmbeddingModel>(),
            embeddingStore = get<EmbeddingStore<TextSegment>>(),
            maxSegmentSize = 500,
            maxOverlapSize = 50,
        )
    }

    single<KnowledgeBaseUseCasePort> {
        KnowledgeBaseService(
            knowledgeBaseRepository = get(),
            fileRepository = get(),
            documentRepository = get(),
            domainEventBus = get(),
            tx = get(),
        )
    }

    single<KnowledgeFileUseCasePort> {
        KnowledgeFileService(
            knowledgeBaseRepository = get(),
            fileRepository = get(),
            documentRepository = get(),
            documentParser = get(),
            documentIngestor = get(),
            domainEventBus = get(),
            tx = get(),
        )
    }

    single<KnowledgeSearchUseCasePort> {
        KnowledgeSearchService(
            knowledgeBaseRepository = get(),
            embeddingService = get(),
            vectorStore = get(),
            tx = get(),
        )
    }

    onDomainEvent<FileUploaded>("knowledgeBase.file.uploaded") {
        FileUploadedHandler(get())
    }
    onDomainEvent<KnowledgeBaseDeleted>("knowledgeBase.deleted") {
        KnowledgeBaseDeletedHandler(get())
    }

    single {
        PendingFileProcessor(
            fileService = get(),
            fileRepository = get(),
            tx = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
            config = PendingFileProcessor.Config(
                enabled = true,
                intervalMs = 30_000,
                maxConcurrency = 5,
                maxRetries = 3,
                batchSize = 10,
            ),
        )
    }
}
