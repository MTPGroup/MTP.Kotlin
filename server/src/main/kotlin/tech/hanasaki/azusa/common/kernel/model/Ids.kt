package tech.hanasaki.azusa.common.kernel.model

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@JvmInline
@Serializable
value class UserId(@Contextual val value: Uuid)


@JvmInline
@Serializable
value class ThemeId(@Contextual val value: Uuid)


@JvmInline
@Serializable
value class CharacterId(@Contextual val value: Uuid)


@JvmInline
@Serializable
value class KnowledgeBaseId(@Contextual val value: Uuid)


@JvmInline
@Serializable
value class PluginId(@Contextual val value: Uuid)


@JvmInline
@Serializable
value class KnowledgeFileId(@Contextual val value: Uuid)


@JvmInline
@Serializable
value class KnowledgeDocumentId(@Contextual val value: Uuid)
