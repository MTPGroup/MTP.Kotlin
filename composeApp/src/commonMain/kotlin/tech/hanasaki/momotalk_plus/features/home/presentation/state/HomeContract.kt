package tech.hanasaki.momotalk_plus.features.home.presentation.state

import kotlinx.serialization.Serializable

@Serializable
sealed interface HomeTab {
    @Serializable
    data object Chats : HomeTab

    @Serializable
    data object Contacts : HomeTab
}

data class HomeState(
    val currentTab: HomeTab = HomeTab.Chats,
    val showBottomBar: Boolean = true,
)

sealed class HomeIntent {
    data object SettingsClicked : HomeIntent()
    data object LogoutClicked : HomeIntent()
    data object ProfileClicked : HomeIntent()
    data class TabSelected(val tab: HomeTab) : HomeIntent()
    data class SetBottomBarVisibility(val visible: Boolean) : HomeIntent()
}

sealed class HomeSideEffect {
    data object NavigateToSettings : HomeSideEffect()
    data object NavigateToLogin : HomeSideEffect()
    data object NavigateToProfile : HomeSideEffect()
}