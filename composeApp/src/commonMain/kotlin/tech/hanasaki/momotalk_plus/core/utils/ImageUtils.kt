package tech.hanasaki.momotalk_plus.core.utils

import androidx.compose.ui.graphics.ImageBitmap

/**
 * 将 Base64 编码的字符串解码为 ImageBitmap。
 *
 * @param base64String Base64 编码的字符串。
 * @return 解码后的 ImageBitmap，如果解码失败则返回 null。
 */
expect fun decodeBase64ToBitmap(base64String: String): ImageBitmap?