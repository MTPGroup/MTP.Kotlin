package tech.hanasaki.azusa.modules.auth.application.port.out

import tech.hanasaki.azusa.modules.auth.domain.model.HashedPassword
import tech.hanasaki.azusa.modules.auth.domain.model.PlainPassword

/**
 * 密码编码器端口 - 被驱动端口（输出端口）
 */
interface PasswordEncoderPort {
    fun encode(raw: PlainPassword): HashedPassword
    fun matches(raw: PlainPassword, encoded: HashedPassword): Boolean
}