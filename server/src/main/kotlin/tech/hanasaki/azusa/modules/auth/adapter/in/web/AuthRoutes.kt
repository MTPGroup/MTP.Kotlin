package tech.hanasaki.azusa.modules.auth.adapter.`in`.web

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import tech.hanasaki.azusa.modules.auth.adapter.`in`.web.dto.*
import tech.hanasaki.azusa.modules.auth.adapter.`in`.web.mapper.toLoginResponse
import tech.hanasaki.azusa.modules.auth.adapter.`in`.web.mapper.toUserProfile
import tech.hanasaki.azusa.modules.auth.application.port.`in`.AuthUseCasePort
import tech.hanasaki.azusa.modules.auth.application.port.`in`.OtpUseCasePort
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import tech.hanasaki.azusa.modules.auth.domain.model.PlainPassword
import tech.hanasaki.azusa.modules.auth.domain.model.Username
import tech.hanasaki.azusa.shared.domain.model.vo.Email
import tech.hanasaki.azusa.shared.infrastructure.web.response.respondOk
import tech.hanasaki.azusa.shared.infrastructure.web.route.requireUserId
import tech.hanasaki.azusa.shared.infrastructure.web.validation.validateEmail
import tech.hanasaki.azusa.shared.infrastructure.web.validation.validatePassword
import tech.hanasaki.azusa.shared.infrastructure.web.validation.validateUsername

fun Route.authRoutes() {
    val authUseCase: AuthUseCasePort by inject()
    val otpService: OtpUseCasePort by inject()

    route("/auth") {
        post("/sign-up/email") {
            val request = call.receive<SignUpRequest>()

            validateEmail(request.email)
            validatePassword(request.password)
            validateUsername(request.name)

            authUseCase.register(
                email = Email(request.email),
                password = PlainPassword(request.password),
                username = Username(request.name)
            )

            call.respondOk(
                SuccessResponse(),
                "注册成功",
                HttpStatusCode.Created,
            )
        }

        post("/sign-in/email") {
            val request = call.receive<SignInWithPasswordRequest>()

            validateEmail(request.email)

            val result = authUseCase.login(
                email = Email(request.email),
                password = PlainPassword(request.password)
            )
            call.respondOk(result.toLoginResponse(), "登录成功")
        }

        post("/email-otp/send") {
            val request = call.receive<SendOtpRequest>()

            validateEmail(request.email)

            otpService.generate(Email(request.email), OtpType.fromString(request.type))
            call.respondOk(SuccessResponse(), "验证码已发送")
        }

        post("/email-otp/verify-email") {
            val request = call.receive<VerifyOTPRequest>()
            validateEmail(request.email)

            val email = Email(request.email)
            otpService.verify(
                email,
                OtpType.VERIFY_EMAIL,
                request.otp
            )
            authUseCase.verifyEmail(email)
            call.respondOk(SuccessResponse(), "邮箱验证成功")
        }

        post("/email-otp/reset-password") {
            val request = call.receive<ResetPasswordRequest>()

            validateEmail(request.email)
            validatePassword(request.password)

            val email = Email(request.email)
            otpService.verify(
                email,
                OtpType.RESET_PASSWORD,
                request.otp
            )
            authUseCase.resetPassword(
                email,
                PlainPassword(request.password)
            )
            call.respondOk(SuccessResponse(), "密码重置成功")
        }

        post("/refresh") {
            val request = call.receive<RefreshTokenRequest>()
            val result = authUseCase.refreshToken(request.refreshToken)
            call.respondOk(result.toLoginResponse(), "令牌刷新成功")
        }

        post("/sign-out") {
            val request = runCatching { call.receive<RefreshTokenRequest>() }.getOrNull()
            if (request != null) {
                authUseCase.logout(request.refreshToken)
            }
            call.respond(HttpStatusCode.NoContent)
        }

        authenticate("auth-jwt") {
            get("/me") {
                val userId = call.requireUserId()
                val user = authUseCase.getProfile(userId)
                call.respondOk(user.toUserProfile())
            }

            post("/password/change") {
                val userId = call.requireUserId()
                val request = call.receive<ChangePasswordRequest>()

                validatePassword(request.newPassword)

                authUseCase.changePassword(
                    userId,
                    PlainPassword(request.oldPassword),
                    PlainPassword(request.newPassword),
                )
                call.respondOk(SuccessResponse(), "密码修改成功")
            }

            delete("/account") {
                val userId = call.requireUserId()
                authUseCase.deleteAccount(userId)
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
