package tech.hanasaki.azusa.modules.contact.domain.model

import tech.hanasaki.azusa.common.kernel.base.AggregateRoot
import tech.hanasaki.azusa.common.kernel.model.CharacterId
import tech.hanasaki.azusa.common.kernel.model.UserId
import kotlin.time.Clock
import kotlin.time.Instant

data class Contact(
    val userId: UserId,
    val characterId: CharacterId,
    var nickname: String?,
    val addedAt: Instant,
) : AggregateRoot() {
    companion object {
        /**
         * 创建新联系人
         */
        fun create(
            userId: UserId,
            characterId: CharacterId,
            nickname: String? = null,
        ): Contact = Contact(
            userId = userId,
            characterId = characterId,
            nickname = nickname,
            addedAt = Clock.System.now(),
        )

        /**
         * 从持久化层重建
         */
        fun reconstitute(
            userId: UserId,
            characterId: CharacterId,
            nickname: String?,
            addedAt: Instant,
        ): Contact = Contact(
            userId = userId,
            characterId = characterId,
            nickname = nickname,
            addedAt = addedAt,
        )
    }

    /**
     * 更新联系人昵称
     */
    fun updateNickname(nickname: String?) {
        this.nickname = nickname
    }

    /**
     * 清空昵称联系人
     */
    fun clearNickname() {
        this.nickname = null
    }
}
