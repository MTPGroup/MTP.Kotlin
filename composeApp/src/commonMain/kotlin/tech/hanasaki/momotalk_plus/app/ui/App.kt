package tech.hanasaki.momotalk_plus.app.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import tech.hanasaki.momotalk_plus.core.theme.AppThemeProvider
import tech.hanasaki.momotalk_plus.core.theme.ThemeManager

@Preview
@Composable
fun App(
    themeManager: ThemeManager = koinInject(),
) {
    val currentTheme by themeManager.currentTheme.collectAsState()

    AppThemeProvider(theme = currentTheme) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavigation()
        }
    }
}