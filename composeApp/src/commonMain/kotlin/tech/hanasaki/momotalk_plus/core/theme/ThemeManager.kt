package tech.hanasaki.momotalk_plus.core.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeManager {
    private val _currentTheme = MutableStateFlow(PredefinedThemes.Default)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
    }

    fun setThemeById(themeId: String) {
        val theme = PredefinedThemes.allThemes.find { it.id == themeId }
            ?: PredefinedThemes.Default
        setTheme(theme)
    }

    fun getAvailableThemes(): List<AppTheme> {
        return PredefinedThemes.allThemes
    }
}

