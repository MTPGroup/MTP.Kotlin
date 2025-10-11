package tech.hanasaki.momotalk_plus.db

import androidx.room.*
import tech.hanasaki.momotalk_plus.core.data.datasource.local.dao.*
import tech.hanasaki.momotalk_plus.core.data.datasource.local.entity.*
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.local.dao.ChatDao
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.local.dao.MessageDao
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.local.entity.ChatEntity
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.local.entity.MessageEntity
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.local.dao.ContactDao
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.local.entity.ContactEntity


@Database(
    entities = [
        UserEntity::class,
        CharacterEntity::class,
        CookieEntity::class,
        SessionEntity::class,
        ContactEntity::class,
        ChatEntity::class,
        MessageEntity::class,
        SettingsEntity::class,
    ],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 2, to = 3)
    ]
)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun characterDao(): CharacterDao
    abstract fun sessionDao(): SessionDao
    abstract fun cookieDao(): CookieDao
    abstract fun contactDao(): ContactDao
    abstract fun chatDao(): ChatDao
    abstract fun messageDao(): MessageDao
    abstract fun settingsDao(): SettingsDao
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
