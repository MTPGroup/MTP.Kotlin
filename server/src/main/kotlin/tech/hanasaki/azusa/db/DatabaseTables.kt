package tech.hanasaki.azusa.db

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.json.jsonb
import org.jetbrains.exposed.sql.kotlin.datetime.datetime
import java.util.UUID

private val json = Json {
    ignoreUnknownKeys = true
}

object UsersTable : UUIDTable("users") {
    val email: Column<String?> = text("email").nullable()
    val passwordHash: Column<String> = text("password_hash")
    val rawUserMetaData: Column<JsonElement> = jsonb("raw_user_meta_data", json)
    val createdAt: Column<LocalDateTime> = datetime("created_at")
    val updatedAt: Column<LocalDateTime> = datetime("updated_at")
}

object ProfilesTable : UUIDTable("profiles") {
    val uid: Column<EntityID<UUID>> = reference("uid", UsersTable, onDelete = ReferenceOption.CASCADE)
    val username: Column<String> = varchar("username", 50)
    val avatar: Column<String?> = text("avatar").nullable()
    val createdAt: Column<LocalDateTime> = datetime("created_at")
    val updatedAt: Column<LocalDateTime> = datetime("updated_at")
}

object SettingsTable : Table("settings") {
    val ownerId: Column<EntityID<UUID>> = reference("owner_id", ProfilesTable, onDelete = ReferenceOption.CASCADE)
    val theme: Column<String> = varchar("theme", 30)
    val chatModels: Column<JsonElement> = jsonb("chat_models", json)
    val createdAt: Column<LocalDateTime> = datetime("created_at")
    val updatedAt: Column<LocalDateTime> = datetime("updated_at")
    override val primaryKey = PrimaryKey(ownerId)
}

object PluginsTable : UUIDTable("plugins") {
    val name: Column<String> = text("name")
    val description: Column<String> = text("description")
    val version: Column<String> = text("version")
    val liked: Column<Int> = integer("liked")
    val status: Column<String> = varchar("status", 20)
    val schema: Column<JsonElement> = jsonb("schema", json)
    val code: Column<String> = text("code")
    val authorId: Column<EntityID<UUID>> = reference("author_id", ProfilesTable, onDelete = ReferenceOption.CASCADE)
    val createdAt: Column<LocalDateTime> = datetime("created_at")
    val updatedAt: Column<LocalDateTime> = datetime("updated_at")
}

object PluginSubscriptionsTable : Table("plugin_subscriptions") {
    val userId: Column<EntityID<UUID>> = reference("user_id", ProfilesTable, onDelete = ReferenceOption.CASCADE)
    val pluginId: Column<EntityID<UUID>> = reference("plugin_id", PluginsTable, onDelete = ReferenceOption.CASCADE)
    val isActive: Column<Boolean> = bool("is_active")
    val subscribedAt: Column<LocalDateTime> = datetime("subscribed_at")
    override val primaryKey = PrimaryKey(userId, pluginId)
}

object PluginLikesTable : Table("plugin_likes") {
    val userId: Column<EntityID<UUID>> = reference("user_id", ProfilesTable, onDelete = ReferenceOption.CASCADE)
    val pluginId: Column<EntityID<UUID>> = reference("plugin_id", PluginsTable, onDelete = ReferenceOption.CASCADE)
    val createdAt: Column<LocalDateTime> = datetime("created_at")
    override val primaryKey = PrimaryKey(userId, pluginId)
}

object CharactersTable : UUIDTable("characters") {
    val authorId: Column<EntityID<UUID>> = reference("author_id", ProfilesTable, onDelete = ReferenceOption.CASCADE)
    val name: Column<String> = varchar("name", 100)
    val avatar: Column<String?> = text("avatar").nullable()
    val bio: Column<String?> = text("bio").nullable()
    val originPrompt: Column<String?> = text("origin_prompt").nullable()
    val isPublic: Column<Boolean> = bool("is_public")
    val createdAt: Column<LocalDateTime> = datetime("created_at")
    val updatedAt: Column<LocalDateTime> = datetime("updated_at")
}

object ContactsTable : Table("contacts") {
    val profileId: Column<EntityID<UUID>> = reference("profile_id", ProfilesTable, onDelete = ReferenceOption.CASCADE)
    val contactId: Column<EntityID<UUID>> = reference("contact_id", CharactersTable, onDelete = ReferenceOption.CASCADE)
    val nickname: Column<String?> = varchar("nickname", 100).nullable()
    val addedAt: Column<LocalDateTime> = datetime("added_at")
    override val primaryKey = PrimaryKey(profileId, contactId)
}

