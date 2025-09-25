package tech.hanasaki.momotalk_plus.core.data.repository

import LocalCookieStorage
import tech.hanasaki.momotalk_plus.core.common.IResult
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.UserRemoteDatasource
import tech.hanasaki.momotalk_plus.core.data.model.UserProfile
import tech.hanasaki.momotalk_plus.core.domain.model.User
import tech.hanasaki.momotalk_plus.core.domain.model.UserError
import tech.hanasaki.momotalk_plus.core.domain.repository.UserRepository

class UserRepositoryImpl(
    private val userRemoteDatasource: UserRemoteDatasource,
    private val localCookieStorage: LocalCookieStorage,
) : UserRepository {
    override suspend fun getCurrentUser(): IResult<UserProfile?, UserError> {
        return when (val sessionInfo = userRemoteDatasource.getSessionInfo()) {
            is IResult.Success -> {
                IResult.Success(
                    sessionInfo.data?.user
                )
            }

            is IResult.Error -> {
                IResult.Error(UserError.Unknown)
            }
        }
    }

    override suspend fun updateUser(user: User): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deleteUser(uid: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun logout(): IResult<Unit, UserError> {
        userRemoteDatasource.logout()
        localCookieStorage.removeCookie("better-auth.session_token")
        return IResult.Success(Unit)
    }

    override suspend fun getLoginState(): Boolean {
        return when (userRemoteDatasource.getSessionInfo()) {
            is IResult.Success -> true
            is IResult.Error -> false
        }
    }
}