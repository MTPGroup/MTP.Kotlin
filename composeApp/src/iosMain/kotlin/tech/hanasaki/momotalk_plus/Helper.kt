package tech.hanasaki.momotalk_plus

import org.koin.core.context.startKoin
import tech.hanasaki.momotalk_plus.app.di.appModule

fun initKoin() {
    startKoin {
        modules(appModule)
    }
}