package tech.hanasaki.azusa.modules.plugin.infrastructure.persistence.repository

import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import tech.hanasaki.azusa.modules.plugin.domain.repository.PluginLikeRepository
import tech.hanasaki.azusa.modules.plugin.infrastructure.persistence.table.PluginLikeTable
import tech.hanasaki.azusa.common.kernel.model.PluginId
import tech.hanasaki.azusa.common.kernel.model.UserId
import tech.hanasaki.azusa.common.platform.database.dbQuery

class ExposedPluginLikeRepository : PluginLikeRepository {

    override suspend fun exists(userId: UserId, pluginId: PluginId): Boolean = dbQuery {
        PluginLikeTable.selectAll()
            .where {
                (PluginLikeTable.userId eq userId.value) and
                        (PluginLikeTable.pluginId eq pluginId.value)
            }
            .count() > 0
    }

    override suspend fun like(userId: UserId, pluginId: PluginId): Unit = dbQuery {
        val exists = exists(userId, pluginId)

        if (!exists) {
            PluginLikeTable.insert {
                it[PluginLikeTable.userId] = userId.value
                it[PluginLikeTable.pluginId] = pluginId.value
            }
        }
    }

    override suspend fun unlike(userId: UserId, pluginId: PluginId): Unit = dbQuery {
        PluginLikeTable.deleteWhere {
            (PluginLikeTable.userId eq userId.value) and
                    (PluginLikeTable.pluginId eq pluginId.value)
        }
    }
}
