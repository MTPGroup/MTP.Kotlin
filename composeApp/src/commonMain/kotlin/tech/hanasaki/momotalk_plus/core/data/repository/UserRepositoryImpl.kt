package tech.hanasaki.momotalk_plus.core.data.repository

import LocalCookieStorage
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.UserRemoteDatasource
import tech.hanasaki.momotalk_plus.core.data.model.UserProfile
import tech.hanasaki.momotalk_plus.core.domain.model.User
import tech.hanasaki.momotalk_plus.core.domain.model.UserError
import tech.hanasaki.momotalk_plus.core.domain.repository.UserRepository

class UserRepositoryImpl(
    private val userRemoteDatasource: UserRemoteDatasource,
    private val localCookieStorage: LocalCookieStorage,
) : UserRepository {
    override suspend fun getCurrentUser(): Result<UserProfile?, UserError> {
        return when (val sessionInfo = userRemoteDatasource.getSessionInfo()) {
            is Result.Success -> {
                Result.Success(
                    sessionInfo.data?.user
                )
            }

            is Result.Error -> {
                Result.Error(UserError.Unknown)
            }
        }
    }

    override suspend fun updateUser(user: User): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deleteUser(uid: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun logout(): Result<Unit, UserError> {
        userRemoteDatasource.logout()
        localCookieStorage.removeCookie("better-auth.session_token")
        return Result.Success(Unit)
    }

    override suspend fun getLoginState(): Boolean {
        return when (userRemoteDatasource.getSessionInfo()) {
            is Result.Success -> true
            is Result.Error -> false
        }
    }
}