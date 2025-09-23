package tech.hanasaki.momotalk_plus.app.di

import LocalCookieStorage
import com.russhwolf.settings.Settings
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tech.hanasaki.momotalk_plus.app.viewmodel.AppViewModel
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.UserRemoteDatasource
import tech.hanasaki.momotalk_plus.core.data.repository.UserRepositoryImpl
import tech.hanasaki.momotalk_plus.core.domain.repository.UserRepository
import tech.hanasaki.momotalk_plus.core.domain.usecase.GetLoginStateUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.GetUserInfoUseCase
import tech.hanasaki.momotalk_plus.core.domain.usecase.LogoutUserUseCase
import tech.hanasaki.momotalk_plus.features.auth.data.datasource.remote.AuthRemoteDatasource
import tech.hanasaki.momotalk_plus.features.auth.data.repository.AuthRepositoryImpl
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.*
import tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel.ForgotPasswordViewModel
import tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel.LoginViewModel
import tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel.RegisterViewModel
import tech.hanasaki.momotalk_plus.features.chats.presentation.viewmodel.ChatsViewModel
import tech.hanasaki.momotalk_plus.features.home.presentation.viewmodel.HomeViewModel

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
}

val storageModule = module {
    single { Settings() }
    single { LocalCookieStorage(get()) }
}

val networkModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(HttpCookies) {
                storage = object : CookiesStorage {
                    private val delegate = AcceptAllCookiesStorage()

                    override suspend fun get(requestUrl: Url): List<Cookie> {
                        val mem = delegate.get(requestUrl)
                        if (mem.isNotEmpty()) return mem
                        val cookieStorage: LocalCookieStorage = get()
                        val cookie = cookieStorage.getCookie("better-auth.session_token")
                        return cookie?.let { listOf(it) } ?: emptyList()
                    }

                    override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
                        delegate.addCookie(requestUrl, cookie)
                        if (cookie.name == "better-auth.session_token") {
                            val cookieStorage: LocalCookieStorage = get()
                            cookieStorage.saveCookie(cookie, "better-auth.session_token")
                        }
                    }

                    override fun close() = delegate.close()

                }
            }
            install(Logging) {
                level = LogLevel.ALL
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