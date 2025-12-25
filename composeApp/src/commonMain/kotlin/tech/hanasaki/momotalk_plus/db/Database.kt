package tech.hanasaki.momotalk_plus.db

import tech.hanasaki.momotalk_plus.core.data.datasource.local.dao.CharacterDao
import tech.hanasaki.momotalk_plus.core.data.datasource.local.dao.CookieDao
import tech.hanasaki.momotalk_plus.core.data.datasource.local.dao.SettingsDao
import tech.hanasaki.momotalk_plus.core.data.datasource.local.dao.UserDao
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.local.dao.ChatDao
import tech.hanasaki.momotalk_plus.features.chats.data.datasource.local.dao.MessageDao
import tech.hanasaki.momotalk_plus.features.contacts.data.datasource.local.dao.ContactDao

class AppDatabase {
    private val userDaoImpl = UserDao()
    private val characterDaoImpl = CharacterDao()
    private val cookieDaoImpl = CookieDao()
    private val contactDaoImpl = ContactDao()
    private val chatDaoImpl = ChatDao()
    private val messageDaoImpl = MessageDao()
    private val settingsDaoImpl = SettingsDao()

    fun userDao(): UserDao = userDaoImpl
    fun characterDao(): CharacterDao = characterDaoImpl
    fun cookieDao(): CookieDao = cookieDaoImpl
    fun contactDao(): ContactDao = contactDaoImpl
    fun chatDao(): ChatDao = chatDaoImpl
    fun messageDao(): MessageDao = messageDaoImpl
    fun settingsDao(): SettingsDao = settingsDaoImpl
}
