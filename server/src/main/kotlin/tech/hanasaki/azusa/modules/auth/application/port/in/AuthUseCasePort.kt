package tech.hanasaki.azusa.modules.auth.application.port.`in`

import tech.hanasaki.azusa.modules.auth.application.dto.AuthenticatedUser
import tech.hanasaki.azusa.modules.auth.application.dto.UserProfileDto
import tech.hanasaki.azusa.modules.auth.domain.model.PlainPassword
import tech.hanasaki.azusa.modules.auth.domain.model.Username
import tech.hanasaki.azusa.shared.domain.model.vo.AvatarUrl
import tech.hanasaki.azusa.shared.domain.model.vo.Email
import tech.hanasaki.azusa.shared.domain.model.vo.UserId

/**
 * Auth use case - 主端口（输入端口）
 * 定义认证模块的所有公共操作
 */
interface AuthUseCasePort {
    /**
     * 用户注册
     */
    suspend fun register(
        email: Email,
        password: PlainPassword,
        username: Username,
    )

    /**
     * 用户登录
     */
    suspend fun login(
        email: Email,
        password: PlainPassword,
    ): AuthenticatedUser

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
    suspend fun resetPassword(
        email: Email,
        newPassword: PlainPassword,
    )

    /**
     * 修改密码
     */
    suspend fun changePassword(
        userId: UserId,
        oldPassword: PlainPassword,
        newPassword: PlainPassword,
    )

    /**
     * 获取用户信息
     */
    suspend fun getProfile(userId: UserId): UserProfileDto

    suspend fun updateProfile(userId: UserId, username: Username, avatar: AvatarUrl?)

    /**
     * 使用 RefreshToken 刷新 AccessToken
     */
    suspend fun refreshToken(refreshToken: String): AuthenticatedUser

    /**
     * 更新头像
     */
    suspend fun updateAvatar(userId: UserId, avatarUrl: AvatarUrl): UserProfileDto

    /**
     * 授予管理员权限
     */
    suspend fun grantAdmin(operatorId: UserId, targetUserId: UserId)

    /**
     * 撤销管理员权限
     */
    suspend fun revokeAdmin(operatorId: UserId, targetUserId: UserId)

    /**
     * 密码变更后处理：销毁所有 refresh token，发送通知
     */
    suspend fun onPasswordChanged(userId: UserId, email: String?)
}