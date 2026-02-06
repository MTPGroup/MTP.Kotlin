package tech.hanasaki.azusa.shared.domain.model.vo

import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid


@JvmInline
@Serializable
value class UserId(val value: Uuid) {
    companion object {
        fun generate(): UserId = UserId(Uuid.random())
        fun fromString(value: String): UserId = UserId(Uuid.parse(value))
    }
}


@JvmInline
@Serializable
value class ThemeId(val value: Uuid)


@JvmInline
@Serializable
value class CharacterId(val value: Uuid)


@JvmInline
@Serializable
value class KnowledgeBaseId(val value: Uuid)


@JvmInline
@Serializable
value class PluginId(val value: Uuid)


@JvmInline
@Serializable
value class KnowledgeFileId(val value: Uuid)


@JvmInline
@Serializable
value class KnowledgeDocumentId(val value: Uuid)
