package tech.hanasaki.azusa.modules.notification.domain.model

/**
 * 通知渠道枚举
 */
enum class NotificationChannel {
    EMAIL,      // 电子邮件
    SMS,        // 短信
    PUSH,       // 推送通知
    WEBSOCKET,  // WebSocket 实时通知
}
