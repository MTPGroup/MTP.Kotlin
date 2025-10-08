package tech.hanasaki.momotalk_plus.core.utils

import androidx.compose.runtime.Composable

@Composable
actual fun rememberPlatformContext(): Any {
    return Unit // iOS doesn't need context
}

