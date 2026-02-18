package tech.hanasaki.azusa.modules.theme

import io.ktor.server.config.*
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import tech.hanasaki.azusa.modules.theme.application.service.ThemeService
import tech.hanasaki.azusa.modules.theme.domain.repository.ThemeRepository
import tech.hanasaki.azusa.modules.theme.infrastructure.persistence.repository.ExposedThemeRepository

fun themeModule(config: ApplicationConfig) = module {
    single<ThemeRepository> { ExposedThemeRepository() }
    factoryOf(::ThemeService)
}
