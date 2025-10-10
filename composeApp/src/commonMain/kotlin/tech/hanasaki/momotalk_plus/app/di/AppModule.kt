package tech.hanasaki.momotalk_plus.app.di

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.sse.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.util.date.*
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tech.hanasaki.momotalk_plus.app.viewmodel.AppViewModel
import tech.hanasaki.momotalk_plus.core.data.datasource.local.CharacterLocalDataSource
import tech.hanasaki.momotalk_plus.core.data.datasource.local.LocalCookieStorage
import tech.hanasaki.momotalk_plus.core.data.datasource.local.LocalSessionDataSource
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.api.*
import tech.hanasaki.momotalk_plus.core.data.repository.CharacterRepositoryImpl
import tech.hanasaki.momotalk_plus.core.data.repository.SessionRepositoryImpl
import tech.hanasaki.momotalk_plus.core.data.repository.UploadImageRepositoryImpl
import tech.hanasaki.momotalk_plus.core.domain.repository.CharacterRepository
import tech.hanasaki.momotalk_plus.core.domain.repository.ContactProvider
import tech.hanasaki.momotalk_plus.core.domain.repository.SessionRepository
import tech.hanasaki.momotalk_plus.core.domain.repository.UploadImageRepository
import tech.hanasaki.momotalk_plus.core.domain.usecase.*
import tech.hanasaki.momotalk_plus.core.theme.ThemeManager
import tech.hanasaki.momotalk_plus.features.auth.data.datasource.remote.api.AuthApi
import tech.hanasaki.momotalk_plus.features.auth.data.datasource.remote.api.createAuthApi
import tech.hanasaki.momotalk_plus.features.auth.data.repository.AuthRepositoryImpl
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.*
import tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel.ForgotPasswordViewModel
import tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel.LoginViewModel
import tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel.RegisterViewModel
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.ChatRemoteDatasource
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.api.ChatApi
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.api.createChatApi
import tech.hanasaki.momotalk_plus.features.chats.data.repository.ChatRepositoryImpl
import tech.hanasaki.momotalk_plus.features.chats.domain.repository.ChatRepository
import tech.hanasaki.momotalk_plus.features.chats.domain.usecase.*
import tech.hanasaki.momotalk_plus.features.chats.presentation.viewmodel.ChatDetailViewModel
import tech.hanasaki.momotalk_plus.features.chats.presentation.viewmodel.ChatsViewModel
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.remote.api.ContactApi
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.remote.api.createContactApi
import tech.hanasaki.momotalk_plus.features.contacts.data.repository.ContactProviderImpl
import tech.hanasaki.momotalk_plus.features.contacts.data.repository.ContactRepositoryImpl
import tech.hanasaki.momotalk_plus.features.contacts.domain.repository.ContactRepository
import tech.hanasaki.momotalk_plus.features.contacts.domain.usecase.AddContactUseCase
import tech.hanasaki.momotalk_plus.features.contacts.domain.usecase.DeleteContactUseCase
import tech.hanasaki.momotalk_plus.features.contacts.domain.usecase.ListContactUseCase
import tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel.ContactDetailViewModel
import tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel.ContactEditViewModel
import tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel.ContactListViewModel
import tech.hanasaki.momotalk_plus.features.contacts.presentation.viewmodel.ContactsManageViewModel
import tech.hanasaki.momotalk_plus.features.home.presentation.viewmodel.HomeViewModel
import tech.hanasaki.momotalk_plus.features.profile.data.datasource.remote.api.ProfileApi
import tech.hanasaki.momotalk_plus.features.profile.data.datasource.remote.api.createProfileApi
import tech.hanasaki.momotalk_plus.features.profile.data.repository.ProfileRepositoryImpl
import tech.hanasaki.momotalk_plus.features.profile.domain.repository.ProfileRepository
import tech.hanasaki.momotalk_plus.features.profile.domain.usecase.UpdateUserProfileUseCase
import tech.hanasaki.momotalk_plus.features.profile.presentation.viewmodel.ProfileViewModel
import tech.hanasaki.momotalk_plus.features.settings.presentation.viewmodel.SettingsViewModel
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

expect val platformModule: Module

val storageModule = module {
    single { LocalSessionDataSource(get()) }

    single { LocalCookieStorage(get()) }
}

