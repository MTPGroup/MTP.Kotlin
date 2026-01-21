package tech.hanasaki.azusa.modules.character

import io.ktor.server.config.*
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import tech.hanasaki.azusa.modules.character.application.service.CharacterService
import tech.hanasaki.azusa.modules.character.domain.repository.CharacterRepository
import tech.hanasaki.azusa.modules.character.infrastructure.persistence.repository.ExposedCharacterRepository

fun characterModule(config: ApplicationConfig) = module {
    single<CharacterRepository> { ExposedCharacterRepository() }
    factoryOf(::CharacterService)
}
