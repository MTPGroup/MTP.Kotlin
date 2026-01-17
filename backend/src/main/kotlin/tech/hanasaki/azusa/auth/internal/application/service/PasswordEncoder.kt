package tech.hanasaki.azusa.auth.internal.application.service

interface PasswordEncoder {
    fun encode(raw: String): String
    fun matches(raw: String, encoded: String): Boolean
}