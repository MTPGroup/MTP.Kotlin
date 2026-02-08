package tech.hanasaki.azusa.modules.knowledge.adapter.out.parser

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.hwpf.extractor.WordExtractor
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.XWPFDocument
import tech.hanasaki.azusa.modules.knowledge.application.port.out.DocumentChunk
import tech.hanasaki.azusa.modules.knowledge.application.port.out.DocumentParser
import tech.hanasaki.azusa.shared.port.out.FileStoragePort
import java.io.ByteArrayInputStream

private val logger = KotlinLogging.logger {}

class S3DocumentParser(
    private val fileStorage: FileStoragePort,
) : DocumentParser {

    companion object {
        private const val CHUNK_SIZE = 1000
        private const val CHUNK_OVERLAP = 200
    }

    override fun supportedTypes(): List<String> = listOf(
        "text/plain",
        "text/markdown",
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    )

    override suspend fun parse(filePath: String, fileType: String?): List<DocumentChunk> {
        logger.info { "解析文件: $filePath (文件类型: $fileType)" }

        val bytes = fileStorage.download(filePath)
        val text = extractText(bytes, fileType)

        if (text.isBlank()) {
            logger.warn { "无法从文件中解析出文本: $filePath" }
            return emptyList()
        }

        return chunkText(text).mapIndexed { index, chunk ->
            DocumentChunk(
                content = chunk,
                metadata = buildJsonObject {
                    put("source", filePath)
                    put("fileType", fileType ?: "unknown")
                    put("chunkIndex", index)
                },
            )
        }
    }

    private fun extractText(bytes: ByteArray, fileType: String?): String = when (fileType) {
        "text/plain", "text/markdown" -> String(bytes, Charsets.UTF_8)

        "application/pdf" -> Loader.loadPDF(bytes).use { doc ->
            PDFTextStripper().getText(doc)
        }

        "application/msword" -> ByteArrayInputStream(bytes).use { input ->
            WordExtractor(input).use { extractor ->
                extractor.text
            }
        }

        "application/vnd.openxmlformats-officedocument.wordprocessingml.document" ->
            ByteArrayInputStream(bytes).use { input ->
                XWPFDocument(input).use { doc ->
                    XWPFWordExtractor(doc).use { extractor ->
                        extractor.text
                    }
                }
            }

        else -> {
            logger.warn { "不支持的文件类型: $fileType, 正在尝试从纯文本解析" }
            String(bytes, Charsets.UTF_8)
        }
    }

    private fun chunkText(text: String): List<String> {
        if (text.length <= CHUNK_SIZE) {
            return listOf(text)
        }

        val chunks = mutableListOf<String>()
        var start = 0
        while (start < text.length) {
            val end = minOf(start + CHUNK_SIZE, text.length)
            chunks.add(text.substring(start, end))
            start += CHUNK_SIZE - CHUNK_OVERLAP
        }
        return chunks
    }
}
