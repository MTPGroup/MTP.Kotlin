package tech.hanasaki.momotalk_plus.features.auth.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class OTPType {
    @SerialName("sign-in")
    SIGN_IN,

    @SerialName("reset-password")
    RESET_PASSWORD,

    @SerialName("email-verification")
    VERIFY_EMAIL,
}