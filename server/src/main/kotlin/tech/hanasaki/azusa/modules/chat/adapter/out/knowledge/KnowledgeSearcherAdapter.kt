package tech.hanasaki.azusa.modules.chat.adapter.out.knowledge

import tech.hanasaki.azusa.modules.chat.application.port.out.KnowledgeSearcherPort
import tech.hanasaki.azusa.modules.chat.application.port.out.SearchResult
import tech.hanasaki.azusa.modules.knowledge.application.port.`in`.KnowledgeSearchUseCasePort
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import kotlin.uuid.Uuid

class KnowledgeSearcherAdapter(
    private val knowledgeSearchService: KnowledgeSearchUseCasePort,
) : KnowledgeSearcherPort {

    override suspend fun search(
        userId: UserId,
        knowledgeBaseIds: List<String>,
        query: String,
        limit: Int,
    ): List<SearchResult> {
        val kbIds = knowledgeBaseIds.map { KnowledgeBaseId(Uuid.parse(it)) }
        val results = knowledgeSearchService.search(userId, kbIds, query, limit = limit)
        return results.map { r ->
            SearchResult(
                content = r.content,
                similarity = r.similarity,
                source = r.documentId.value.toString(),
            )
        }
    }
}
