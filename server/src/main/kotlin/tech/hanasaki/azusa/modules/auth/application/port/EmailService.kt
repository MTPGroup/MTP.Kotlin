package tech.hanasaki.azusa.modules.auth.application.port

import tech.hanasaki.azusa.modules.auth.domain.model.Email
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType

interface EmailService {
    suspend fun sendHtml(to: Email, subject: String, html: String)
}

interface NotificationService {
    suspend fun sendOtp(email: Email, code: String, type: OtpType)
}