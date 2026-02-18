package tech.hanasaki.azusa.modules.notification.adapter.out.persistence.repository

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import tech.hanasaki.azusa.modules.notification.adapter.out.persistence.mapper.NotificationLogMapper
import tech.hanasaki.azusa.modules.notification.adapter.out.persistence.table.NotificationLogTable
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationChannel
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationLog
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationLogId
import tech.hanasaki.azusa.modules.notification.domain.model.NotificationStatus
import tech.hanasaki.azusa.modules.notification.domain.port.NotificationLogRepositoryPort
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

/**
 * 通知日志仓储实现
 */
class ExposedNotificationLogRepository : NotificationLogRepositoryPort {

    override suspend fun save(log: NotificationLog) {
        val exists = NotificationLogTable.selectAll()
            .where { NotificationLogTable.id eq log.id.value }
            .count() > 0

        if (exists) {
            NotificationLogTable.update({ NotificationLogTable.id eq log.id.value }) {
                NotificationLogMapper.toEntity(log, it)
            }
        } else {
            NotificationLogTable.insert {
                NotificationLogMapper.toEntity(log, it)
            }
        }
    }

    override suspend fun findById(id: NotificationLogId): NotificationLog? =
        NotificationLogTable.selectAll()
            .where { NotificationLogTable.id eq id.value }
            .map { NotificationLogMapper.toDomain(it) }
            .singleOrNull()

    override suspend fun findByUserIdPaged(
        userId: UserId,
        page: Int,
        limit: Int,
    ): PageResult<NotificationLog> {
        val total = NotificationLogTable.selectAll()
            .where { NotificationLogTable.userId eq userId.value }
            .count()

        val items = NotificationLogTable.selectAll()
            .where { NotificationLogTable.userId eq userId.value }
            .orderBy(NotificationLogTable.createdAt, SortOrder.DESC)
            .limit(limit)
            .offset(((page - 1) * limit).toLong())
            .map { NotificationLogMapper.toDomain(it) }

        return PageResult(
            items = items,
            total = total,
            page = page,
            limit = limit,
        )
    }

    override suspend fun findByStatus(status: NotificationStatus): List<NotificationLog> =
        NotificationLogTable.selectAll()
            .where { NotificationLogTable.status eq status.name }
            .map { NotificationLogMapper.toDomain(it) }

    override suspend fun findByChannelAndStatus(
        channel: NotificationChannel,
        status: NotificationStatus,
    ): List<NotificationLog> = NotificationLogTable.selectAll()
        .where {
            (NotificationLogTable.channel eq channel.name) and
                    (NotificationLogTable.status eq status.name)
        }
        .map { NotificationLogMapper.toDomain(it) }
}
