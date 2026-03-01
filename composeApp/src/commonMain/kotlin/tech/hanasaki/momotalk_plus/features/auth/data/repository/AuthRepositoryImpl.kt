package tech.hanasaki.momotalk_plus.features.auth.data.repository

import tech.hanasaki.momotalk_plus.core.auth.AuthTokens
import tech.hanasaki.momotalk_plus.core.auth.TokenStore
import tech.hanasaki.momotalk_plus.core.network.AppErrorException
import tech.hanasaki.momotalk_plus.core.network.AppResult
import tech.hanasaki.momotalk_plus.core.network.NetworkErrorMapper
import tech.hanasaki.momotalk_plus.core.network.callApi
import tech.hanasaki.momotalk_plus.features.auth.data.datasource.remote.*
import tech.hanasaki.momotalk_plus.features.auth.domain.model.OTPType
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val remote: AuthRemoteDataSource,
    private val tokenStore: TokenStore,
    private val errorMapper: NetworkErrorMapper,
) : AuthRepository {

    override suspend fun signUp(email: String, password: String) {
        val result = callApi(errorMapper) {
            remote.signUp(
                SignUpRequest(
                    email = email,
                    name = email.substringBefore('@'),
                    password = password,
                )
            )
        }
        result.throwIfFailure()
    }

    override suspend fun signInWithPassword(email: String, password: String) {
        val result = callApi(errorMapper) {
            remote.signIn(
                SignInWithPasswordRequest(
                    email = email,
                    password = password,
                )
            )
        }

        when (result) {
            is AppResult.Success -> {
                val tokens = result.data.tokens
                tokenStore.save(
                    AuthTokens(
                        accessToken = tokens.accessToken,
                        refreshToken = tokens.refreshToken,
                    )
                )
            }

            is AppResult.Failure -> result.throwAsException()
        }
    }

    override suspend fun signOut() {
        val refreshToken = tokenStore.get()?.refreshToken
        runCatching { remote.signOut(refreshToken) }
        tokenStore.clear()
    }

    override suspend fun sendEmailVerification(email: String, type: OTPType) {
        val otpType = when (type) {
            OTPType.SIGN_IN -> "sign_in"
            OTPType.VERIFY_EMAIL -> "verify_email"
            OTPType.RESET_PASSWORD -> "reset_password"
        }

        val result = callApi(errorMapper) {
            remote.sendOtp(
                SendOtpRequest(
                    email = email,
                    type = otpType,
                )
            )
        }
        result.throwIfFailure()
    }

    override suspend fun verifyEmail(type: OTPType, email: String, otp: String) {
        val result = callApi(errorMapper) {
            remote.verifyEmail(
                VerifyOTPRequest(
                    email = email,
                    otp = otp,
                )
            )
        }
        result.throwIfFailure()
    }

    override suspend fun sendResetPasswordEmail(email: String) {
        val result = callApi(errorMapper) {
            remote.sendOtp(
                SendOtpRequest(
                    email = email,
                    type = "reset_password",
                )
            )
        }
        result.throwIfFailure()
    }

    override suspend fun resetPassword(email: String, otp: String, password: String) {
        val result = callApi(errorMapper) {
            remote.resetPassword(
                ResetPasswordRequest(
                    email = email,
                    otp = otp,
                    password = password,
                )
            )
        }
        result.throwIfFailure()
    }
}

private fun AppResult<*>.throwIfFailure() {
    if (this is AppResult.Failure) throw AppErrorException(error)
}

private fun AppResult.Failure.throwAsException(): Nothing = throw AppErrorException(error)
