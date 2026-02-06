package tech.hanasaki.azusa.modules.auth.adapter.`in`.web

import io.ktor.http.*
import io.ktor.openapi.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
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
import tech.hanasaki.azusa.shared.infrastructure.web.response.ApiResponse
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
        }.describe {
            tag("认证管理")
            operationId = "registerUser"
            summary = "用户注册"
            description = "使用邮箱、密码和用户名注册新用户"
            requestBody {
                description = "注册信息"
                schema = jsonSchema<SignUpRequest>()
            }
            responses {
                HttpStatusCode.Created {
                    description = "注册成功"
                    schema = jsonSchema<ApiResponse<SuccessResponse>>()
                }
                HttpStatusCode.BadRequest {
                    description = "输入参数无效（如邮箱格式错误、密码太短等）"
                }
                HttpStatusCode.Conflict {
                    description = "邮箱已被注册"
                }
            }
        }

        post("/sign-in/email") {
            val request = call.receive<SignInWithPasswordRequest>()

            validateEmail(request.email)

            val result = authUseCase.login(
                email = Email(request.email),
                password = PlainPassword(request.password)
            )
            call.respondOk(result.toLoginResponse(), "登录成功")
        }.describe {
            tag("认证管理")
            operationId = "loginUser"
            summary = "用户登录"
            description = "使用邮箱和密码登录，获取访问令牌和刷新令牌"
            requestBody {
                description = "登录凭证"
                schema = jsonSchema<SignInWithPasswordRequest>()
            }
            responses {
                HttpStatusCode.OK {
                    description = "登录成功"
                    schema = jsonSchema<ApiResponse<LoginResponse>>()
                }
                HttpStatusCode.BadRequest {
                    description = "输入参数无效"
                }
                HttpStatusCode.Unauthorized {
                    description = "用户名或密码错误"
                }
            }
        }

        post("/email-otp/send") {
            val request = call.receive<SendOtpRequest>()

            validateEmail(request.email)

            otpService.generate(Email(request.email), OtpType.fromString(request.type))
            call.respondOk(SuccessResponse(), "验证码已发送")
        }.describe {
            tag("认证管理")
            operationId = "sendOtp"
            summary = "发送邮箱验证码"
            description = "向指定邮箱发送验证码，用于验证邮箱或重置密码"
            requestBody {
                description = "发送请求信息"
                schema = jsonSchema<SendOtpRequest>()
            }
            responses {
                HttpStatusCode.OK {
                    description = "验证码发送成功"
                    schema = jsonSchema<ApiResponse<SuccessResponse>>()
                }
                HttpStatusCode.BadRequest {
                    description = "邮箱格式错误或类型不支持"
                }
            }
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
        }.describe {
            tag("认证管理")
            operationId = "verifyEmail"
            summary = "验证邮箱"
            description = "使用收到的验证码验证用户邮箱"
            requestBody {
                description = "验证请求信息"
                schema = jsonSchema<VerifyOTPRequest>()
            }
            responses {
                HttpStatusCode.OK {
                    description = "邮箱验证成功"
                    schema = jsonSchema<ApiResponse<SuccessResponse>>()
                }
                HttpStatusCode.BadRequest {
                    description = "验证码错误或已过期"
                }
            }
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
        }.describe {
            tag("认证管理")
            operationId = "resetPassword"
            summary = "重置密码"
            description = "使用验证码重置用户密码"
            requestBody {
                description = "重置密码请求信息"
                schema = jsonSchema<ResetPasswordRequest>()
            }
            responses {
                HttpStatusCode.OK {
                    description = "密码重置成功"
                    schema = jsonSchema<ApiResponse<SuccessResponse>>()
                }
                HttpStatusCode.BadRequest {
                    description = "验证码错误或密码格式不符"
                }
            }
        }

        post("/refresh") {
            val request = call.receive<RefreshTokenRequest>()
            val result = authUseCase.refreshToken(request.refreshToken)
            call.respondOk(result.toLoginResponse(), "令牌刷新成功")
        }.describe {
            tag("认证管理")
            operationId = "refreshToken"
            summary = "刷新令牌"
            description = "使用刷新令牌获取新的访问令牌"
            requestBody {
                description = "刷新令牌请求"
                schema = jsonSchema<RefreshTokenRequest>()
            }
            responses {
                HttpStatusCode.OK {
                    description = "刷新成功"
                    schema = jsonSchema<ApiResponse<LoginResponse>>()
                }
                HttpStatusCode.Unauthorized {
                    description = "刷新令牌无效或已过期"
                }
            }
        }

        post("/sign-out") {
            val request = runCatching { call.receive<RefreshTokenRequest>() }.getOrNull()
            if (request != null) {
                authUseCase.logout(request.refreshToken)
            }
            call.respond(HttpStatusCode.NoContent)
        }.describe {
            tag("认证管理")
            operationId = "logout"
            summary = "退出登录"
            description = "注销当前会话，使刷新令牌失效"
            requestBody {
                description = "需要注销的刷新令牌"
                required = false
                schema = jsonSchema<RefreshTokenRequest>()
            }
            responses {
                HttpStatusCode.NoContent {
                    description = "退出成功"
                }
            }
        }

        authenticate("auth-jwt") {
            get("/me") {
                val userId = call.requireUserId()
                val user = authUseCase.getProfile(userId)
                call.respondOk(user.toUserProfile())
            }.describe {
                tag("认证管理")
                operationId = "getCurrentUser"
                summary = "获取个人信息"
                description = "获取当前登录用户的详细信息"
                responses {
                    HttpStatusCode.OK {
                        description = "获取成功"
                        schema = jsonSchema<ApiResponse<UserProfile>>()
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
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
            }.describe {
                tag("认证管理")
                operationId = "changePassword"
                summary = "修改密码"
                description = "已登录用户修改自己的密码"
                requestBody {
                    description = "修改密码请求"
                    schema = jsonSchema<ChangePasswordRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "密码修改成功"
                        schema = jsonSchema<ApiResponse<SuccessResponse>>()
                    }
                    HttpStatusCode.Unauthorized {
                        description = "旧密码错误或未登录"
                    }
                    HttpStatusCode.BadRequest {
                        description = "新密码格式不符"
                    }
                }
            }

            delete("/account") {
                val userId = call.requireUserId()
                authUseCase.deleteAccount(userId)
                call.respond(HttpStatusCode.NoContent)
            }.describe {
                tag("认证管理")
                operationId = "deleteAccount"
                summary = "注销账号"
                description = "永久删除当前用户账号"
                responses {
                    HttpStatusCode.NoContent {
                        description = "账号已删除"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录"
                    }
                }
            }
        }
    }
}
