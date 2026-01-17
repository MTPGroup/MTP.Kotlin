package tech.hanasaki.azusa.auth.application.service

interface PasswordEncoder {
    fun encode(raw: String): String
    fun matches(raw: String, encoded: String): Boolean
}