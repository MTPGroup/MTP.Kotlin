package tech.hanasaki.momotalk_plus.core.data.datasource.local

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import tech.hanasaki.momotalk_plus.core.data.datasource.mapper.SessionMapper.toSession
import tech.hanasaki.momotalk_plus.core.data.datasource.mapper.SessionMapper.toSessionEntity
import tech.hanasaki.momotalk_plus.core.data.datasource.mapper.UserMapper.toUser
import tech.hanasaki.momotalk_plus.core.data.datasource.mapper.UserMapper.toUserEntity
import tech.hanasaki.momotalk_plus.core.domain.model.Session
import tech.hanasaki.momotalk_plus.core.domain.model.User
import tech.hanasaki.momotalk_plus.db.AppDatabase

/**
 * 本地会话数据源 - 使用 SQLDelight 存储会话数据
 */
class LocalSessionDataSource(
    db: AppDatabase,
) {
    private val sessionDao = db.sessionDao()
    private val userDao = db.userDao()

    /**
     * 获取当前会话和用户信息
     */
    suspend fun getCurrentSessionWithUser(): Pair<Session, User?>? {
        sessionDao.getCurrentSession()?.let { session ->
            val user = userDao.getUserById(session.userId)
            return Pair(session.toSession(), user?.toUser())
        }
        return null
    }

    /**
     * 观察当前会话变化
     */
    fun observeCurrentSession(): Flow<Session?> =
        sessionDao.getCurrentSessionAsFlow().map { it?.toSession() }


    /**
     * 保存当前会话及其用户信息
     */
    suspend fun saveSessionWithUser(session: Session, user: User) {
        sessionDao.upsert(session.toSessionEntity())
        userDao.upsert(user.toUserEntity())
    }


    /**
     * 删除所有会话
     */
    suspend fun deleteAllSessions() =
        sessionDao.deleteAllSession()
}
