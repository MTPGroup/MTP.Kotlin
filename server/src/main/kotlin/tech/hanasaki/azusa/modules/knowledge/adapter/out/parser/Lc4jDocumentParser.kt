package tech.hanasaki.azusa.modules.knowledge.adapter.out.parser

import dev.langchain4j.data.document.BlankDocumentException
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.DocumentParser
import dev.langchain4j.data.document.parser.TextDocumentParser
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser
import dev.langchain4j.data.document.parser.markdown.MarkdownDocumentParser
import dev.langchain4j.data.document.parser.yaml.YamlDocumentParser
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import tech.hanasaki.azusa.modules.knowledge.application.port.out.DocumentChunk
import tech.hanasaki.azusa.modules.knowledge.application.port.out.DocumentParserPort
import tech.hanasaki.azusa.shared.port.out.FileStoragePort
import java.io.ByteArrayInputStream


/**
 * 基于 LangChain4j 的文档解析器
 * 支持格式：Text, PDF, Office (Word/Excel/PowerPoint), Markdown, YAML, 以及通过 Apache Tika 的通用解析
 */
class Lc4jDocumentParser(
    private val fileStorage: FileStoragePort,
) : DocumentParserPort {
    private val logger = KotlinLogging.logger { }

    companion object {
        // 文件大小限制 (200MB)
        private const val MAX_FILE_SIZE = 200 * 1024 * 1024L
    }

    override fun supportedTypes(): List<String> = listOf(
        // 文本文件
        "text/plain",
        "text/markdown",
        "text/x-markdown",
        "text/yaml",
        "text/x-yaml",
        "application/yaml",
        "application/x-yaml",
        // PDF
        "application/pdf",
        // Microsoft Office
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        // OpenDocument
        "application/vnd.oasis.opendocument.text",
        "application/vnd.oasis.opendocument.spreadsheet",
        "application/vnd.oasis.opendocument.presentation",
        // 其他（通过 Tika 解析）
        "application/rtf",
        "text/rtf",
        "text/html",
        "application/xhtml+xml",
        "application/xml",
        "text/xml",
    )

    override suspend fun parse(filePath: String, fileType: String?): List<DocumentChunk> {
        logger.info { "使用 LangChain4j 解析文件: $filePath (文件类型: $fileType)" }

        val bytes = fileStorage.download(filePath)

        if (bytes.isEmpty()) {
            logger.warn { "文件内容为空: $filePath" }
            return emptyList()
        }

        if (bytes.size > MAX_FILE_SIZE) {
            throw IllegalArgumentException("文件大小超过限制 (${MAX_FILE_SIZE / 1024 / 1024}MB): $filePath")
        }

        val document = parseDocument(bytes, fileType, filePath)

        if (document.text().isBlank()) {
            logger.warn { "无法从文件中解析出文本: $filePath" }
            return emptyList()
        }

        logger.debug { "成功解析文件: $filePath, 文本长度: ${document.text().length}" }

        return listOf(
            DocumentChunk(
                content = document.text(),
                metadata = buildJsonObject {
                    put("source", filePath)
                    put("fileType", fileType ?: "unknown")
                    put("parser", "langchain4j")
                },
            )
        )
    }

    /**
     * 根据文件类型选择合适的解析器
     */
    private fun parseDocument(bytes: ByteArray, fileType: String?, filePath: String): Document {
        val parser = selectParser(fileType, filePath)

        return try {
            ByteArrayInputStream(bytes).use { input ->
                parser.parse(input)
            }
        } catch (e: BlankDocumentException) {
            logger.warn(e) { "文档内容为空: $filePath" }
            Document.from("")
        } catch (e: Exception) {
            logger.error(e) { "解析文档失败: $filePath, 尝试使用 Tika 作为备选" }
            // 备选：使用 Tika 尝试解析
            tryTikaParse(bytes)
        }
    }

    /**
     * 根据 MIME 类型和文件扩展名选择合适的解析器
     */
    private fun selectParser(fileType: String?, filePath: String): DocumentParser {
        return when (fileType?.lowercase()) {
            // 文本文件
            "text/plain" -> TextDocumentParser()

            // Markdown
            "text/markdown", "text/x-markdown" -> MarkdownDocumentParser()

            // YAML/JSON
            "text/yaml", "text/x-yaml", "application/yaml", "application/x-yaml",
                -> YamlDocumentParser()

            // PDF
            "application/pdf" -> ApacheTikaDocumentParser()

            // Office 文档 - 使用 POI
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-powerpoint",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                -> ApacheTikaDocumentParser()

            // 其他格式尝试使用 Tika
            else -> {
                // 根据文件扩展名再次判断
                when (getFileExtension(filePath).lowercase()) {
                    "pdf" -> ApacheTikaDocumentParser()
                    "doc", "docx", "xls", "xlsx", "ppt", "pptx" -> ApacheTikaDocumentParser()
                    "txt", "md", "markdown", "yaml", "yml", "json", "xml", "html", "htm" -> TextDocumentParser()
                    else -> ApacheTikaDocumentParser()
                }
            }
        }
    }

    /**
     * 尝试使用 Tika 解析
     */
    private fun tryTikaParse(bytes: ByteArray): Document {
        return try {
            ByteArrayInputStream(bytes).use { input ->
                ApacheTikaDocumentParser().parse(input)
            }
        } catch (e: Exception) {
            logger.error(e) { "Tika 解析也失败了" }
            Document.from("")
        }
    }

    /**
     * 获取文件扩展名
     */
    private fun getFileExtension(filePath: String): String {
        val lastDot = filePath.lastIndexOf('.')
        return if (lastDot > 0 && lastDot < filePath.length - 1) {
            filePath.substring(lastDot + 1)
        } else {
            ""
        }
    }
}
