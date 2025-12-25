package tech.hanasaki.momotalk_plus.app.di

import org.koin.dsl.module
import tech.hanasaki.momotalk_plus.db.AppDatabase

actual val platformModule = module {
    single<AppDatabase> {
        AppDatabase()
    }
}
