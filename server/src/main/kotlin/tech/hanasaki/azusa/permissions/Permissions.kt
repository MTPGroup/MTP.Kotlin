package tech.hanasaki.azusa.permissions

import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import tech.hanasaki.azusa.db.*
import java.util.*

object Permissions {
    private fun profileId(id: UUID): EntityID<UUID> = EntityID(id, ProfilesTable)
    private fun characterId(id: UUID): EntityID<UUID> = EntityID(id, CharactersTable)
    private fun knowledgeBaseId(id: UUID): EntityID<UUID> = EntityID(id, KnowledgeBasesTable)
    private fun pluginId(id: UUID): EntityID<UUID> = EntityID(id, PluginsTable)
    private fun chatId(id: UUID): EntityID<UUID> = EntityID(id, ChatsTable)

    suspend fun canViewCharacter(profileId: UUID?, characterId: UUID): Boolean = dbQuery {
        CharactersTable
            .selectAll()
            .where {
                (CharactersTable.id eq characterId(characterId)) and
                        (CharactersTable.isPublic eq true or
                                (profileId?.let { CharactersTable.authorId eq profileId(it) } ?: Op.FALSE))
            }
            .limit(1)
            .any()
    }

    suspend fun isCharacterOwner(profileId: UUID, characterId: UUID): Boolean = dbQuery {
        CharactersTable
            .selectAll()
            .where {
                (CharactersTable.id eq characterId(characterId)) and (CharactersTable.authorId eq profileId(
                    profileId
                ))
            }
            .limit(1)
            .any()
    }

    suspend fun canViewKnowledgeBase(profileId: UUID?, kbId: UUID): Boolean = dbQuery {
        KnowledgeBasesTable
            .selectAll()
            .where {
                (KnowledgeBasesTable.id eq knowledgeBaseId(kbId)) and
                        (KnowledgeBasesTable.isPublic eq true or
                                (profileId?.let { KnowledgeBasesTable.authorId eq profileId(it) } ?: Op.FALSE))
            }
            .limit(1)
            .any()
    }

    suspend fun isKnowledgeBaseOwner(profileId: UUID, kbId: UUID): Boolean = dbQuery {
        KnowledgeBasesTable
            .selectAll()
            .where {
                (KnowledgeBasesTable.id eq knowledgeBaseId(kbId)) and
                        (KnowledgeBasesTable.authorId eq profileId(profileId))
            }
            .limit(1)
            .any()
    }

    suspend fun canViewPlugin(profileId: UUID?, pluginId: UUID): Boolean = dbQuery {
        PluginsTable
            .selectAll()
            .where {
                (PluginsTable.id eq pluginId(pluginId)) and
                        (PluginsTable.status eq "approved" or
                                (profileId?.let { PluginsTable.authorId eq profileId(it) } ?: Op.FALSE))
            }
            .limit(1)
            .any()
    }

    suspend fun isPluginOwner(profileId: UUID, pluginId: UUID): Boolean = dbQuery {
        PluginsTable
            .selectAll()
            .where { (PluginsTable.id eq pluginId(pluginId)) and (PluginsTable.authorId eq profileId(profileId)) }
            .limit(1)
            .any()
    }

    suspend fun isContactOwner(profileId: UUID, contactId: UUID): Boolean = dbQuery {
        ContactsTable
            .selectAll()
            .where {
                (ContactsTable.profileId eq profileId(profileId)) and
                        (ContactsTable.contactId eq characterId(contactId))
            }
            .limit(1)
            .any()
    }

    suspend fun isChatOwner(profileId: UUID, chatId: UUID): Boolean = dbQuery {
        ChatsTable
            .selectAll()
            .where { (ChatsTable.id eq chatId(chatId)) and (ChatsTable.ownerId eq profileId(profileId)) }
            .limit(1)
            .any()
    }

    suspend fun isChatMemberOrCharacterOwner(profileId: UUID, chatId: UUID): Boolean = dbQuery {
        val userMembership = ChatMembersTable
            .selectAll()
            .where {
                (ChatMembersTable.chatId eq chatId(chatId)) and
                        (ChatMembersTable.memberType eq "user") and
                        (ChatMembersTable.profileId eq profileId(profileId))
            }
            .limit(1)
            .any()

        if (userMembership) return@dbQuery true

        ChatMembersTable
            .join(CharactersTable, JoinType.INNER, ChatMembersTable.characterId, CharactersTable.id)
            .selectAll()
            .where {
                (ChatMembersTable.chatId eq chatId(chatId)) and
                        (ChatMembersTable.memberType eq "character") and
                        (CharactersTable.authorId eq profileId(profileId))
            }
            .limit(1)
            .any()
    }
}
