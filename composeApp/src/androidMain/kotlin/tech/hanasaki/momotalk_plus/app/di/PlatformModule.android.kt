package tech.hanasaki.momotalk_plus.app.di

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import tech.hanasaki.momotalk_plus.db.AppDatabase

actual val platformModule = module {
    single<AppDatabase> {
        val appContext = androidContext()
        val dbFile = appContext.getDatabasePath("test.db")
        Room.databaseBuilder<AppDatabase>(
            context = appContext,
            name = dbFile.absolutePath
        )
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .build()
    }
}