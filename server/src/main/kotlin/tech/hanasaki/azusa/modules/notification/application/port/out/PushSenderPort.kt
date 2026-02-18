package tech.hanasaki.azusa.modules.notification.application.port.out

/**
 * 推送通知发送端口接口
 */
interface PushSenderPort {
    /**
     * 发送推送通知
     *
     * @param deviceToken 设备令牌
     * @param title 通知标题
     * @param body 通知内容
     * @param data 附加数据（可选）
     */
    suspend fun send(
        deviceToken: String,
        title: String,
        body: String,
        data: Map<String, String>? = null,
    )

    /**
     * 批量发送推送通知
     *
     * @param deviceTokens 设备令牌列表
     * @param title 通知标题
     * @param body 通知内容
     * @param data 附加数据（可选）
     */
    suspend fun sendBatch(
        deviceTokens: List<String>,
        title: String,
        body: String,
        data: Map<String, String>? = null,
    )
}