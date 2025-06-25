package tech.hanasaki.momotalk_plus

import android.app.Application
import org.koin.core.context.startKoin
import tech.hanasaki.momotalk_plus.app.di.appModule
import org.koin.android.ext.koin.androidLogger
import org.koin.android.ext.koin.androidContext

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(
                appModule
            )
        }
    }
}