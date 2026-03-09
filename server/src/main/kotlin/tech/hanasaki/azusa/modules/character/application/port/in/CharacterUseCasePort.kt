package tech.hanasaki.azusa.modules.character.application.port.`in`

import tech.hanasaki.azusa.modules.character.domain.model.Character
import tech.hanasaki.azusa.modules.character.domain.model.KnowledgeSubscription
import tech.hanasaki.azusa.shared.domain.model.vo.AvatarUrl
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface CharacterUseCasePort {
    suspend fun createCharacter(
        authorId: UserId,
        name: String,
        avatar: AvatarUrl? = null,
        bio: String? = null,
        originPrompt: String? = null,
        isPublic: Boolean = false,
    ): Character

    suspend fun updateCharacter(
        userId: UserId,
        characterId: CharacterId,
        name: String,
        avatar: AvatarUrl? = null,
        bio: String? = null,
        originPrompt: String? = null,
        isPublic: Boolean = false,
    ): Character

    suspend fun updateCharacterAvatar(userId: UserId, characterId: CharacterId, avatar: AvatarUrl): Character

    suspend fun deleteCharacter(userId: UserId, characterId: CharacterId)

    suspend fun getKnowledgeSubscriptions(
        characterId: CharacterId,
    ): List<KnowledgeSubscription>

    suspend fun subscribeKnowledgeBase(
        authorId: UserId,
        characterId: CharacterId,
        knowledgeBaseId: KnowledgeBaseId,
        priority: Int = 0,
    )

    suspend fun unsubscribeKnowledgeBase(
        authorId: UserId,
        characterId: CharacterId,
        knowledgeBaseId: KnowledgeBaseId,
    )
}
