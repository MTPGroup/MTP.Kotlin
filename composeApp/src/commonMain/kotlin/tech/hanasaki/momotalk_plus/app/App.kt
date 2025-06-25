package tech.hanasaki.momotalk_plus.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import tech.hanasaki.momotalk_plus.features.login.presentation.ui.LoginScreen

@Preview
@Composable
fun App() {
    MaterialTheme {
        LoginScreen(onLoginSuccess = {})
    }
}