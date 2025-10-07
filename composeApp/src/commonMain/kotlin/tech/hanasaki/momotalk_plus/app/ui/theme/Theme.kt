// app/ui/theme/Theme.kt
package tech.hanasaki.momotalk_plus.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import tech.hanasaki.momotalk_plus.core.theme.AppThemeProvider
import tech.hanasaki.momotalk_plus.core.theme.PredefinedThemes

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val theme = if (darkTheme) {
        PredefinedThemes.Dark
    } else {
        PredefinedThemes.Default
    }

    AppThemeProvider(
        theme = theme,
        content = content
    )
}