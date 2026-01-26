package tech.hanasaki.azusa.modules.plugin.infrastructure.persistence.repository

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import org.jetbrains.exposed.v1.jdbc.upsert
import tech.hanasaki.azusa.modules.plugin.domain.model.PluginSubscription
import tech.hanasaki.azusa.modules.plugin.domain.repository.PluginSubscriptionRepository
import tech.hanasaki.azusa.modules.plugin.infrastructure.persistence.table.PluginSubscriptionTable
import tech.hanasaki.azusa.shared.domain.model.PluginId
import tech.hanasaki.azusa.shared.domain.model.UserId
import tech.hanasaki.azusa.shared.infrastructure.database.dbQuery

class ExposedPluginSubscriptionRepository : PluginSubscriptionRepository {
    override suspend fun findByUserId(userId: UserId): List<PluginSubscription> = dbQuery {
        PluginSubscriptionTable.selectAll()
            .where { PluginSubscriptionTable.userId eq userId.value }
            .map { row ->
                PluginSubscription(
                    userId = UserId(row[PluginSubscriptionTable.userId]),
                    pluginId = PluginId(row[PluginSubscriptionTable.pluginId]),
                    isActive = row[PluginSubscriptionTable.isActive],
                    subscribedAt = row[PluginSubscriptionTable.subscribedAt].toInstant(TimeZone.UTC),
                )
            }
    }

    override suspend fun findActiveByUserId(userId: UserId): List<PluginSubscription> = dbQuery {
        PluginSubscriptionTable.selectAll()
            .where {
                (PluginSubscriptionTable.userId eq userId.value) and
                        (PluginSubscriptionTable.isActive eq true)
            }
            .map { row ->
                PluginSubscription(
                    userId = UserId(row[PluginSubscriptionTable.userId]),
                    pluginId = PluginId(row[PluginSubscriptionTable.pluginId]),
                    isActive = row[PluginSubscriptionTable.isActive],
                    subscribedAt = row[PluginSubscriptionTable.subscribedAt].toInstant(TimeZone.UTC),
                )
            }
    }

    override suspend fun findByUserIdAndPluginId(userId: UserId, pluginId: PluginId): PluginSubscription? = dbQuery {
        PluginSubscriptionTable.selectAll()
            .where {
                (PluginSubscriptionTable.userId eq userId.value) and
                        (PluginSubscriptionTable.pluginId eq pluginId.value)
            }
            .map { row ->
                PluginSubscription(
                    userId = UserId(row[PluginSubscriptionTable.userId]),
                    pluginId = PluginId(row[PluginSubscriptionTable.pluginId]),
                    isActive = row[PluginSubscriptionTable.isActive],
                    subscribedAt = row[PluginSubscriptionTable.subscribedAt].toInstant(TimeZone.UTC),
                )
            }
            .singleOrNull()
    }

    override suspend fun subscribe(userId: UserId, pluginId: PluginId, isActive: Boolean): Unit = dbQuery {
        PluginSubscriptionTable.upsert {
            it[PluginSubscriptionTable.userId] = userId.value
            it[PluginSubscriptionTable.pluginId] = pluginId.value
            it[PluginSubscriptionTable.isActive] = isActive
        }
    }

    override suspend fun unsubscribe(userId: UserId, pluginId: PluginId): Unit = dbQuery {
        PluginSubscriptionTable.deleteWhere {
            (PluginSubscriptionTable.userId eq userId.value) and
                    (PluginSubscriptionTable.pluginId eq pluginId.value)
        }
    }

    override suspend fun updateActiveStatus(userId: UserId, pluginId: PluginId, isActive: Boolean): Unit = dbQuery {
        PluginSubscriptionTable.update({
            (PluginSubscriptionTable.userId eq userId.value) and
                    (PluginSubscriptionTable.pluginId eq pluginId.value)
        }) {
            it[PluginSubscriptionTable.isActive] = isActive
        }
    }
}
