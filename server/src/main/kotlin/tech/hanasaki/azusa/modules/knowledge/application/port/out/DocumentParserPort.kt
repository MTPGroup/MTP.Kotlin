package tech.hanasaki.azusa.modules.knowledge.application.port.out

import kotlinx.serialization.json.JsonObject

/**
 * 文档解析器端口
 */
interface DocumentParser {
    /**
     * 支持的文件类型
     */
    fun supportedTypes(): List<String>

    /**
     * 解析文件并分块
     * @param filePath 文件路径（S3路径或本地路径）
     * @param fileType 文件类型（MIME type）
     * @return 分块列表
     */
    suspend fun parse(filePath: String, fileType: String?): List<DocumentChunk>
}

/**
 * 文档分块
 */
data class DocumentChunk(
    val content: String,
    val metadata: JsonObject,
)
