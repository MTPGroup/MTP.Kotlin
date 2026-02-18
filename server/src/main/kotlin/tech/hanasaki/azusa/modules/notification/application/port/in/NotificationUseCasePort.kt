package tech.hanasaki.azusa.modules.notification.application.port.`in`

import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface NotificationUseCasePort {
    suspend fun sendEmail(
        to: String,
        subject: String,
        content: String? = null,
        templateName: String? = null,
        model: Map<String, Any> = emptyMap(),
        userId: UserId? = null,
    )

    suspend fun sendSms(
        to: String,
        content: String,
        userId: UserId? = null,
    )

    suspend fun sendPush(
        deviceToken: String,
        title: String,
        body: String,
        userId: UserId? = null,
        data: Map<String, String>? = null,
    )
}