package tech.hanasaki.momotalk_plus.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import tech.hanasaki.momotalk_plus.app.ui.theme.AppTypography

val LocalAppTheme = staticCompositionLocalOf { PredefinedThemes.Default }

@Composable
fun AppThemeProvider(
    theme: AppTheme = PredefinedThemes.Default,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = theme.colorScheme,
        typography = AppTypography, // 使用现有的排版配置
        content = {
            CompositionLocalProvider(LocalAppTheme provides theme) {
                content()
            }
        }
    )
}

// 扩展属性方便访问自定义颜色
val MaterialTheme.appTheme: AppTheme
    @Composable
    get() = LocalAppTheme.current