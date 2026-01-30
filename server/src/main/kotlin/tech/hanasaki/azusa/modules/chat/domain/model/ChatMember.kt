package tech.hanasaki.azusa.modules.chat.domain.model

import tech.hanasaki.azusa.common.kernel.model.CharacterId
import tech.hanasaki.azusa.common.kernel.model.UserId
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * 成员类型枚举
 */
enum class MemberType {
    USER,
    CHARACTER,
}

/**
 * 成员角色枚举
 */
enum class MemberRole {
    OWNER,
    ADMIN,
    MEMBER,
}

/**
 * ChatMember 实体 - 表示聊天成员
 */
data class ChatMember(
    val id: ChatMemberId,
    val chatId: ChatId,
    val memberType: MemberType,
    val profileId: UserId?,
    val characterId: CharacterId?,
    var role: MemberRole,
    val joinedAt: Instant,
    var updatedAt: Instant,
) {
    init {
        require(
            (memberType == MemberType.USER && profileId != null && characterId == null) ||
                (memberType == MemberType.CHARACTER && characterId != null && profileId == null)
        ) {
            "MemberType and IDs must match: USER requires profileId, CHARACTER requires characterId"
        }
    }

    fun isUser(): Boolean = memberType == MemberType.USER

    fun isCharacter(): Boolean = memberType == MemberType.CHARACTER

    fun isOwner(): Boolean = role == MemberRole.OWNER

    fun isAdmin(): Boolean = role == MemberRole.ADMIN

    fun updateRole(newRole: MemberRole) {
        this.role = newRole
        this.updatedAt = Clock.System.now()
    }
}
