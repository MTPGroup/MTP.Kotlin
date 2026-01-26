package tech.hanasaki.azusa.modules.notification.domain.repository

import tech.hanasaki.azusa.modules.notification.domain.model.NotificationChannel
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationTemplate
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationTemplateId
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationTemplateType

/**
 * 通知模板仓储接口
 */
interface NotificationTemplateRepository {
    /**
     * 保存模板
     */
    suspend fun save(template: NotificationTemplate)

    /**
     * 根据 ID 查找模板
     */
    suspend fun findById(id: NotificationTemplateId): NotificationTemplate?

    /**
     * 根据类型和渠道查找激活的模板
     */
    suspend fun findActiveByTypeAndChannel(
        type: NotificationTemplateType,
        channel: NotificationChannel,
    ): NotificationTemplate?

    /**
     * 获取所有模板
     */
    suspend fun findAll(): List<NotificationTemplate>

    /**
     * 删除模板
     */
    suspend fun deleteById(id: NotificationTemplateId)
}
