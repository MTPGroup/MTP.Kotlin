package tech.hanasaki.azusa.modules.notification.application.port.out

/**
 * 短信发送端口接口
 */
interface SmsSenderPort {
    /**
     * 发送短信
     *
     * @param to 收件人手机号
     * @param content 短信内容
     */
    suspend fun send(to: String, content: String)
}