object KnowledgeBasesTable : UUIDTable("knowledge_bases") {
    val name: Column<String> = varchar("name", 100)
    val description: Column<String?> = text("description").nullable()
    val authorId: Column<EntityID<UUID>> = reference("author_id", ProfilesTable, onDelete = ReferenceOption.CASCADE)
    val isPublic: Column<Boolean> = bool("is_public")
    val createdAt: Column<LocalDateTime> = datetime("created_at")
    val updatedAt: Column<LocalDateTime> = datetime("updated_at")
}

object KnowledgeFilesTable : UUIDTable("knowledge_files") {
    val knowledgeBaseId: Column<EntityID<UUID>> =
        reference("knowledge_base_id", KnowledgeBasesTable, onDelete = ReferenceOption.CASCADE)
    val filePath: Column<String> = text("file_path")
    val fileName: Column<String> = text("file_name")
    val fileSize: Column<Int?> = integer("file_size").nullable()
    val fileType: Column<String?> = text("file_type").nullable()
    val status: Column<String> = varchar("status", 20)
    val errorMessage: Column<String?> = text("error_message").nullable()
    val createdAt: Column<LocalDateTime> = datetime("created_at")
    val updatedAt: Column<LocalDateTime> = datetime("updated_at")
}

object KnowledgeDocumentsTable : UUIDTable("knowledge_documents") {
    val knowledgeBaseId: Column<EntityID<UUID>> =
        reference("knowledge_base_id", KnowledgeBasesTable, onDelete = ReferenceOption.CASCADE)
    val fileId: Column<EntityID<UUID>?> =
        optReference("file_id", KnowledgeFilesTable, onDelete = ReferenceOption.CASCADE)
    val content: Column<String> = text("content")
    val metadata: Column<JsonElement> = jsonb("metadata", json)
    val createdAt: Column<LocalDateTime> = datetime("created_at")
    val updatedAt: Column<LocalDateTime> = datetime("updated_at")
}

object KnowledgeSubscriptionsTable : Table("knowledge_subscriptions") {
    val characterId: Column<EntityID<UUID>> =
        reference("character_id", CharactersTable, onDelete = ReferenceOption.CASCADE)
    val knowledgeBaseId: Column<EntityID<UUID>> =
        reference("knowledge_base_id", KnowledgeBasesTable, onDelete = ReferenceOption.CASCADE)
    val priority: Column<Int> = integer("priority")
    override val primaryKey = PrimaryKey(characterId, knowledgeBaseId)
}

object ChatsTable : UUIDTable("chats") {
    val name: Column<String?> = text("name").nullable()
    val avatar: Column<String?> = text("avatar").nullable()
    val lastMessage: Column<String?> = text("last_message").nullable()
    val isGroup: Column<Boolean> = bool("is_group")
    val ownerId: Column<EntityID<UUID>> = reference("owner_id", ProfilesTable, onDelete = ReferenceOption.CASCADE)
    val createdAt: Column<LocalDateTime> = datetime("created_at")
    val updatedAt: Column<LocalDateTime> = datetime("updated_at")
}

object ChatMembersTable : UUIDTable("chat_members") {
    val chatId: Column<EntityID<UUID>> = reference("chat_id", ChatsTable, onDelete = ReferenceOption.CASCADE)
    val memberType: Column<String> = varchar("member_type", 20)
    val profileId: Column<EntityID<UUID>?> = optReference("profile_id", ProfilesTable, onDelete = ReferenceOption.CASCADE)
    val characterId: Column<EntityID<UUID>?> =
        optReference("character_id", CharactersTable, onDelete = ReferenceOption.CASCADE)
    val role: Column<String> = varchar("role", 20)
    val joinedAt: Column<LocalDateTime> = datetime("joined_at")
    val updatedAt: Column<LocalDateTime> = datetime("updated_at")
}

object MessagesTable : UUIDTable("messages") {
    val chatId: Column<EntityID<UUID>> = reference("chat_id", ChatsTable, onDelete = ReferenceOption.CASCADE)
    val senderType: Column<String> = varchar("sender_type", 20)
    val senderProfileId: Column<EntityID<UUID>?> =
        optReference("sender_profile_id", ProfilesTable, onDelete = ReferenceOption.CASCADE)
    val senderCharacterId: Column<EntityID<UUID>?> =
        optReference("sender_character_id", CharactersTable, onDelete = ReferenceOption.CASCADE)
    val messageType: Column<String> = varchar("message_type", 20)
    val content: Column<JsonElement> = jsonb("content", json)
    val metadata: Column<JsonElement> = jsonb("metadata", json)
    val createdAt: Column<LocalDateTime> = datetime("created_at")
    val updatedAt: Column<LocalDateTime> = datetime("updated_at")
}
