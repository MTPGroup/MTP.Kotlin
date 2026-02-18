package tech.hanasaki.azusa.modules.chat.adapter.out.llm

import dev.langchain4j.data.audio.Audio
import dev.langchain4j.data.message.Content
import dev.langchain4j.data.message.AudioContent
import dev.langchain4j.data.message.ImageContent
import dev.langchain4j.data.message.PdfFileContent
import dev.langchain4j.data.message.TextContent
import dev.langchain4j.data.message.VideoContent
import dev.langchain4j.data.pdf.PdfFile
import dev.langchain4j.data.video.Video
import tech.hanasaki.azusa.modules.chat.domain.model.MessageContent
import java.net.URI

object MessageContentConverter {

    fun toContents(contents: List<MessageContent>): List<Content> {
        return contents.map { toContent(it) }
    }

    fun toContent(content: MessageContent): Content {
        return when (content) {
            is MessageContent.Text -> TextContent.from(content.content)
            is MessageContent.Image -> ImageContent.from(content.url)
            is MessageContent.Audio -> AudioContent.from(Audio.builder().url(URI.create(content.url)).build())
            is MessageContent.Video -> VideoContent.from(Video.builder().url(URI.create(content.url)).build())
            is MessageContent.Pdf -> PdfFileContent.from(PdfFile.builder().url(URI.create(content.url)).build())
            is MessageContent.Code -> TextContent.from("```${content.language ?: ""}\n${content.code}\n```")
            is MessageContent.File -> TextContent.from("[文件: ${content.fileName}]")
        }
    }
}
