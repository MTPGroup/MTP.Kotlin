package tech.hanasaki.azusa.modules.notification.infrastructure.persistence.repository

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationChannel
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationTemplate
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationTemplateId
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationTemplateType
import tech.hanasaki.azusa.modules.notification.domain.repository.NotificationTemplateRepository
import tech.hanasaki.azusa.modules.notification.infrastructure.persistence.mapper.NotificationTemplateMapper
import tech.hanasaki.azusa.modules.notification.infrastructure.persistence.table.NotificationTemplateTable
import tech.hanasaki.azusa.common.platform.database.dbQuery

/**
 * 通知模板仓储实现
 */
class ExposedNotificationTemplateRepository : NotificationTemplateRepository {

    override suspend fun save(template: NotificationTemplate): Unit = dbQuery {
        val exists = NotificationTemplateTable.selectAll()
            .where { NotificationTemplateTable.id eq template.id.value }
            .count() > 0

        if (exists) {
            NotificationTemplateTable.update({ NotificationTemplateTable.id eq template.id.value }) {
                NotificationTemplateMapper.toEntity(template, it)
            }
        } else {
            NotificationTemplateTable.insert {
                NotificationTemplateMapper.toEntity(template, it)
            }
        }
    }

    override suspend fun findById(id: NotificationTemplateId): NotificationTemplate? = dbQuery {
        NotificationTemplateTable.selectAll()
            .where { NotificationTemplateTable.id eq id.value }
            .map { NotificationTemplateMapper.toDomain(it) }
            .singleOrNull()
    }

    override suspend fun findActiveByTypeAndChannel(
        type: NotificationTemplateType,
        channel: NotificationChannel,
    ): NotificationTemplate? = dbQuery {
        NotificationTemplateTable.selectAll()
            .where {
                (NotificationTemplateTable.type eq type.name) and
                        (NotificationTemplateTable.channel eq channel.name) and
                        (NotificationTemplateTable.isActive eq true)
            }
            .map { NotificationTemplateMapper.toDomain(it) }
            .singleOrNull()
    }

    override suspend fun findAll(): List<NotificationTemplate> = dbQuery {
        NotificationTemplateTable.selectAll()
            .orderBy(NotificationTemplateTable.createdAt, SortOrder.DESC)
            .map { NotificationTemplateMapper.toDomain(it) }
    }

    override suspend fun deleteById(id: NotificationTemplateId): Unit = dbQuery {
        NotificationTemplateTable.deleteWhere { NotificationTemplateTable.id eq id.value }
    }
}
