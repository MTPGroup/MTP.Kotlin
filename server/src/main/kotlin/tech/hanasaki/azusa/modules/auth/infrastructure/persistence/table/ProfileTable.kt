package tech.hanasaki.azusa.modules.auth.infrastructure.persistence.table

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.*

object ProfileTable : IdTable<UUID>("profiles") {
    override val id: Column<EntityID<UUID>> = reference("uid", UserTable.id, ReferenceOption.CASCADE)
    val username = text("username")
    val avatar = text("avatar").nullable()
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
}