@OptIn(ExperimentalTime::class)
val networkModule = module {
    single<Ktorfit> {
        Ktorfit.Builder()
            .baseUrl("http://localhost:3001/api/")
            .httpClient {
                install(Logging) {
                    logger = Logger.DEFAULT
                    level = LogLevel.ALL
                }
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
                            // 优先从数据库读取 Cookie，确保使用最新的有效 Cookie
                            val cookieStorage = get<LocalCookieStorage>()
                            val dbCookies = cookieStorage.getAllCookie()
                            if (dbCookies.isNotEmpty()) {
                                // 同时更新内存缓存
                                val cookies = dbCookies.map {
                                    Cookie(
                                        name = it.name,
                                        encoding = CookieEncoding.RAW,
                                        value = it.value,
                                        maxAge = it.maxAge,
                                        expires = it.expires?.takeIf { exp -> exp.isNotBlank() }?.let { exp ->
                                            GMTDate(
                                                Instant.parse(exp).toEpochMilliseconds()
                                            )
                                        },
                                        domain = it.domain,
                                        path = it.path,
                                        secure = it.secure,
                                        httpOnly = it.httpOnly,
                                        extensions = it.extensions
                                    )
                                }
                                return cookies
                            }
                            return emptyList()
                        }

                        override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
                            delegate.addCookie(requestUrl, cookie)
                            val cookieStorage = get<LocalCookieStorage>()
                            cookieStorage.saveCookie(cookie, cookie.name)
                        }

                        override fun close() = delegate.close()

                    }
                }
                install(SSE) {
                    showRetryEvents()
                    showCommentEvents()
                }
            }
            .build()
    }

    // 如果有地方需要直接使用 HttpClient，从 Ktorfit 中获取
    single<HttpClient> {
        get<Ktorfit>().httpClient
    }
}

val themeModule = module {
    single { ThemeManager() }
}

val uploadModule = module {
    single<UploadApi> {
        get<Ktorfit>().createUploadApi()
    }

    single<UploadImageRepository> { UploadImageRepositoryImpl(get()) }

    factoryOf(::UploadImageUseCase)
}
val characterModule = module {
    single<CharacterApi> {
        get<Ktorfit>().createCharacterApi()
    }

    single<CharacterRepository> { CharacterRepositoryImpl(get(), get()) }

    factoryOf(::CharacterDetailUseCase)
    factoryOf(::ListCharacterUseCase)
    factoryOf(::UpdateCharacterUseCase)

}

val sessionModule = module {
    single<SessionApi> {
        get<Ktorfit>().createSessionApi()
    }

    single<SessionRepository> { SessionRepositoryImpl(get(), get(), get()) }

    factoryOf(::ObserveCurrentUserUseCase)
    factoryOf(::ObserveLoginStateUseCase)
    factoryOf(::LogoutUseCase)
}

val authModule = module {
    single<AuthApi> {
        get<Ktorfit>().createAuthApi()
    }
    single<AuthRepository> { AuthRepositoryImpl(get()) }

    factoryOf(::SignInUserUseCase)
    factoryOf(::SignUpUserUseCase)
    factoryOf(::SignOutUserUseCase)
    factoryOf(::SendEmailVerificationUseCase)
    factoryOf(::SendPasswordResetEmailUseCase)
    factoryOf(::VerifyEmailUseCase)
    factoryOf(::ResetPasswordUseCase)
}

val contactModule = module {
    single<ContactApi> {
        get<Ktorfit>().createContactApi()
    }
    single<ContactRepository> { ContactRepositoryImpl(get(), get()) }
    single<ContactProvider> { ContactProviderImpl(get()) }

    factoryOf(::AddContactUseCase)
    factoryOf(::DeleteContactUseCase)
    factoryOf(::ListContactUseCase)
}

val chatModule = module {
    single<ChatApi> {
        get<Ktorfit>().createChatApi()
    }
    factoryOf(::ChatRemoteDatasource)
    single<ChatRepository> { ChatRepositoryImpl(get(), get(), get()) }

    factoryOf(::ClearChatHistoryUseCase)
    factoryOf(::CreateChatUseCase)
    factoryOf(::DeleteChatUseCase)
    factoryOf(::GetChatHistoryUseCase)
    factoryOf(::GetChatsUseCase)
    factoryOf(::SendMessageStreamUseCase)
    factoryOf(::UpdateChatUseCase)
    factoryOf(::GetChatInfoUseCase)
}


val profileModule = module {
    single<ProfileApi> {
        get<Ktorfit>().createProfileApi()
    }

    single<ProfileRepository> { ProfileRepositoryImpl(get()) }

    factoryOf(::UpdateUserProfileUseCase)
}


val datasourceModule = module {
    factoryOf(::CharacterLocalDataSource)
}

val viewModelModule = module {
    viewModelOf(::AppViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::ForgotPasswordViewModel)
    viewModelOf(::RegisterViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::ChatsViewModel)
    viewModelOf(::ChatDetailViewModel)
    viewModelOf(::ContactListViewModel)
    viewModelOf(::ContactDetailViewModel)
    viewModelOf(::ContactEditViewModel)
    viewModelOf(::ContactsManageViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::ProfileViewModel)
}

val appModule =
    listOf(
        platformModule,
        storageModule,
        networkModule,
        themeModule,
        datasourceModule,
        uploadModule,
        characterModule,
        sessionModule,
        authModule,
        contactModule,
        chatModule,
        profileModule,
        viewModelModule
    )