package tech.hanasaki.momotalk_plus

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.koin.core.context.startKoin
import tech.hanasaki.momotalk_plus.app.App
import tech.hanasaki.momotalk_plus.app.di.appModule

fun main() {
    startKoin {
        modules(appModule)
    }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "MomoTalkPlus",
        ) {
            App()
        }
    }
}