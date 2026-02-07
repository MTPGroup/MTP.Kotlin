package tech.hanasaki.azusa.modules.plugin.adapter.out.persistence.table

import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.json.jsonb
import tech.hanasaki.azusa.modules.plugin.domain.model.PluginSchema
import tech.hanasaki.azusa.modules.plugin.domain.model.PluginStatus
import kotlin.time.Clock


object PluginTable : Table("plugins") {
    val id = uuid("id")
    val name = text("name")
    val description = text("description")
    val version = text("version")
    val schema = jsonb<PluginSchema>("schema", Json { prettyPrint = true })
    val code = text("code")
    val authorId = uuid("author_id")
    val status = enumerationByName<PluginStatus>("status", 20)
    val liked = integer("liked").default(0)
    val createdAt = timestamp("created_at").default(Clock.System.now())
    val updatedAt = timestamp("updated_at").default(Clock.System.now())

    override val primaryKey = PrimaryKey(id)
}
