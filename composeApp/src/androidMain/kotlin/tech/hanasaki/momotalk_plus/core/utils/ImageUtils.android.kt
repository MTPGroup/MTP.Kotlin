package tech.hanasaki.momotalk_plus.core.utils

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

/**
 * 将 Base64 编码的字符串解码为 ImageBitmap。
 *
 * @param base64String Base64 编码的字符串。
 * @return 解码后的 ImageBitmap，如果解码失败则返回 null。
 */
actual fun decodeBase64ToBitmap(base64String: String): ImageBitmap? {
    return try {
        val pureBase64 = if (base64String.contains(',')) {
            base64String.substringAfter(',')
        } else {
            base64String
        }

        // 解码纯 Base64 字符串
        val imageBytes = Base64.decode(pureBase64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)?.asImageBitmap()
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        null
    }
}