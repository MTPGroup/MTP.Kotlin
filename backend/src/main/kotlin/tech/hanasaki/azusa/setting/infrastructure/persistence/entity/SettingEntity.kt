package tech.hanasaki.azusa.setting.infrastructure.persistence.entity

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.MappedCollection
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant
import java.util.*

@Table("settings")
data class SettingEntity(
    @Id
    @get:JvmName("getSettingId")
    val uid: UUID,

    val theme: String,

    @MappedCollection(idColumn = "setting_id")
    val llmConfigs: Set<LLMConfigEntity>,

    @Column("active_theme_id")
    val activeThemeId: UUID?,
    @Column("active_llm_config_id")
    val activeLlmConfigId: UUID,
    @Column("created_at")
    val createdAt: Instant,
    @Column("updated_at")
    val updatedAt: Instant,
) : Persistable<UUID> {
    @Transient
    var isNewRecord: Boolean = false
    override fun getId(): UUID = uid
    override fun isNew(): Boolean = isNewRecord
}

@Table("llm_configs")
data class LLMConfigEntity(
    @Id
    @get:JvmName("getLLMConfigId")
    val id: UUID,
    val provider: String,
    @Column("base_url")
    val baseUrl: String,
    @Column("api_key")
    val apiKey: String,
    val model: String,
    val temperature: Float,
    @Column("max_tokens")
    val maxTokens: Int?,
    @Column("run_on_client")
    val runOnClient: Boolean,
) : Persistable<UUID> {
    @Transient
    var isNewRecord: Boolean = false

    override fun getId(): UUID = id
    override fun isNew(): Boolean = isNewRecord
}
