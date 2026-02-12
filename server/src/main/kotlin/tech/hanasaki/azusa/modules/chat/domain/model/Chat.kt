package tech.hanasaki.azusa.modules.chat.domain.model

import kotlinx.serialization.Serializable
import tech.hanasaki.azusa.modules.chat.domain.events.ChatCreated
import tech.hanasaki.azusa.shared.domain.model.base.AggregateRoot
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

@JvmInline
@Serializable
value class ChatId(val value: Uuid)

/**
 * Chat 聚合根 （支持单聊和群聊）
 */
class Chat private constructor(
    val id: ChatId,
    val ownerId: UserId,
    var name: String?,
    var lastMessage: String?,
    val createdAt: Instant,
    var updatedAt: Instant,
    var members: List<ChatMember>,
) : AggregateRoot() {

    companion object {
        /**
         * 创建私聊
         */
        fun createPrivateChat(
            ownerId: UserId,
            characterId: CharacterId,
            name: String? = null,
        ): Chat {
            val now = Clock.System.now()
            val chat = Chat(
                id = ChatId(Uuid.random()),
                ownerId = ownerId,
                name = name,
                lastMessage = null,
                createdAt = now,
                updatedAt = now,
                members = emptyList(),
            )
            chat.addMember(
                ChatMember(
                    id = ChatMemberId(Uuid.random()),
                    chatId = chat.id,
                    memberType = MemberType.USER,
                    profileId = ownerId,
                    characterId = null,
                    role = MemberRole.OWNER,
                    joinedAt = now,
                    updatedAt = now,
                )
            )
            chat.addMember(
                ChatMember(
                    id = ChatMemberId(Uuid.random()),
                    chatId = chat.id,
                    memberType = MemberType.CHARACTER,
                    profileId = null,
                    characterId = characterId,
                    role = MemberRole.MEMBER,
                    joinedAt = now,
                    updatedAt = now,
                )
            )
            chat.addDomainEvent(
                ChatCreated(
                    chatId = chat.id,
                    ownerId = ownerId,
                    characterId = characterId,
                )
            )
            return chat
        }

        /**
         * 从持久化层重建聊天
         */
        fun reconstitute(
            id: ChatId,
            ownerId: UserId,
            name: String?,
            lastMessage: String?,
            createdAt: Instant,
            updatedAt: Instant,
            members: List<ChatMember>,
        ): Chat = Chat(
            id = id,
            ownerId = ownerId,
            name = name,
            lastMessage = lastMessage,
            createdAt = createdAt,
            updatedAt = updatedAt,
            members = members,
        )
    }

    /**
     * 更新最后一条消息
     */
    fun updateLastMessage(message: String) {
        this.lastMessage = message
        this.updatedAt = Clock.System.now()
    }

    /**
     * 更新聊天名称
     */
    fun updateName(newName: String?) {
        this.name = newName
        this.updatedAt = Clock.System.now()
    }

    /**
     * 获取成员（按类型）
     */
    fun getUsers(): List<ChatMember> = members.filter { it.isUser() }

    fun getCharacters(): List<ChatMember> = members.filter { it.isCharacter() }

    /**
     * 获取私聊中的角色（仅适用于私聊场景）
     */
    fun getCharacter(): CharacterId? {
        val characters = getCharacters()
        require(characters.size <= 1) { "Private chat should have at most one character member" }
        return characters.firstOrNull()?.characterId
    }

    /**
     * 获取私聊中的用户（仅适用于私聊场景）
     */
    fun getUser(): UserId? {
        val users = getUsers()
        require(users.size == 1) { "Chat must have exactly one user" }
        return users.firstOrNull()?.profileId
    }

    /**
     * 添加成员
     */
    fun addMember(member: ChatMember) {
        require(member.chatId == this.id) { "Member chatId must match Chat id" }
        val updatedMembers = members.toMutableList()
        updatedMembers.add(member)
        this.members = updatedMembers
        this.updatedAt = Clock.System.now()
    }

    /**
     * 移除成员
     */
    fun removeMember(memberId: ChatMemberId) {
        val memberToRemove = members.find { it.id == memberId }
        require(memberToRemove != null) { "Member not found" }
        require(!memberToRemove.isOwner()) { "Cannot remove owner from chat" }
        val updatedMembers = members.toMutableList()
        updatedMembers.removeIf { it.id == memberId }
        this.members = updatedMembers
        this.updatedAt = Clock.System.now()
    }

    /**
     * 判断是否为私聊
     */
    fun isPrivateChat(): Boolean = members.size == 2 &&
            getUsers().size == 1 &&
            getCharacters().size == 1
}
