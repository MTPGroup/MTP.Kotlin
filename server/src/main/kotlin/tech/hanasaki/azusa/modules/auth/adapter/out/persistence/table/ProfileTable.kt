package tech.hanasaki.azusa.modules.auth.adapter.out.persistence.table

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


object ProfileTable : Table("profiles") {
    val uid: Column<Uuid> = uuid("uid").references(UserTable.id, onDelete = ReferenceOption.CASCADE)
    val username = text("username")
    val avatar = text("avatar").nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(uid)
}
