package tech.hanasaki.azusa.modules.notification.domain.port

/**
 * 邮件发送端口接口
 */
interface EmailSender {
    /**
     * 发送 HTML 邮件
     *
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param html HTML 内容
     */
    suspend fun sendHtml(to: String, subject: String, html: String)

    /**
     * 发送纯文本邮件
     *
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param text 纯文本内容
     */
    suspend fun sendText(to: String, subject: String, text: String)
}
