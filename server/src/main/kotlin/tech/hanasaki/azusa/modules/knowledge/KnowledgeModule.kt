package tech.hanasaki.azusa.modules.knowledge

import io.ktor.server.config.*
import org.koin.dsl.module
import tech.hanasaki.azusa.modules.knowledge.adapter.out.embedding.KoogEmbeddingService
import tech.hanasaki.azusa.modules.knowledge.adapter.out.parser.PlaceholderDocumentParser
import tech.hanasaki.azusa.modules.knowledge.adapter.out.persistence.repository.ExposedKnowledgeBaseRepository
import tech.hanasaki.azusa.modules.knowledge.adapter.out.persistence.repository.ExposedKnowledgeDocumentRepository
import tech.hanasaki.azusa.modules.knowledge.adapter.out.persistence.repository.ExposedKnowledgeFileRepository
import tech.hanasaki.azusa.modules.knowledge.adapter.out.vector.PgVectorStore
import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.KnowledgeBaseUseCasePort
import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.KnowledgeFileUseCasePort
import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.KnowledgeSearchUseCasePort
import tech.hanasaki.azusa.modules.knowledge.application.port.out.DocumentParser
import tech.hanasaki.azusa.modules.knowledge.application.port.out.EmbeddingServicePort
import tech.hanasaki.azusa.modules.knowledge.application.port.out.VectorStore
import tech.hanasaki.azusa.modules.knowledge.application.service.KnowledgeBaseService
import tech.hanasaki.azusa.modules.knowledge.application.service.KnowledgeFileService
import tech.hanasaki.azusa.modules.knowledge.application.service.KnowledgeSearchService
import tech.hanasaki.azusa.modules.knowledge.domain.port.KnowledgeBaseRepositoryPort
import tech.hanasaki.azusa.modules.knowledge.domain.port.KnowledgeDocumentRepositoryPort
import tech.hanasaki.azusa.modules.knowledge.domain.port.KnowledgeFileRepositoryPort

fun knowledgeModule(config: ApplicationConfig) = module {
    single<KnowledgeBaseRepositoryPort> { ExposedKnowledgeBaseRepository() }
    single<KnowledgeFileRepositoryPort> { ExposedKnowledgeFileRepository() }
    single<KnowledgeDocumentRepositoryPort> { ExposedKnowledgeDocumentRepository() }

    single<EmbeddingServicePort> { KoogEmbeddingService(get()) }
    single<DocumentParser> { PlaceholderDocumentParser() }
    single<VectorStore> { PgVectorStore() }

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
            embeddingService = get(),
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
}
