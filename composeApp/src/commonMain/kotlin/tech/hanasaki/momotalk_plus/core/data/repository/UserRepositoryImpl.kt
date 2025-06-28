package tech.hanasaki.momotalk_plus.core.data.repository

import kotlinx.coroutines.flow.Flow
import tech.hanasaki.momotalk_plus.core.common.AppError
import tech.hanasaki.momotalk_plus.core.common.Result
import tech.hanasaki.momotalk_plus.core.data.datasource.local.AuthSettings
import tech.hanasaki.momotalk_plus.core.data.datasource.remote.UserRemoteDatasource
import tech.hanasaki.momotalk_plus.core.domain.model.RefreshInfo
import tech.hanasaki.momotalk_plus.core.domain.model.User
import tech.hanasaki.momotalk_plus.core.domain.model.UserError
import tech.hanasaki.momotalk_plus.core.domain.repository.UserRepository

class UserRepositoryImpl(
    private val userRemoteDatasource: UserRemoteDatasource,
    private val authSettings: AuthSettings,
) : UserRepository {
    override suspend fun refreshIdToken(): Result<RefreshInfo, UserError> {
        val refreshToken = authSettings.getRefreshToken()
            ?: return Result.Error(UserError.ApiError(code = -1, message = "No refresh token found"))
        return userRemoteDatasource.refreshIdToken(refreshToken).fold(
            onSuccess = { response ->
                Result.Success(
                    RefreshInfo(
                        uid = response.userId,
                        idToken = response.idToken,
                        refreshToken = response.refreshToken,
                        expiresIn = response.expiresIn,
                    )
                )
            },
            onError = { error ->
                Result.Error(error)
            }
        )
    }

    override suspend fun getCurrentUser(idToken: String): Result<User?, UserError> =
        userRemoteDatasource.getAccountInfo(idToken).fold(
            onSuccess = { accounts ->
                val currentUser = accounts.users.firstOrNull()
                Result.Success(
                    User(
                        uid = currentUser?.localId ?: "",
                        email = currentUser?.email ?: "",
                        name = currentUser?.displayName ?: "",
                        avatar = currentUser?.photoUrl ?: "",
                    )
                )
            },
            onError = { error ->
                Result.Error(error)
            }
        )

    override suspend fun updateUser(user: User): Boolean =
        userRemoteDatasource.setAccountInfo(idToken = user.uid, displayName = user.name, photoUrl = user.avatar).fold(
            onSuccess = { true },
            onError = { false }
        )

    override suspend fun deleteUser(uid: String): Boolean =
        userRemoteDatasource.deleteAccount(uid).fold(
            onSuccess = { true },
            onError = { false }
        )

    override suspend fun logout(): Result<Unit, UserError> {
        authSettings.clear()
        return Result.Success(Unit)
    }

    override fun getLoginState(): Flow<String?> =
        authSettings.getLoggedInUserUidFlow()

    override suspend fun saveLoginState(
        uid: String,
        idToken: String,
        refreshToken: String,
        expiresIn: Long,
    ): Result<Unit, AppError> =
        try {
            authSettings.saveAuthTokens(uid, idToken, refreshToken, expiresIn)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(AppError("Failed to save login state: ${e.message}"))
        }
}