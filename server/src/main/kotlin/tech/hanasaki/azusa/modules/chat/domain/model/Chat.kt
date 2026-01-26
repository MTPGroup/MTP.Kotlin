package tech.hanasaki.azusa.modules.chat.domain.model

import tech.hanasaki.azusa.common.kernel.base.AggregateRoot
import tech.hanasaki.azusa.common.kernel.model.CharacterId
import tech.hanasaki.azusa.common.kernel.model.UserId
import tech.hanasaki.azusa.modules.chat.domain.events.ChatCreatedEvent
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Chat 聚合根 - 表示用户与 AI 角色的聊天
 */
data class Chat(
    val id: ChatId,
    val ownerId: UserId,
    val characterId: CharacterId,
    var name: String?,
    var lastMessage: String?,
    val createdAt: Instant,
    var updatedAt: Instant,
) : AggregateRoot() {

    companion object {
        /**
         * 创建新聊天
         */
        fun create(
            ownerId: UserId,
            characterId: CharacterId,
            name: String? = null,
        ): Chat {
            val now = Clock.System.now()
            val chat = Chat(
                id = ChatId(java.util.UUID.randomUUID()),
                ownerId = ownerId,
                characterId = characterId,
                name = name,
                lastMessage = null,
                createdAt = now,
                updatedAt = now,
            )
            chat.addDomainEvent(
                ChatCreatedEvent(
                    chatId = chat.id,
                    ownerId = ownerId,
                    characterId = characterId,
                )
            )
            return chat
        }

        /**
         * 从持久化层重建聊天（不触发事件）
         */
        fun reconstitute(
            id: ChatId,
            ownerId: UserId,
            characterId: CharacterId,
            name: String?,
            lastMessage: String?,
            createdAt: Instant,
            updatedAt: Instant,
        ): Chat = Chat(
            id = id,
            ownerId = ownerId,
            characterId = characterId,
            name = name,
            lastMessage = lastMessage,
            createdAt = createdAt,
            updatedAt = updatedAt,
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
}
