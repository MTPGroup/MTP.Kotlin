package tech.hanasaki.momotalk_plus.features.auth.data.datasource.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import tech.hanasaki.momotalk_plus.core.network.ApiEnvelope

class AuthRemoteDataSource(
    private val client: HttpClient,
) {
    suspend fun signUp(request: SignUpRequest): ApiEnvelope<SuccessResponse> =
        client.post("auth/sign-up/email") {
            setBody(request)
        }.body()

    suspend fun signIn(request: SignInWithPasswordRequest): ApiEnvelope<LoginResponse> =
        client.post("auth/sign-in/email") {
            setBody(request)
        }.body()

    suspend fun sendOtp(request: SendOtpRequest): ApiEnvelope<SuccessResponse> =
        client.post("auth/email-otp/send") {
            setBody(request)
        }.body()

    suspend fun verifyEmail(request: VerifyOTPRequest): ApiEnvelope<SuccessResponse> =
        client.post("auth/email-otp/verify-email") {
            setBody(request)
        }.body()

    suspend fun resetPassword(request: ResetPasswordRequest): ApiEnvelope<SuccessResponse> =
        client.post("auth/email-otp/reset-password") {
            setBody(request)
        }.body()

    suspend fun signOut(refreshToken: String?) {
        client.post("auth/sign-out") {
            if (refreshToken != null) {
                setBody(RefreshTokenRequest(refreshToken))
            }
        }
    }
}
