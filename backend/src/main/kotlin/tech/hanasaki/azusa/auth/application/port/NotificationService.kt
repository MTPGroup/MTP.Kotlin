package tech.hanasaki.azusa.auth.application.port

import tech.hanasaki.azusa.auth.domain.model.Email
import tech.hanasaki.azusa.auth.domain.model.OtpType

interface NotificationService {
    fun sendOtp(email: Email, code: String, type: OtpType)
}