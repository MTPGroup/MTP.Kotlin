package tech.hanasaki.azusa.modules.auth.adapter.`in`.web

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.openapi.*
import io.ktor.openapi.ReferenceOr.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.routing.openapi.*
import io.ktor.utils.io.jvm.javaio.*
import org.koin.ktor.ext.inject
import tech.hanasaki.azusa.modules.auth.adapter.`in`.web.dto.*
import tech.hanasaki.azusa.modules.auth.adapter.`in`.web.mapper.toLoginResponse
import tech.hanasaki.azusa.modules.auth.adapter.`in`.web.mapper.toUserProfile
import tech.hanasaki.azusa.modules.auth.application.port.`in`.AuthUseCasePort
import tech.hanasaki.azusa.modules.auth.application.port.`in`.OtpUseCasePort
import tech.hanasaki.azusa.modules.auth.domain.model.OtpType
import tech.hanasaki.azusa.modules.auth.domain.model.PlainPassword
import tech.hanasaki.azusa.modules.auth.domain.model.Username
import tech.hanasaki.azusa.shared.domain.exception.ValidationException
import tech.hanasaki.azusa.shared.domain.model.vo.AvatarUrl
import tech.hanasaki.azusa.shared.domain.model.vo.Email
import tech.hanasaki.azusa.shared.domain.model.vo.UserId
import tech.hanasaki.azusa.shared.infrastructure.web.response.ApiResponse
import tech.hanasaki.azusa.shared.infrastructure.web.response.respondOk
import tech.hanasaki.azusa.shared.infrastructure.web.route.requireAdmin
import tech.hanasaki.azusa.shared.infrastructure.web.route.requireUserId
import tech.hanasaki.azusa.shared.infrastructure.web.route.uuidParam
import tech.hanasaki.azusa.shared.infrastructure.web.validation.ValidationCollector
import tech.hanasaki.azusa.shared.port.out.FileStoragePort
import kotlin.uuid.Uuid

