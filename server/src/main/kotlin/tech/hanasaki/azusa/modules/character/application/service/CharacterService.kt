package tech.hanasaki.azusa.modules.character.application.service

import tech.hanasaki.azusa.modules.character.application.port.`in`.CharacterUseCasePort
import tech.hanasaki.azusa.modules.character.domain.events.CharacterDeleted
import tech.hanasaki.azusa.modules.character.domain.model.Character
import tech.hanasaki.azusa.modules.character.domain.model.KnowledgeSubscription
import tech.hanasaki.azusa.modules.character.domain.port.CharacterRepositoryPort
import tech.hanasaki.azusa.modules.character.domain.port.KnowledgeSubscriptionRepositoryPort
import tech.hanasaki.azusa.shared.domain.exception.AuthorizationException
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException
import tech.hanasaki.azusa.shared.domain.model.base.publishAndClear
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.AvatarUrl
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.port.out.DomainEventBusPort
import tech.hanasaki.azusa.shared.port.out.TransactionalPort

class CharacterService(
    private val characterRepository: CharacterRepositoryPort,
    private val knowledgeSubscriptionRepository: KnowledgeSubscriptionRepositoryPort,
    private val domainEventBus: DomainEventBusPort,
    private val tx: TransactionalPort,
) : CharacterUseCasePort {
    override suspend fun listMyCharacters(authorId: UserId, page: Int, limit: Int): PageResult<Character> =
        tx.readOnly {
            return@readOnly characterRepository.findByAuthorIdPaged(authorId, page, limit)
        }


    override suspend fun listPublicCharacters(page: Int, limit: Int): PageResult<Character> = tx.readOnly {
        return@readOnly characterRepository.findPublicCharactersPaged(page, limit)
    }

    override suspend fun searchCharacters(
        query: String,
        page: Int,
        limit: Int,
        userId: UserId?,
    ): PageResult<Character> =
        tx.readOnly {
            return@readOnly characterRepository.searchCharacters(query, page, limit, userId)
        }

    override suspend fun getCharacter(authorId: UserId?, characterId: CharacterId): Character = tx.readOnly {
        val character = characterRepository.findById(characterId)
            ?: throw NotFoundException("角色不存在")
        if (!character.isPublic && character.authorId != authorId) {
            throw AuthorizationException("权限不足")
        }
        return@readOnly character
    }

    override suspend fun createCharacter(
        authorId: UserId,
        name: String,
        avatar: AvatarUrl?,
        bio: String?,
        originPrompt: String?,
        isPublic: Boolean,
    ): Character = tx.execute {
        val character = Character.create(
            authorId = authorId,
            name = name,
            avatar = avatar,
            bio = bio,
            originPrompt = originPrompt,
            isPublic = isPublic,
        )
        characterRepository.save(character)
        character.publishAndClear(domainEventBus)
        return@execute character
    }

    override suspend fun updateCharacter(
        userId: UserId,
        characterId: CharacterId,
        name: String,
        avatar: AvatarUrl?,
        bio: String?,
        originPrompt: String?,
        isPublic: Boolean,
    ): Character = tx.execute {
        val character = validCharacter(characterId, userId)
        character.update(
            name = name,
            avatar = avatar,
            bio = bio,
            originPrompt = originPrompt,
            isPublic = isPublic,
        )
        characterRepository.save(character)
        character.publishAndClear(domainEventBus)
        return@execute character
    }

    override suspend fun updateCharacterAvatar(
        userId: UserId,
        characterId: CharacterId,
        avatar: AvatarUrl,
    ): Character = tx.execute {
        val character = validCharacter(characterId, userId)
        character.avatar = avatar
        characterRepository.save(character)
        return@execute character
    }

    override suspend fun deleteCharacter(userId: UserId, characterId: CharacterId) = tx.execute {
        validCharacter(characterId, userId)
        characterRepository.deleteById(characterId)
        domainEventBus.publish(CharacterDeleted(characterId, userId))
    }

    /**
     * 获取角色订阅的知识库列表
     */
    override suspend fun getKnowledgeSubscriptions(
        characterId: CharacterId,
    ): List<KnowledgeSubscription> = tx.readOnly {
        return@readOnly knowledgeSubscriptionRepository.findByCharacterId(characterId)
    }

    /**
     * 订阅知识库到角色
     */
    override suspend fun subscribeKnowledgeBase(
        authorId: UserId,
        characterId: CharacterId,
        knowledgeBaseId: KnowledgeBaseId,
        priority: Int,
    ) = tx.execute {
        validCharacter(characterId, authorId)
        knowledgeSubscriptionRepository.subscribe(characterId, knowledgeBaseId, priority)
    }

    /**
     * 解除角色与知识库的订阅
     */
    override suspend fun unsubscribeKnowledgeBase(
        authorId: UserId,
        characterId: CharacterId,
        knowledgeBaseId: KnowledgeBaseId,
    ) = tx.execute {
        validCharacter(characterId, authorId)
        knowledgeSubscriptionRepository.unsubscribe(characterId, knowledgeBaseId)
    }

    private suspend fun validCharacter(characterId: CharacterId, authorId: UserId): Character {
        val character = characterRepository.findById(characterId)
            ?: throw NotFoundException("角色不存在")
        if (character.authorId != authorId) {
            throw AuthorizationException("权限不足")
        }
        return character
    }
}
