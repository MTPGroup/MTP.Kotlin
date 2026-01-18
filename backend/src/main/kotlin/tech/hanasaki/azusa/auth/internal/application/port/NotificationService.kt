package tech.hanasaki.azusa.auth.internal.application.port

import tech.hanasaki.azusa.auth.internal.domain.model.Email
import tech.hanasaki.azusa.auth.internal.domain.model.OtpType

interface NotificationService {
    fun sendOtp(email: Email, code: String, type: OtpType)
}