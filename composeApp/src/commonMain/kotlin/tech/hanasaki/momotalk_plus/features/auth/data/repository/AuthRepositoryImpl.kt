package tech.hanasaki.momotalk_plus.features.auth.data.repository

import tech.hanasaki.momotalk_plus.features.auth.data.datasource.remote.api.AuthApi
import tech.hanasaki.momotalk_plus.features.auth.data.datasource.remote.dto.*
import tech.hanasaki.momotalk_plus.features.auth.domain.model.OTPType
import tech.hanasaki.momotalk_plus.features.auth.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authApi: AuthApi,
) : AuthRepository {
    override suspend fun signUp(email: String, username: String, password: String) {
        authApi.signUp(SignUpRequest(email, username, password, ""))
    }

    override suspend fun signInWithPassword(
        email: String,
        password: String,
    ): SignInWithPasswordResponse =
        authApi.signIn(
            SignInWithPasswordRequest(
                email,
                password,
            )
        )

    override suspend fun signOut() =
        authApi.signOut()

    override suspend fun sendEmailVerification(
        email: String,
        type: OTPType,
    ) {
        authApi.sendEmailVerification(
            SendEmailVerificationRequest(
                email,
                type
            )
        )
    }

    override suspend fun sendPasswordResetEmail(email: String) {
        authApi.sendForgetPasswordEmail(SendPasswordResetEmailRequest(email))
    }

    override suspend fun verifyEmail(email: String, otp: String) {
        authApi.verifyEmail(VerifyOTPRequest(email, otp))
    }

    override suspend fun resetPassword(email: String, otp: String, password: String) {
        authApi.resetPassword(ResetPasswordRequest(email, otp, password))
    }
}
