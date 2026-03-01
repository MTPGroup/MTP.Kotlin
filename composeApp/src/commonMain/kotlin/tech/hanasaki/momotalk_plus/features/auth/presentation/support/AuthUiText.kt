package tech.hanasaki.momotalk_plus.features.auth.presentation.support

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

sealed interface AuthUiText {
    data class Resource(
        val key: StringResource,
        val args: List<Any> = emptyList(),
    ) : AuthUiText

    data class Dynamic(
        val value: String,
    ) : AuthUiText
}

@Composable
fun AuthUiText.asString(): String = when (this) {
    is AuthUiText.Dynamic -> value
    is AuthUiText.Resource -> stringResource(key, *args.toTypedArray())
}

suspend fun AuthUiText.resolve(): String = when (this) {
    is AuthUiText.Dynamic -> value
    is AuthUiText.Resource -> getString(key, *args.toTypedArray())
}
