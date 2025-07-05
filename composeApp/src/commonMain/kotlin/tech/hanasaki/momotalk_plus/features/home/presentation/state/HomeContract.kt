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
)

sealed class HomeIntent {
    data object LogoutClicked : HomeIntent()
    data object NewChatClicked : HomeIntent()
    data object ProfileClicked : HomeIntent()
    data class TabSelected(val tab: HomeTab) : HomeIntent()
}

sealed class HomeSideEffect {
    data object NavigateToLogin : HomeSideEffect()
    data object NavigateToNewChat : HomeSideEffect()
    data object NavigateToProfile : HomeSideEffect()
}