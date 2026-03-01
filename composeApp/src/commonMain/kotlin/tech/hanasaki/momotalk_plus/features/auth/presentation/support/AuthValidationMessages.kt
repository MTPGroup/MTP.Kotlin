package tech.hanasaki.momotalk_plus.features.auth.presentation.support

import momotalkplus.composeapp.generated.resources.Res
import momotalkplus.composeapp.generated.resources.auth_email_invalid
import momotalkplus.composeapp.generated.resources.auth_email_required
import momotalkplus.composeapp.generated.resources.auth_email_verified_success
import momotalkplus.composeapp.generated.resources.auth_login_failed
import momotalkplus.composeapp.generated.resources.auth_otp_required
import momotalkplus.composeapp.generated.resources.auth_otp_sent_check_email
import momotalkplus.composeapp.generated.resources.auth_otp_sent_to_email
import momotalkplus.composeapp.generated.resources.auth_password_min_length
import momotalkplus.composeapp.generated.resources.auth_password_not_match
import momotalkplus.composeapp.generated.resources.auth_reset_password_failed
import momotalkplus.composeapp.generated.resources.auth_send_otp_failed
import momotalkplus.composeapp.generated.resources.auth_send_otp_retry_failed
import momotalkplus.composeapp.generated.resources.auth_sign_up_failed
import momotalkplus.composeapp.generated.resources.auth_verify_failed
import org.jetbrains.compose.resources.StringResource

object AuthValidationMessages {
    val EMAIL_REQUIRED: StringResource = Res.string.auth_email_required
    val EMAIL_INVALID: StringResource = Res.string.auth_email_invalid
    val PASSWORD_MIN_LENGTH: StringResource = Res.string.auth_password_min_length
    val PASSWORD_NOT_MATCH: StringResource = Res.string.auth_password_not_match
    val OTP_REQUIRED: StringResource = Res.string.auth_otp_required
    val EMAIL_VERIFIED_SUCCESS: StringResource = Res.string.auth_email_verified_success

    val LOGIN_FAILED: StringResource = Res.string.auth_login_failed
    val SIGN_UP_FAILED: StringResource = Res.string.auth_sign_up_failed
    val SEND_OTP_FAILED: StringResource = Res.string.auth_send_otp_failed
    val SEND_OTP_RETRY_FAILED: StringResource = Res.string.auth_send_otp_retry_failed
    val VERIFY_FAILED: StringResource = Res.string.auth_verify_failed
    val RESET_PASSWORD_FAILED: StringResource = Res.string.auth_reset_password_failed

    val OTP_SENT_CHECK_EMAIL: StringResource = Res.string.auth_otp_sent_check_email
    val OTP_SENT_TO_EMAIL: StringResource = Res.string.auth_otp_sent_to_email
}
