package tech.hanasaki.azusa.modules.auth.application.port.out

/**
 * 密码编码器端口 - 被驱动端口（输出端口）
 */
interface PasswordEncoder {
    fun encode(raw: String): String
    fun matches(raw: String, encoded: String): Boolean
}