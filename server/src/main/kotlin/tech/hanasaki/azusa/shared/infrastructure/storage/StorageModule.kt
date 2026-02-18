package tech.hanasaki.azusa.shared.infrastructure.storage

import io.ktor.server.config.*
import org.koin.dsl.module
import tech.hanasaki.azusa.shared.infrastructure.config.S3Config
import tech.hanasaki.azusa.shared.infrastructure.config.readS3Config
import tech.hanasaki.azusa.shared.port.out.FileStoragePort

fun storageModule(config: ApplicationConfig) = module {
    single<S3Config> { config.readS3Config() }
    single<FileStoragePort> { S3Storage(get()) }
}
