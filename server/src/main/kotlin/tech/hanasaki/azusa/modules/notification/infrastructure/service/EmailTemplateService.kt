package tech.hanasaki.azusa.modules.notification.infrastructure.service

import freemarker.template.Configuration
import java.io.StringWriter

class EmailTemplateService(
    private val freemarkerConfig: Configuration,
) {
    fun renderTemplate(templateName: String, model: Map<String, Any>): String {
        val template = freemarkerConfig.getTemplate(templateName)
        val writer = StringWriter()
        template.process(model, writer)
        return writer.toString()
    }
}
