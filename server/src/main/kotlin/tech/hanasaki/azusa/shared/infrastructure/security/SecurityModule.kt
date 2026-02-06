package tech.hanasaki.azusa.shared.infrastructure.security

import org.koin.dsl.module
import tech.hanasaki.azusa.shared.port.out.StringEncoderPort

fun securityModule() = module {
    single<StringEncoderPort> { StringEncoder() }
}