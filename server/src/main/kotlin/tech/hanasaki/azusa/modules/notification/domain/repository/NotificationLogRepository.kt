package tech.hanasaki.azusa.modules.notification.domain.repository

import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationChannel
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationLog
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationLogId
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationStatus

/**
 * 通知日志仓储接口
 */
interface NotificationLogRepository {
    /**
     * 保存通知日志
     */
    suspend fun save(log: NotificationLog)

    /**
     * 根据 ID 查找通知日志
     */
    suspend fun findById(id: NotificationLogId): NotificationLog?

    /**
     * 根据用户 ID 查找通知日志（分页）
     */
    suspend fun findByUserIdPaged(
        userId: UserId,
        page: Int,
        limit: Int,
    ): PageResult<NotificationLog>

    /**
     * 根据状态查找通知日志
     */
    suspend fun findByStatus(status: NotificationStatus): List<NotificationLog>

    /**
     * 根据渠道和状态查找通知日志
     */
    suspend fun findByChannelAndStatus(
        channel: NotificationChannel,
        status: NotificationStatus,
    ): List<NotificationLog>
}
