package tech.hanasaki.momotalk_plus.app.di

import LocalCookieStorage
import com.russhwolf.settings.Settings
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.sse.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tech.hanasaki.momotalk_plus.app.viewmodel.AppViewModel
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.CharacterRemoteDatasource
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.UserRemoteDatasource
import tech.hanasaki.momotalk_plus.core.data.repository.CharacterRepositoryImpl
import tech.hanasaki.momotalk_plus.core.data.repository.UserRepositoryImpl
import tech.hanasaki.momotalk_plus.core.domain.repository.CharacterRepository
import tech.hanasaki.momotalk_plus.core.domain.repository.ContactProvider
import tech.hanasaki.momotalk_plus.core.domain.repository.UserRepository
import tech.hanasaki.momotalk_plus.core.domain.usecase.*
import tech.hanasaki.momotalk_plus.core.theme.ThemeManager
import tech.hanasaki.momotalk_plus.features.auth.data.datasource.remote.AuthRemoteDatasource
import tech.hanasaki.momotalk_plus.features.auth.data.repository.AuthRepositoryImpl
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository
import tech.hanasaki.momotalk_plus.features.auth.domain.usecase.*
import tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel.ForgotPasswordViewModel
import tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel.LoginViewModel
import tech.hanasaki.momotalk_plus.features.auth.presentation.viewmodel.RegisterViewModel
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.remote.ChatRemoteDatasource
import tech.hanasaki.momotalk_plus.features.chats.data.repository.ChatRepositoryImpl
import tech.hanasaki.momotalk_plus.features.chats.domain.repository.ChatRepository
import tech.hanasaki.momotalk_plus.features.chats.domain.usecase.*
import tech.hanasaki.momotalk_plus.features.chats.presentation.viewmodel.ChatDetailViewModel
import tech.hanasaki.momotalk_plus.features.chats.presentation.viewmodel.ChatsViewModel
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.remote.ContactRemoteDatasource
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
import tech.hanasaki.momotalk_plus.features.profile.data.datasource.remote.ProfileRemoteDataSource
import tech.hanasaki.momotalk_plus.features.profile.data.repository.ProfileRepositoryImpl
import tech.hanasaki.momotalk_plus.features.profile.domain.repository.ProfileRepository
import tech.hanasaki.momotalk_plus.features.profile.domain.usecase.UpdateUserProfileUseCase
import tech.hanasaki.momotalk_plus.features.profile.presentation.viewmodel.ProfileViewModel
import tech.hanasaki.momotalk_plus.features.settings.data.repository.SettingsRepositoryImpl
import tech.hanasaki.momotalk_plus.features.settings.domain.repository.SettingsRepository
import tech.hanasaki.momotalk_plus.features.settings.domain.usecase.*
import tech.hanasaki.momotalk_plus.features.settings.presentation.viewmodel.SettingsViewModel

val themeModule = module {
    single { ThemeManager() }
}
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
    factoryOf(::CharacterDetailUseCase)
    factoryOf(::CreateCharacterUseCase)
    factoryOf(::DeleteCharacterUseCase)
    factoryOf(::ListCharacterUseCase)
    factoryOf(::UpdateCharacterUseCase)
    factoryOf(::AddContactUseCase)
    factoryOf(::DeleteContactUseCase)
    factoryOf(::ListContactUseCase)
    factoryOf(::ClearChatHistoryUseCase)
    factoryOf(::CreateChatUseCase)
    factoryOf(::DeleteChatUseCase)
    factoryOf(::GetChatHistoryUseCase)
    factoryOf(::GetChatsUseCase)
    factoryOf(::SendMessageStreamUseCase)
    factoryOf(::UpdateChatUseCase)
    factoryOf(::GetChatInfoUseCase)
    // Settings UseCases
    factoryOf(::GetUserSettingsUseCase)
    factoryOf(::SaveThemeUseCase)
    factoryOf(::SaveNotificationSettingsUseCase)
    factoryOf(::SaveSoundSettingsUseCase)
    factoryOf(::SaveVibrationSettingsUseCase)
    factoryOf(::UpdateUserProfileUseCase)
}

val storageModule = module {
    single { Settings() }
    single { LocalCookieStorage(get()) }
}

val networkModule = module {
    single {
        HttpClient {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.HEADERS
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
            install(SSE) {
                showRetryEvents()
                showCommentEvents()
            }
        }
    }
}

val datasourceModule = module {
    factoryOf(::AuthRemoteDatasource)
    factoryOf(::UserRemoteDatasource)
    factoryOf(::CharacterRemoteDatasource)
    factoryOf(::ContactRemoteDatasource)
    factoryOf(::ChatRemoteDatasource)
    factoryOf(::ProfileRemoteDataSource)
}

val repositoryModule = module {
    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
    single<CharacterRepository> { CharacterRepositoryImpl(get()) }
    single<ContactRepository> { ContactRepositoryImpl(get()) }
    single<ChatRepository> { ChatRepositoryImpl(get()) }
    single<ContactProvider> { ContactProviderImpl(get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get()) }
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
        themeModule,
        commonModule,
        networkModule,
        datasourceModule,
        repositoryModule,
        viewModelModule,
        storageModule
    )