package tech.hanasaki.azusa.modules.auth.application.service

import tech.hanasaki.azusa.modules.auth.domain.model.Email

interface EmailService {
    suspend fun sendHtml(to: Email, subject: String, html: String)
}
