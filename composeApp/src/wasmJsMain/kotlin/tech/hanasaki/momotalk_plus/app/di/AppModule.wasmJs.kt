package tech.hanasaki.momotalk_plus.app.di

import org.koin.core.scope.Scope
import tech.hanasaki.momotalk_plus.core.data.datasource.local.db.DatabaseDriverFactory

actual fun Scope.createDatabaseDriverFactory(): DatabaseDriverFactory {
    TODO("Not yet implemented")
}