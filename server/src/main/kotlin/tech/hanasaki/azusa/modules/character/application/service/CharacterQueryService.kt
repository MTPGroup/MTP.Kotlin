package tech.hanasaki.azusa.modules.character.application.service

import tech.hanasaki.azusa.modules.character.application.port.`in`.CharacterQueryUseCasePort
import tech.hanasaki.azusa.modules.character.application.port.`in`.dto.CharacterFavoriteStatusView
import tech.hanasaki.azusa.modules.character.application.port.`in`.dto.CharacterView
import tech.hanasaki.azusa.modules.character.application.port.out.CharacterQueryRepositoryPort
import tech.hanasaki.azusa.modules.character.domain.port.CharacterRepositoryPort
import tech.hanasaki.azusa.shared.domain.exception.AuthorizationException
import tech.hanasaki.azusa.shared.domain.exception.NotFoundException
import tech.hanasaki.azusa.shared.domain.model.page.PageResult
import tech.hanasaki.azusa.shared.domain.model.vo.CharacterId
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.port.out.TransactionalPort

class CharacterQueryService(
    private val characterQueryRepository: CharacterQueryRepositoryPort,
    private val characterRepository: CharacterRepositoryPort,
    private val tx: TransactionalPort,
) : CharacterQueryUseCasePort {
    override suspend fun listCharacters(
        page: Int,
        limit: Int,
        query: String?,
        visibility: String?,
        scope: String?,
        authorId: UserId?,
        userId: UserId?,
        sort: String?,
        tags: Set<String>?,
    ): PageResult<CharacterView> = tx.readOnly {
        characterQueryRepository.findCharactersPaged(
            page,
            limit,
            query,
            visibility,
            scope,
            authorId,
            userId,
            sort,
            tags,
        )
    }

    override suspend fun listMyCharacters(authorId: UserId, page: Int, limit: Int): PageResult<CharacterView> =
        tx.readOnly {
            characterQueryRepository.findByAuthorIdPaged(authorId, page, limit)
        }

    override suspend fun listPublicCharacters(page: Int, limit: Int): PageResult<CharacterView> = tx.readOnly {
        characterQueryRepository.findPublicCharactersPaged(page, limit)
    }

    override suspend fun listTrendingCharacters(period: String, limit: Int): List<CharacterView> = tx.readOnly {
        characterQueryRepository.findTrendingCharacters(period, limit)
    }

    override suspend fun listRecommendedCharacters(userId: UserId, limit: Int): List<CharacterView> = tx.readOnly {
        characterQueryRepository.findRecommendedCharacters(userId, limit)
    }

    override suspend fun searchCharacters(
        query: String,
        page: Int,
        limit: Int,
        userId: UserId?,
    ): PageResult<CharacterView> = tx.readOnly {
        characterQueryRepository.searchCharacters(query, page, limit, userId)
    }

    override suspend fun getCharacter(authorId: UserId?, characterId: CharacterId): CharacterView = tx.readOnly {
        characterQueryRepository.findVisibleById(characterId, authorId)
            ?: run {
                val character = characterRepository.findById(characterId)
                    ?: throw NotFoundException("角色不存在")
                if (!character.isPublic && character.authorId != authorId) {
                    throw AuthorizationException("权限不足")
                }
                throw NotFoundException("角色不存在")
            }
    }

    override suspend fun getFavoriteStatus(userId: UserId, characterId: CharacterId): CharacterFavoriteStatusView =
        tx.readOnly {
            characterQueryRepository.findVisibleById(characterId, userId)
                ?: throw NotFoundException("角色不存在")
            characterQueryRepository.findFavoriteStatus(characterId, userId)
        }
}
