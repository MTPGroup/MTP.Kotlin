package tech.hanasaki.azusa.auth.internal.application.service

import tech.hanasaki.azusa.auth.internal.domain.model.Email

interface EmailService {
    suspend fun sendHtml(to: Email, subject: String, html: String)
    suspend fun sendTemplate(to: Email, subject: String, templateName: String, model: Map<String, Any>)
}
