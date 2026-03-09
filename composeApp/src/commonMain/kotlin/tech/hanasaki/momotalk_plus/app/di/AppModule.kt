package tech.hanasaki.momotalk_plus.app.di

import io.ktor.client.*
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import tech.hanasaki.momotalk_plus.app.viewmodel.AppViewModel
import tech.hanasaki.momotalk_plus.core.auth.PersistentTokenStore
import tech.hanasaki.momotalk_plus.core.auth.TokenStore
import tech.hanasaki.momotalk_plus.core.data.datasource.local.CharacterLocalDataSource
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.CharacterRemoteDataSource
import tech.hanasaki.momotalk_plus.core.data.datasource.local.LocalCookieStorage
import tech.hanasaki.momotalk_plus.core.data.repository.CharacterRepositoryImpl
import tech.hanasaki.momotalk_plus.core.data.repository.SessionRepositoryImpl
import tech.hanasaki.momotalk_plus.core.data.repository.UploadImageRepositoryImpl
import tech.hanasaki.momotalk_plus.core.domain.repository.CharacterRepository
import tech.hanasaki.momotalk_plus.core.domain.repository.ContactProvider
import tech.hanasaki.momotalk_plus.core.domain.repository.SessionRepository
import tech.hanasaki.momotalk_plus.core.domain.repository.UploadImageRepository
import tech.hanasaki.momotalk_plus.core.domain.usecase.*
import tech.hanasaki.momotalk_plus.core.network.NetworkErrorMapper
import tech.hanasaki.momotalk_plus.core.network.createHttpClient
import tech.hanasaki.momotalk_plus.core.theme.ThemeManager
import tech.hanasaki.momotalk_plus.features.auth.data.datasource.remote.AuthRemoteDataSource
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
import tech.hanasaki.momotalk_plus.features.contacts.data.repository.ContactProviderImpl
import tech.hanasaki.momotalk_plus.features.contacts.data.repository.ContactRepositoryImpl
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.remote.ContactRemoteDatasource
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
import tech.hanasaki.momotalk_plus.features.profile.domain.usecase.UploadAvatarUseCase
import tech.hanasaki.momotalk_plus.features.profile.presentation.viewmodel.ProfileViewModel
import tech.hanasaki.momotalk_plus.features.settings.data.datasource.remote.SettingsRemoteDataSource
import tech.hanasaki.momotalk_plus.features.settings.data.repository.SettingsRepositoryImpl
import tech.hanasaki.momotalk_plus.features.settings.presentation.viewmodel.SettingsViewModel
import kotlin.time.ExperimentalTime
import tech.hanasaki.momotalk_plus.features.settings.domain.repository.SettingsRepository as FeatureSettingsRepository
import tech.hanasaki.momotalk_plus.features.settings.domain.usecase.ObserveSettingsUseCase as ObserveFeatureSettingsUseCase
import tech.hanasaki.momotalk_plus.features.settings.domain.usecase.SaveNotificationsUseCase as SaveFeatureNotificationsUseCase
import tech.hanasaki.momotalk_plus.features.settings.domain.usecase.SaveSoundUseCase as SaveFeatureSoundUseCase
import tech.hanasaki.momotalk_plus.features.settings.domain.usecase.SaveThemeUseCase as SaveFeatureThemeUseCase
import tech.hanasaki.momotalk_plus.features.settings.domain.usecase.SaveVibrationUseCase as SaveFeatureVibrationUseCase

expect val platformModule: Module

val storageModule = module {
    single { LocalCookieStorage(get()) }
}

@OptIn(ExperimentalTime::class)
val networkModule = module {
    single {
        Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        }
    }

    single<TokenStore> { PersistentTokenStore() }

    single { NetworkErrorMapper(get()) }

    single<HttpClient> {
        createHttpClient(
            tokenStore = get(),
            json = get(),
        )
    }
}

val themeModule = module {
    single { ThemeManager() }
}

val uploadModule = module {
    single<UploadImageRepository> { UploadImageRepositoryImpl(get()) }

    factoryOf(::UploadImageUseCase)
}
val characterModule = module {
    factoryOf(::CharacterRemoteDataSource)
    single<CharacterRepository> { CharacterRepositoryImpl(get(), get(), get()) }

    factoryOf(::CharacterDetailUseCase)
    factoryOf(::ListCharacterUseCase)
    factoryOf(::UpdateCharacterUseCase)

}

val sessionModule = module {
    single<SessionRepository> { SessionRepositoryImpl(get(), get(), get()) }

    factoryOf(::ObserveCurrentUserUseCase)
    factoryOf(::ObserveLoginStateUseCase)
    factoryOf(::RefreshCurrentUserUseCase)
    factoryOf(::LogoutUseCase)
}

val authModule = module {
    single { AuthRemoteDataSource(get()) }
    single<AuthRepository> { AuthRepositoryImpl(get(), get(), get()) }

    factoryOf(::SignInUserUseCase)
    factoryOf(::SignUpUserUseCase)
    factoryOf(::SignOutUserUseCase)
    factoryOf(::SendEmailVerificationUseCase)
    factoryOf(::SendPasswordResetEmailUseCase)
    factoryOf(::VerifyEmailUseCase)
    factoryOf(::ResetPasswordUseCase)
}

val contactModule = module {
    factoryOf(::ContactRemoteDatasource)
    single<ContactRepository> { ContactRepositoryImpl(get(), get()) }
    single<ContactProvider> { ContactProviderImpl(get()) }

    factoryOf(::AddContactUseCase)
    factoryOf(::DeleteContactUseCase)
    factoryOf(::ListContactUseCase)
}

val chatModule = module {
    factoryOf(::ChatRemoteDatasource)
    single<ChatRepository> { ChatRepositoryImpl(get(), get()) }

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
    single { ProfileRemoteDataSource(get()) }
    single<ProfileRepository> { ProfileRepositoryImpl(get(), get()) }

    factoryOf(::UpdateUserProfileUseCase)
    factoryOf(::UploadAvatarUseCase)
}

val settingsModule = module {
    single { SettingsRemoteDataSource(get()) }
    single<FeatureSettingsRepository> { SettingsRepositoryImpl(get(), get(), get()) }

    factoryOf(::ObserveFeatureSettingsUseCase)
    factoryOf(::SaveFeatureNotificationsUseCase)
    factoryOf(::SaveFeatureSoundUseCase)
    factoryOf(::SaveFeatureThemeUseCase)
    factoryOf(::SaveFeatureVibrationUseCase)
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
        settingsModule,
        viewModelModule
    )
