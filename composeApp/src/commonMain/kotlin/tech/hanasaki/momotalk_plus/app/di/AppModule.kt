package tech.hanasaki.momotalk_plus.app.di

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.observable.makeObservable
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import tech.hanasaki.momotalk_plus.app.viewmodel.AppViewModel
import tech.hanasaki.momotalk_plus.core.data.datasource.local.TokenStorage
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.UserRemoteDatasource
import tech.hanasaki.momotalk_plus.core.data.repository.UserRepositoryImpl
import tech.hanasaki.momotalk_plus.core.domain.repository.UserRepository
import tech.hanasaki.momotalk_plus.core.domain.usecase.*
import tech.hanasaki.momotalk_plus.features.auth.data.datasource.remote.AuthRemoteDatasource
import tech.hanasaki.momotalk_plus.features.auth.data.repository.AuthRepositoryImpl
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.*
import tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel.ForgotPasswordViewModel
import tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel.LoginViewModel
import tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel.RegisterViewModel
import tech.hanasaki.momotalk_plus.features.chats.presentation.viewmodel.ChatsViewModel
import tech.hanasaki.momotalk_plus.features.home.presentation.viewmodel.HomeViewModel

@Serializable
data class RefreshTokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
)

val commonModule = module {
    factoryOf(::SignInUserUseCase)
    factoryOf(::SignUpUserUseCase)
    factoryOf(::SignOutUserUseCase)
    factoryOf(::SendEmailVerificationUseCase)
    factoryOf(::SendPasswordResetEmailUseCase)
    factoryOf(::VerifyEmailUseCase)
    factoryOf(::ResetPasswordUseCase)
    factoryOf(::GetUserInfoUseCase)
    factoryOf(::GetLoginStateUseCase)
    factoryOf(::LogoutUserUseCase)
    factoryOf(::RefreshIdTokenUseCase)
    factoryOf(::SaveLoginStateUseCase)
}

@OptIn(ExperimentalSettingsApi::class)
val storageModule = module {
    single<ObservableSettings> {
        Settings().makeObservable()
    }
    single { TokenStorage(get()) }
}

val networkModule = module {
    single(named("noAuthClient")) {
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

    single(named("authClient")) {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(HttpCookies)
        }
    }
}

val datasourceModule = module {
    factory<AuthRemoteDatasource> { AuthRemoteDatasource(get(named("noAuthClient"))) }
    factory<UserRemoteDatasource> { UserRemoteDatasource(get(named("authClient"))) }
}

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get()) }
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
    listOf(
        commonModule,
        networkModule,
        datasourceModule,
        repositoryModule,
        viewModelModule,
        storageModule
    )