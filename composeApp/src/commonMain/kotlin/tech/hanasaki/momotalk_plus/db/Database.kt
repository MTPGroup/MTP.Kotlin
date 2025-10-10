package tech.hanasaki.momotalk_plus.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import tech.hanasaki.momotalk_plus.core.data.datasource.local.dao.CharacterDao
import tech.hanasaki.momotalk_plus.core.data.datasource.local.dao.CookieDao
import tech.hanasaki.momotalk_plus.core.data.datasource.local.dao.SessionDao
import tech.hanasaki.momotalk_plus.core.data.datasource.local.dao.UserDao
import tech.hanasaki.momotalk_plus.core.data.datasource.local.entity.CharacterEntity
import tech.hanasaki.momotalk_plus.core.data.datasource.local.entity.CookieEntity
import tech.hanasaki.momotalk_plus.core.data.datasource.local.entity.SessionEntity
import tech.hanasaki.momotalk_plus.core.data.datasource.local.entity.UserEntity
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
    ],
    version = 2,
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
}

@Suppress("KotlinNoActualForExpect")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}
