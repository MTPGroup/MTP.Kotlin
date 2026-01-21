package tech.hanasaki.azusa.modules.auth.application.port

interface PasswordEncoder {
    fun encode(raw: String): String
    fun matches(raw: String, encoded: String): Boolean
}