fun Route.authRoutes() {
    val authUseCase: AuthUseCasePort by inject()
    val otpService: OtpUseCasePort by inject()
    val fileStoragePort: FileStoragePort by inject()

    route("/auth") {
        post("/sign-up/email") {
            val request = call.receive<SignUpRequest>()
            val validator = ValidationCollector()
            val email = validator.vo("email") { Email(request.email) }
            val password = validator.vo("password") { PlainPassword(request.password) }
            val username = validator.vo("name") { Username(request.name) }
            validator.throwIfAny()

            authUseCase.register(
                email = requireNotNull(email),
                password = requireNotNull(password),
                username = requireNotNull(username),
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
            val validator = ValidationCollector()
            val email = validator.vo("email") { Email(request.email) }
            val password = validator.vo("password") { PlainPassword(request.password) }
            validator.throwIfAny()

            val result = authUseCase.login(
                email = requireNotNull(email),
                password = requireNotNull(password),
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
            val validator = ValidationCollector()
            val email = validator.vo("email") { Email(request.email) }
            validator.throwIfAny()

            otpService.generate(requireNotNull(email), OtpType.fromString(request.type))
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
            val validator = ValidationCollector()
            val email = validator.vo("email") { Email(request.email) }
            validator.throwIfAny()
            val verifiedEmail = requireNotNull(email)
            otpService.verify(
                verifiedEmail,
                OtpType.VERIFY_EMAIL,
                request.otp
            )
            authUseCase.verifyEmail(verifiedEmail)
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
            val validator = ValidationCollector()
            val email = validator.vo("email") { Email(request.email) }
            val password = validator.vo("password") { PlainPassword(request.password) }
            validator.throwIfAny()

            val verifiedEmail = requireNotNull(email)
            otpService.verify(
                verifiedEmail,
                OtpType.RESET_PASSWORD,
                request.otp
            )
            authUseCase.resetPassword(
                verifiedEmail,
                requireNotNull(password)
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

            put("/me") {
                val userId = call.requireUserId()
                val request = call.receive<UpdateProfileRequest>()
                val validator = ValidationCollector()
                val username = validator.vo("username") { Username(request.username) }
                val avatar = request.avatar?.let { validator.vo("avatar") { AvatarUrl(it) } }
                validator.throwIfAny()

                authUseCase.updateProfile(
                    userId,
                    requireNotNull(username),
                    avatar
                )
                call.respondOk(SuccessResponse(), "个人信息更新成功")
            }.describe {
                tag("认证管理")
                operationId = "updateProfile"
                summary = "更新个人信息"
                description = "更新当前登录用户的用户名"
                requestBody {
                    description = "更新信息"
                    schema = jsonSchema<UpdateProfileRequest>()
                }
                responses {
                    HttpStatusCode.OK {
                        description = "更新成功"
                        schema = jsonSchema<ApiResponse<SuccessResponse>>()
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }

            put("/me/avatar") {
                val userId = call.requireUserId()

                val multipart = call.receiveMultipart()
                var contentType: String? = null
                var fileBytes: ByteArray? = null
                var fileName: String? = null

                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            fileName = part.originalFileName
                            contentType = part.contentType?.toString()
                            fileBytes = part.provider().toInputStream().readBytes()
                        }

                        else -> {}
                    }
                    part.dispose()
                }

                val bytes = fileBytes ?: throw ValidationException("未上传文件")
                val mime = contentType ?: "application/octet-stream"
                val allowedTypes = setOf("image/jpeg", "image/png", "image/gif", "image/webp")
                if (mime !in allowedTypes) {
                    throw ValidationException("不支持的图片格式，仅支持 JPEG、PNG、GIF、WebP")
                }
                val ext = (fileName ?: "avatar").substringAfterLast('.', "png")
                val objectKey = "avatars/${userId.value}/${Uuid.random()}.$ext"

                fileStoragePort.upload(objectKey, mime, bytes)
                val avatarUrl = AvatarUrl(fileStoragePort.publicUrl(objectKey))
                call.respondOk(UploadAvatarResponse(avatarUrl.value), "头像上传成功")
            }.describe {
                tag("认证管理")
                operationId = "uploadAvatar"
                summary = "上传头像"
                description = "上传用户头像，支持 multipart/form-data"
                requestBody {
                    description = "头像文件"
                    required = true
                    ContentType.MultiPart.FormData {
                        schema = JsonSchema(
                            type = JsonType.OBJECT,
                            properties = mapOf(
                                "file" to Value(
                                    JsonSchema(
                                        type = JsonType.STRING,
                                        format = "binary",
                                        description = "头像图片文件"
                                    )
                                )
                            ),
                            required = listOf("file")
                        )
                    }
                }
                responses {
                    HttpStatusCode.OK {
                        description = "上传成功"
                        schema = jsonSchema<ApiResponse<UploadAvatarResponse>>()
                    }
                    HttpStatusCode.BadRequest {
                        description = "未上传文件或文件格式无效"
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                }
            }

            post("/password/change") {
                val userId = call.requireUserId()
                val request = call.receive<ChangePasswordRequest>()
                val validator = ValidationCollector()
                val oldPassword = validator.vo("oldPassword") { PlainPassword(request.oldPassword) }
                val newPassword = validator.vo("newPassword") { PlainPassword(request.newPassword) }
                validator.throwIfAny()

                authUseCase.changePassword(
                    userId,
                    requireNotNull(oldPassword),
                    requireNotNull(newPassword),
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

            post("/admin/users/{userId}/grant-admin") {
                call.requireAdmin()
                val operatorId = call.requireUserId()
                val targetUserId = UserId(call.uuidParam("userId"))
                authUseCase.grantAdmin(operatorId, targetUserId)
                call.respondOk(SuccessResponse(), "已授予管理员权限")
            }.describe {
                tag("管理员")
                operationId = "grantAdmin"
                summary = "授予管理员权限"
                description = "管理员授予指定用户管理员权限"
                responses {
                    HttpStatusCode.OK {
                        description = "授权成功"
                        schema = jsonSchema<ApiResponse<SuccessResponse>>()
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                    HttpStatusCode.Forbidden {
                        description = "需要管理员权限"
                    }
                    HttpStatusCode.NotFound {
                        description = "目标用户不存在"
                    }
                }
            }

            post("/admin/users/{userId}/revoke-admin") {
                call.requireAdmin()
                val operatorId = call.requireUserId()
                val targetUserId = UserId(call.uuidParam("userId"))
                authUseCase.revokeAdmin(operatorId, targetUserId)
                call.respondOk(SuccessResponse(), "已撤销管理员权限")
            }.describe {
                tag("管理员")
                operationId = "revokeAdmin"
                summary = "撤销管理员权限"
                description = "管理员撤销指定用户的管理员权限"
                responses {
                    HttpStatusCode.OK {
                        description = "撤销成功"
                        schema = jsonSchema<ApiResponse<SuccessResponse>>()
                    }
                    HttpStatusCode.Unauthorized {
                        description = "未登录或令牌无效"
                    }
                    HttpStatusCode.Forbidden {
                        description = "需要管理员权限"
                    }
                    HttpStatusCode.NotFound {
                        description = "目标用户不存在"
                    }
                }
            }
        }
    }
}
