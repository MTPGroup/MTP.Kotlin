package tech.hanasaki.momotalk_plus.app.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tech.hanasaki.momotalk_plus.core.data.repository.createUserRepository
import tech.hanasaki.momotalk_plus.features.login.domain.repository.AuthRepository
import tech.hanasaki.momotalk_plus.core.domain.repository.UserRepository
import tech.hanasaki.momotalk_plus.features.login.domain.usecase.*
import tech.hanasaki.momotalk_plus.features.login.presentation.viewmodel.LoginViewModel
import tech.hanasaki.momotalk_plus.features.login.data.datasource.remote.AuthRemoteDatasource
import tech.hanasaki.momotalk_plus.features.login.data.repository.AuthRepositoryImpl
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.UserRemoteDatasource
import tech.hanasaki.momotalk_plus.features.login.presentation.viewmodel.ForgotPasswordViewModel
import tech.hanasaki.momotalk_plus.features.login.presentation.viewmodel.RegisterViewModel

val commonModule = module {
    factoryOf(::LoginUserUseCase)
    factoryOf(::ObserveAuthStateUseCase)
    factoryOf(::ResetPasswordUseCase)
    factoryOf(::SendResetPasswordEmailUseCase)
    factoryOf(::SignUpUserUseCase)
    factoryOf(::VerifyPasswordResetCodeUseCase)
}

val networkModule = module {
    single<HttpClient> {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
        }
    }
}

val datasourceModule = module {
    factoryOf(::AuthRemoteDatasource)
    factoryOf(::UserRemoteDatasource)
}

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<UserRepository> { createUserRepository() }
}

val viewModelModule = module {
    viewModelOf(::LoginViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::RegisterViewModel)
}

val appModule =
    listOf(commonModule, networkModule, datasourceModule, repositoryModule, viewModelModule)