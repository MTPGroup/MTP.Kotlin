package tech.hanasaki.momotalk_plus.app.di

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.observable.makeObservable
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
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
import tech.hanasaki.momotalk_plus.core.domain.usecase.GetLoginStateUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.GetUserInfoUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.LogoutUserUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.RefreshIdTokenUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.SaveLoginStateUseCase
import tech.hanasaki.momotalk_plus.features.chats.presentation.viewmodel.ChatsViewModel
import tech.hanasaki.momotalk_plus.features.home.presentation.viewmodel.HomeViewModel
import tech.hanasaki.momotalk_plus.features.login.data.datasource.remote.AuthRemoteDatasource
import tech.hanasaki.momotalk_plus.features.login.data.repository.AuthRepositoryImpl
import tech.hanasaki.momotalk_plus.features.login.domain.repository.AuthRepository
import tech.hanasaki.momotalk_plus.features.login.domain.usecase.GetImageCaptchaUseCase
import tech.hanasaki.momotalk_plus.features.login.domain.usecase.LoginUserUseCase
import tech.hanasaki.momotalk_plus.features.login.domain.usecase.ResetPasswordUseCase
import tech.hanasaki.momotalk_plus.features.login.domain.usecase.SendVerificationCodeUseCase
import tech.hanasaki.momotalk_plus.features.login.domain.usecase.SignUpUserUseCase
import tech.hanasaki.momotalk_plus.features.login.domain.usecase.VerifyCaptchaUseCase
import tech.hanasaki.momotalk_plus.features.login.domain.usecase.VerifyCodeUseCase
import tech.hanasaki.momotalk_plus.features.login.presentation.viewmodel.ForgotPasswordViewModel
import tech.hanasaki.momotalk_plus.features.login.presentation.viewmodel.LoginViewModel
import tech.hanasaki.momotalk_plus.features.login.presentation.viewmodel.RegisterViewModel

@Serializable
data class RefreshTokenResponse(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("refresh_token")
    val refreshToken: String,
)

val commonModule = module {
    factoryOf(::LoginUserUseCase)
    factoryOf(::ResetPasswordUseCase)
    factoryOf(::SendVerificationCodeUseCase)
    factoryOf(::SignUpUserUseCase)
    factoryOf(::VerifyCodeUseCase)
    factoryOf(::GetUserInfoUseCase)
    factoryOf(::GetLoginStateUseCase)
    factoryOf(::LogoutUserUseCase)
    factoryOf(::RefreshIdTokenUseCase)
    factoryOf(::SaveLoginStateUseCase)
    factoryOf(::GetImageCaptchaUseCase)
    factoryOf(::VerifyCaptchaUseCase)
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
        val noAuthClient: HttpClient = get(named("noAuthClient"))
        val tokenStorage: TokenStorage = get()

        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        tokenStorage.getTokens()
                    }
                    refreshTokens {
                        val oldTokens = tokenStorage.getTokens()
                        val response: RefreshTokenResponse =
                            noAuthClient.post("https://cloud1-4gdmg8xt1b179a1c.api.tcloudbasegateway.com/auth/v1/refresh") {
                                contentType(ContentType.Application.Json)
                                setBody(
                                    mapOf(
                                        "refresh_token" to oldTokens?.refreshToken,
                                        "grant_type" to "refresh_token"
                                    ),
                                )
                            }.body<RefreshTokenResponse>()

                        val newTokens = BearerTokens(response.accessToken, response.refreshToken)
                        tokenStorage.saveTokens(newTokens)
                        newTokens
                    }
                }
            }
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