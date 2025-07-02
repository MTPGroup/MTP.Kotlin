package tech.hanasaki.momotalk_plus.core.data.repository

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.core.data.datasource.local.TokenStorage
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.UserRemoteDatasource
import tech.hanasaki.momotalk_plus.core.data.model.UserProfile
import tech.hanasaki.momotalk_plus.core.domain.model.RefreshInfo
import tech.hanasaki.momotalk_plus.core.domain.model.User
import tech.hanasaki.momotalk_plus.core.domain.model.UserError
import tech.hanasaki.momotalk_plus.core.domain.repository.UserRepository

class UserRepositoryImpl(
    private val userRemoteDatasource: UserRemoteDatasource,
    private val tokenStorage: TokenStorage,
) : UserRepository {
    override suspend fun refreshIdToken(): Result<RefreshInfo, UserError> {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrentUser(idToken: String): Result<UserProfile, UserError> =
        userRemoteDatasource.getUserInfo(idToken)

    override suspend fun updateUser(user: User): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deleteUser(uid: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun logout(): Result<Unit, UserError> {
        tokenStorage.clear()
        return Result.Success(Unit)
    }

    override fun getLoginState(): Flow<String?> {
        return tokenStorage.getAccessTokenFlow()
    }

    override suspend fun saveLoginState(
        uid: String,
        idToken: String,
        refreshToken: String,
        expiresIn: Long
    ): Result<Unit, AppError> {
        TODO("Not yet implemented")
    }

}