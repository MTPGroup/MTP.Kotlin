package tech.hanasaki.azusa.modules.knowledge

import io.ktor.server.config.*
import org.koin.dsl.module
import tech.hanasaki.azusa.modules.knowledge.application.service.KnowledgeBaseService
import tech.hanasaki.azusa.modules.knowledge.application.service.KnowledgeFileService
import tech.hanasaki.azusa.modules.knowledge.application.service.KnowledgeSearchService
import tech.hanasaki.azusa.modules.knowledge.domain.port.DocumentParser
import tech.hanasaki.azusa.modules.knowledge.domain.port.EmbeddingService
import tech.hanasaki.azusa.modules.knowledge.domain.port.VectorStore
import tech.hanasaki.azusa.modules.knowledge.domain.repository.KnowledgeBaseRepository
import tech.hanasaki.azusa.modules.knowledge.domain.repository.KnowledgeDocumentRepository
import tech.hanasaki.azusa.modules.knowledge.domain.repository.KnowledgeFileRepository
import tech.hanasaki.azusa.modules.knowledge.infrastructure.embedding.PlaceholderEmbeddingService
import tech.hanasaki.azusa.modules.knowledge.infrastructure.parser.PlaceholderDocumentParser
import tech.hanasaki.azusa.modules.knowledge.infrastructure.persistence.repository.ExposedKnowledgeBaseRepository
import tech.hanasaki.azusa.modules.knowledge.infrastructure.persistence.repository.ExposedKnowledgeDocumentRepository
import tech.hanasaki.azusa.modules.knowledge.infrastructure.persistence.repository.ExposedKnowledgeFileRepository
import tech.hanasaki.azusa.modules.knowledge.infrastructure.vector.PgVectorStore
import tech.hanasaki.azusa.common.kernel.event.EventPublisher

fun knowledgeModule(config: ApplicationConfig) = module {
    // Repositories
    single<KnowledgeBaseRepository> { ExposedKnowledgeBaseRepository() }
    single<KnowledgeFileRepository> { ExposedKnowledgeFileRepository() }
    single<KnowledgeDocumentRepository> { ExposedKnowledgeDocumentRepository() }

    // Ports - 占位实现，可替换为实际实现
    single<EmbeddingService> { PlaceholderEmbeddingService() }
    single<DocumentParser> { PlaceholderDocumentParser() }
    single<VectorStore> { PgVectorStore() }

    // Services
    factory {
        KnowledgeBaseService(
            knowledgeBaseRepository = get(),
            fileRepository = get(),
            documentRepository = get(),
            eventPublisher = get<EventPublisher>(),
        )
    }

    factory {
        KnowledgeFileService(
            knowledgeBaseRepository = get(),
            fileRepository = get(),
            documentRepository = get(),
            documentParser = get(),
            embeddingService = get(),
            eventPublisher = get<EventPublisher>(),
        )
    }

    factory {
        KnowledgeSearchService(
            knowledgeBaseRepository = get(),
            embeddingService = get(),
            vectorStore = get(),
        )
    }
}
