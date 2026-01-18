package tech.hanasaki.azusa.auth.internal.application.port

interface PasswordEncoder {
    fun encode(raw: String): String
    fun matches(raw: String, encoded: String): Boolean
}