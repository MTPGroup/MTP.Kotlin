package tech.hanasaki.azusa.modules.auth.application.port.`in`

import tech.hanasaki.azusa.common.domain.model.Email
import tech.hanasaki.azusa.common.domain.model.UserId
import tech.hanasaki.azusa.modules.auth.domain.model.User
import tech.hanasaki.azusa.modules.auth.application.command.LoginCommand
import tech.hanasaki.azusa.modules.auth.application.command.RegisterCommand
import tech.hanasaki.azusa.modules.auth.application.command.ResetPasswordCommand
import tech.hanasaki.azusa.modules.auth.application.result.LoginResult

/**
 * Auth use case - 主端口（输入端口）
 * 定义认证模块的所有公共操作
 */
interface AuthUseCase {
    /**
     * 用户注册
     */
    suspend fun register(cmd: RegisterCommand)

    /**
     * 用户登录
     */
    suspend fun login(cmd: LoginCommand): LoginResult

    /**
     * 用户登出
     */
    suspend fun logout(refreshToken: String)

    /**
     * 删除账号
     */
    suspend fun deleteAccount(userId: UserId)

    /**
     * 邮箱验证
     */
    suspend fun verifyEmail(email: Email)

    /**
     * 重置密码
     */
    suspend fun resetPassword(cmd: ResetPasswordCommand)

    /**
     * 修改密码
     */
    suspend fun changePassword(userId: UserId, oldPassword: String, newPassword: String)

    /**
     * 获取用户信息
     */
    suspend fun getProfile(userId: UserId): User

    /**
     * 使用 RefreshToken 刷新 AccessToken
     */
    suspend fun refreshToken(refreshToken: String): LoginResult
}