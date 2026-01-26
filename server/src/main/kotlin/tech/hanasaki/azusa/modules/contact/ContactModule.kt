package tech.hanasaki.azusa.modules.contact

import org.koin.dsl.module
import tech.hanasaki.azusa.modules.contact.application.service.ContactService
import tech.hanasaki.azusa.modules.contact.domain.repository.ContactRepository
import tech.hanasaki.azusa.modules.contact.infrastructure.persistence.repository.ExposedContactRepository

fun contactModules() = module {
    single<ContactRepository> { ExposedContactRepository() }
    factory { ContactService(get()) }
}