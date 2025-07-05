package tech.hanasaki.momotalk_plus.core.utils

fun isValidEmail(email: String): Boolean {
    val emailRegex = Regex(
        "[a-zA-Z0-9+._%\\-]{1,256}" +
                "@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
    )
    return emailRegex.matches(email)
}
