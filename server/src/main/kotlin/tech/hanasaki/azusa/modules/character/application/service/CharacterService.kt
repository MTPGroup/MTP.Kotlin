package tech.hanasaki.azusa.modules.character.application.service

import tech.hanasaki.azusa.modules.character.application.command.CreateCharacterCommand
import tech.hanasaki.azusa.modules.character.application.command.UpdateCharacterCommand
import tech.hanasaki.azusa.modules.character.domain.model.Character
import tech.hanasaki.azusa.modules.character.domain.model.KnowledgeSubscription
import tech.hanasaki.azusa.modules.character.domain.repository.CharacterRepository
import tech.hanasaki.azusa.modules.character.domain.repository.KnowledgeSubscriptionRepository
import tech.hanasaki.azusa.shared.domain.exception.AuthorizationException
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.KnowledgeBaseId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.port.out.EventPublisherPort

class CharacterService(
    private val characterRepository: CharacterRepository,
    private val knowledgeSubscriptionRepository: KnowledgeSubscriptionRepository,
    private val eventPublisher: EventPublisherPort,
) {
    suspend fun listMyCharacters(authorId: UserId): List<Character> {
        return characterRepository.findByAuthorId(authorId)
    }

    suspend fun listMyCharactersPaged(authorId: UserId, page: Int, limit: Int): PageResult<Character> {
        return characterRepository.findByAuthorIdPaged(authorId, page, limit)
    }

    suspend fun listPublicCharacters(): List<Character> {
        return characterRepository.findPublicCharacters()
    }

    suspend fun listPublicCharactersPaged(page: Int, limit: Int): PageResult<Character> {
        return characterRepository.findPublicCharactersPaged(page, limit)
    }

    suspend fun searchPublicCharacters(query: String, page: Int, limit: Int): PageResult<Character> {
        return characterRepository.searchPublicCharacters(query, page, limit)
    }

    suspend fun getCharacter(authorId: UserId, characterId: CharacterId): Character {
        val character = characterRepository.findById(characterId)
            ?: throw NotFoundException("Character not found")
        if (!character.isPublic && character.authorId != authorId) {
            throw AuthorizationException("Access denied")
        }
        return character
    }

    suspend fun createCharacter(authorId: UserId, cmd: CreateCharacterCommand): Character {
        val character = Character.create(
            authorId = authorId,
            name = cmd.name,
            avatar = cmd.avatar,
            bio = cmd.bio,
            originPrompt = cmd.originPrompt,
            isPublic = cmd.isPublic,
        )
        characterRepository.save(character)
        eventPublisher.publishAll(character.domainEvents)
        character.clearDomainEvents()
        return character
    }

    suspend fun updateCharacter(authorId: UserId, characterId: CharacterId, cmd: UpdateCharacterCommand): Character {
        val character = characterRepository.findById(characterId)
            ?: throw NotFoundException("Character not found")
        if (character.authorId != authorId) {
            throw AuthorizationException("Access denied")
        }
        character.update(
            name = cmd.name,
            avatar = cmd.avatar,
            bio = cmd.bio,
            originPrompt = cmd.originPrompt,
            isPublic = cmd.isPublic,
        )
        characterRepository.save(character)
        eventPublisher.publishAll(character.domainEvents)
        character.clearDomainEvents()
        return character
    }

    suspend fun deleteCharacter(authorId: UserId, characterId: CharacterId) {
        val character = characterRepository.findById(characterId)
            ?: throw NotFoundException("Character not found")
        if (character.authorId != authorId) {
            throw AuthorizationException("Access denied")
        }
        character.markDeleted()
        // 删除角色时同时删除所有知识库订阅
        knowledgeSubscriptionRepository.unsubscribeAll(characterId)
        characterRepository.deleteById(characterId)
        eventPublisher.publishAll(character.domainEvents)
        character.clearDomainEvents()
    }

    /**
     * 获取角色订阅的知识库列表
     */
    suspend fun getKnowledgeSubscriptions(
        authorId: UserId,
        characterId: CharacterId,
    ): List<KnowledgeSubscription> {
        val character = characterRepository.findById(characterId)
            ?: throw NotFoundException("Character not found")
        if (character.authorId != authorId) {
            throw AuthorizationException("Access denied")
        }
        return knowledgeSubscriptionRepository.findByCharacterId(characterId)
    }

    /**
     * 订阅知识库到角色
     */
    suspend fun subscribeKnowledgeBase(
        authorId: UserId,
        characterId: CharacterId,
        knowledgeBaseId: KnowledgeBaseId,
        priority: Int = 0,
    ) {
        val character = characterRepository.findById(characterId)
            ?: throw NotFoundException("Character not found")
        if (character.authorId != authorId) {
            throw AuthorizationException("Access denied")
        }
        knowledgeSubscriptionRepository.subscribe(characterId, knowledgeBaseId, priority)
    }

    /**
     * 解除角色与知识库的订阅
     */
    suspend fun unsubscribeKnowledgeBase(
        authorId: UserId,
        characterId: CharacterId,
        knowledgeBaseId: KnowledgeBaseId,
    ) {
        val character = characterRepository.findById(characterId)
            ?: throw NotFoundException("Character not found")
        if (character.authorId != authorId) {
            throw AuthorizationException("Access denied")
        }
        knowledgeSubscriptionRepository.unsubscribe(characterId, knowledgeBaseId)
    }

}
