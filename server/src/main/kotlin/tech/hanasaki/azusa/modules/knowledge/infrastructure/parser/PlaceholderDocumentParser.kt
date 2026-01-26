package tech.hanasaki.azusa.modules.knowledge.infrastructure.parser

import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import tech.hanasaki.azusa.modules.knowledge.domain.port.DocumentChunk
import tech.hanasaki.azusa.modules.knowledge.domain.port.DocumentParser

/**
 * 占位文档解析器
 * TODO: 替换为实际的文档解析实现（支持 PDF、Word、Markdown 等格式）
 */
class PlaceholderDocumentParser : DocumentParser {

    companion object {
        private const val CHUNK_SIZE = 1000
        private const val CHUNK_OVERLAP = 200
    }

    override fun supportedTypes(): List<String> {
        return listOf(
            "text/plain",
            "text/markdown",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        )
    }

    override suspend fun parse(filePath: String, fileType: String?): List<DocumentChunk> {
        // 占位实现：返回单个空块
        // 实际实现应：
        // 1. 从 S3 或本地读取文件内容
        // 2. 根据文件类型解析文本
        // 3. 使用分块算法切分文本
        return listOf(
            DocumentChunk(
                content = "Placeholder content from $filePath",
                metadata = buildJsonObject {
                    put("source", filePath)
                    put("fileType", fileType ?: "unknown")
                    put("chunkIndex", 0)
                },
            )
        )
    }

    /**
     * 文本分块（实际实现时使用）
     */
    @Suppress("unused")
    private fun chunkText(text: String, chunkSize: Int = CHUNK_SIZE, overlap: Int = CHUNK_OVERLAP): List<String> {
        if (text.length <= chunkSize) {
            return listOf(text)
        }

        val chunks = mutableListOf<String>()
        var start = 0
        while (start < text.length) {
            val end = minOf(start + chunkSize, text.length)
            chunks.add(text.substring(start, end))
            start += chunkSize - overlap
        }
        return chunks
    }
}
