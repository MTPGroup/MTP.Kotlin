package tech.hanasaki.momotalk_plus.app.di

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tech.hanasaki.momotalk_plus.app.viewmodel.AppViewModel
import tech.hanasaki.momotalk_plus.core.data.datasource.local.AuthSettings
import tech.hanasaki.momotalk_plus.features.login.domain.repository.AuthRepository
import tech.hanasaki.momotalk_plus.core.domain.repository.UserRepository
import tech.hanasaki.momotalk_plus.features.login.domain.usecase.*
import tech.hanasaki.momotalk_plus.features.login.presentation.viewmodel.LoginViewModel
import tech.hanasaki.momotalk_plus.features.login.data.datasource.remote.AuthRemoteDatasource
import tech.hanasaki.momotalk_plus.features.login.data.repository.AuthRepositoryImpl
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.UserRemoteDatasource
import tech.hanasaki.momotalk_plus.core.data.repository.UserRepositoryImpl
import tech.hanasaki.momotalk_plus.core.domain.usecase.*
import tech.hanasaki.momotalk_plus.features.chats.presentation.viewmodel.ChatsViewModel
import tech.hanasaki.momotalk_plus.features.home.presentation.viewmodel.HomeViewModel
import tech.hanasaki.momotalk_plus.features.login.presentation.viewmodel.ForgotPasswordViewModel
import tech.hanasaki.momotalk_plus.features.login.presentation.viewmodel.RegisterViewModel

val commonModule = module {
    factoryOf(::LoginUserUseCase)
    factoryOf(::ObserveAuthStateUseCase)
    factoryOf(::ResetPasswordUseCase)
    factoryOf(::SendResetPasswordEmailUseCase)
    factoryOf(::SignUpUserUseCase)
    factoryOf(::VerifyPasswordResetCodeUseCase)
    factoryOf(::GetUserInfoUseCase)
    factoryOf(::GetLoginStateUseCase)
    factoryOf(::LogoutUserUseCase)
    factoryOf(::RefreshIdTokenUseCase)
    factoryOf(::SaveLoginStateUseCase)
}

val settingsModule = module {
    single<ObservableSettings> {
        Settings() as ObservableSettings
    }
    single<Settings> { get<ObservableSettings>() }
    single { AuthSettings(get()) }
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
    single<AuthRepository> { AuthRepositoryImpl(get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
}

val viewModelModule = module {
    viewModelOf(::AppViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::ChatsViewModel)
}

val appModule =
    listOf(commonModule, networkModule, datasourceModule, repositoryModule, viewModelModule, settingsModule)