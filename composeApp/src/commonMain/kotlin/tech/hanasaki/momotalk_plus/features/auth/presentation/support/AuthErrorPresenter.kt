package tech.hanasaki.momotalk_plus.features.auth.presentation.support

import org.jetbrains.compose.resources.StringResource

object AuthErrorPresenter {
    fun resolveMessage(
        throwable: Throwable?,
        fallback: StringResource,
    ): AuthUiText {
        val backendMessage = throwable?.message?.takeIf { it.isNotBlank() }
        return if (backendMessage != null) {
            AuthUiText.Dynamic(backendMessage)
        } else {
            AuthUiText.Resource(fallback)
        }
    }
}
