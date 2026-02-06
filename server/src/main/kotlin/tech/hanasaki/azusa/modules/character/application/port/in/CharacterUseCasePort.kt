package tech.hanasaki.azusa.modules.character.application.port.`in`

import tech.hanasaki.azusa.modules.character.domain.model.Character
import tech.hanasaki.azusa.modules.character.domain.model.KnowledgeSubscription
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.AvatarUrl
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

interface CharacterUseCasePort {
    suspend fun listMyCharacters(
        authorId: UserId,
        page: Int = 1,
        limit: Int = 10,
    ): PageResult<Character>

    suspend fun listPublicCharacters(page: Int = 1, limit: Int = 10): PageResult<Character>

    suspend fun searchCharacters(
        query: String,
        page: Int = 1,
        limit: Int = 10,
        userId: UserId? = null,
    ): PageResult<Character>

    suspend fun getCharacter(authorId: UserId, characterId: CharacterId): Character

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