package tech.hanasaki.azusa.modules.character

import io.ktor.server.config.*
import org.koin.dsl.module
import tech.hanasaki.azusa.modules.character.application.service.CharacterService
import tech.hanasaki.azusa.modules.character.domain.repository.CharacterRepository
import tech.hanasaki.azusa.modules.character.domain.repository.KnowledgeSubscriptionRepository
import tech.hanasaki.azusa.modules.character.infrastructure.persistence.repository.ExposedCharacterRepository
import tech.hanasaki.azusa.modules.character.infrastructure.persistence.repository.ExposedKnowledgeSubscriptionRepository

fun characterModule(config: ApplicationConfig) = module {
    single<CharacterRepository> { ExposedCharacterRepository() }
    single<KnowledgeSubscriptionRepository> { ExposedKnowledgeSubscriptionRepository() }
    single {
        CharacterService(
            get(),
            get(),
            get(),
        )
    }
}
