package tech.hanasaki.azusa.modules.auth.application.dto

data class AuthenticatedUser(
    val user: UserProfileDto,
    val tokens: TokenPair,
)