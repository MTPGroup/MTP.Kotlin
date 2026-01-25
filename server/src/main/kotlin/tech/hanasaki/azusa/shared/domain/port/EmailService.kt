package tech.hanasaki.azusa.shared.domain.port

import tech.hanasaki.azusa.modules.auth.domain.model.Email
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType

/**
 * 邮件服务端口 - 领域层定义的邮件发送接口
 */
interface EmailService {
    suspend fun sendHtml(to: Email, subject: String, html: String)
}

/**
 * 通知服务端口 - 领域层定义的通知接口
 */
interface NotificationService {
    suspend fun sendOtp(email: Email, code: String, type: OtpType)
}
