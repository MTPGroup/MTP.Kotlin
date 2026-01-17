package tech.hanasaki.azusa.auth.application.service

import tech.hanasaki.azusa.auth.domain.model.Email

interface EmailService {
    suspend fun sendHtml(to: Email, subject: String, html: String)
}
