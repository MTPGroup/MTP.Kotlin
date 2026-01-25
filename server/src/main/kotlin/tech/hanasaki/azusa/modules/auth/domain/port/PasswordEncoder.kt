package tech.hanasaki.azusa.modules.auth.domain.port

/**
 * 密码编码器端口 - 领域层定义的外部服务接口
 */
interface PasswordEncoder {
    fun encode(raw: String): String
    fun matches(raw: String, encoded: String): Boolean
}
