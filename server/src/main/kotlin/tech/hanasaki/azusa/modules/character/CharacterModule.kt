package tech.hanasaki.azusa.modules.character

import org.koin.dsl.module
import tech.hanasaki.azusa.modules.character.adapter.out.persistence.repository.ExposedCharacterQueryRepository
import tech.hanasaki.azusa.modules.character.adapter.out.persistence.repository.ExposedCharacterRepository
import tech.hanasaki.azusa.modules.character.adapter.out.persistence.repository.ExposedKnowledgeSubscriptionRepository
import tech.hanasaki.azusa.modules.character.application.port.`in`.CharacterQueryUseCasePort
import tech.hanasaki.azusa.modules.character.application.port.`in`.CharacterUseCasePort
import tech.hanasaki.azusa.modules.character.application.port.out.CharacterQueryRepositoryPort
import tech.hanasaki.azusa.modules.character.application.service.CharacterQueryService
import tech.hanasaki.azusa.modules.character.application.service.CharacterService
import tech.hanasaki.azusa.modules.character.domain.port.CharacterRepositoryPort
import tech.hanasaki.azusa.modules.character.domain.port.KnowledgeSubscriptionRepositoryPort

fun characterModule() = module {
    single<CharacterRepositoryPort> { ExposedCharacterRepository() }
    single<CharacterQueryRepositoryPort> { ExposedCharacterQueryRepository() }
    single<KnowledgeSubscriptionRepositoryPort> { ExposedKnowledgeSubscriptionRepository() }

    single<CharacterQueryUseCasePort> {
        CharacterQueryService(
            characterQueryRepository = get(),
            characterRepository = get(),
            tx = get(),
        )
    }

    single<CharacterUseCasePort> {
        CharacterService(
            get(),
            get(),
            get(),
            get(),
        )
    }
}
