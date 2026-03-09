package tech.hanasaki.azusa.modules.character.domain.model

import tech.hanasaki.azusa.modules.character.domain.events.CharacterCreated
import tech.hanasaki.azusa.modules.character.domain.events.CharacterUpdated
import tech.hanasaki.azusa.shared.domain.model.base.AggregateRoot
import tech.hanasaki.azusa.shared.domain.model.vo.AvatarUrl
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

class Character private constructor(
    val id: CharacterId,
    val authorId: UserId,
    var name: String,
    var avatar: AvatarUrl?,
    var bio: String?,
    var tags: List<String>,
    var exampleMessages: List<CharacterExampleMessage>,
    var originPrompt: String?,
    var isPublic: Boolean,
    val createdAt: Instant,
    var updatedAt: Instant,
) : AggregateRoot() {

    companion object {
        /**
         * 创建新角色
         */
        fun create(
            authorId: UserId,
            name: String,
            avatar: AvatarUrl? = null,
            bio: String? = null,
            tags: List<String> = emptyList(),
            exampleMessages: List<CharacterExampleMessage> = emptyList(),
            originPrompt: String? = null,
            isPublic: Boolean = false,
        ): Character {
            val now = Clock.System.now()
            val character = Character(
                id = CharacterId(Uuid.random()),
                authorId = authorId,
                name = name,
                avatar = avatar,
                bio = bio,
                tags = tags,
                exampleMessages = exampleMessages,
                originPrompt = originPrompt,
                isPublic = isPublic,
                createdAt = now,
                updatedAt = now,
            )
            character.addDomainEvent(
                CharacterCreated(
                    characterId = character.id,
                    authorId = authorId,
                    name = name,
                    isPublic = isPublic,
                )
            )
            return character
        }

        /**
         * 从持久化层重建角色（不触发事件）
         */
        fun reconstitute(
            id: CharacterId,
            authorId: UserId,
            name: String,
            avatar: AvatarUrl?,
            bio: String?,
            tags: List<String>,
            exampleMessages: List<CharacterExampleMessage>,
            originPrompt: String?,
            isPublic: Boolean,
            createdAt: Instant,
            updatedAt: Instant,
        ): Character = Character(
            id = id,
            authorId = authorId,
            name = name,
            avatar = avatar,
            bio = bio,
            tags = tags,
            exampleMessages = exampleMessages,
            originPrompt = originPrompt,
            isPublic = isPublic,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    /**
     * 更新角色信息
     */
    fun update(
        name: String,
        avatar: AvatarUrl?,
        bio: String?,
        tags: List<String>,
        exampleMessages: List<CharacterExampleMessage>,
        originPrompt: String?,
        isPublic: Boolean,
    ) {
        this.name = name
        this.avatar = avatar
        this.bio = bio
        this.tags = tags
        this.exampleMessages = exampleMessages
        this.originPrompt = originPrompt
        this.isPublic = isPublic
        this.updatedAt = Clock.System.now()

        addDomainEvent(
            CharacterUpdated(
                characterId = id,
                authorId = authorId,
                name = name,
                isPublic = isPublic,
            )
        )
    }
}
