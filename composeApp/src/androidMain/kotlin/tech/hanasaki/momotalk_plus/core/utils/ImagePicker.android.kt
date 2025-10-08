package tech.hanasaki.momotalk_plus.core.utils

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import tech.hanasaki.momotalk_plus.core.domain.model.ImageData

@Composable
actual fun rememberImagePicker(onImagePicked: (ImageData?) -> Unit): () -> Unit {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        val imageData = uri?.let { readImageFromUri(context, it) }
        onImagePicked(imageData)
    }

    return {
        launcher.launch("image/*")
    }
}

private fun readImageFromUri(context: Context, uri: Uri): ImageData? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        if (inputStream == null) {
            null
        } else {
            val bytes = inputStream.use { it.readBytes() }
            val fileName = getFileName(context, uri) ?: "image_${System.currentTimeMillis()}.jpg"
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"

            ImageData(
                fileName = fileName,
                byteArray = bytes,
                mimeType = mimeType
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun getFileName(context: Context, uri: Uri): String? {
    var fileName: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) {
            fileName = cursor.getString(nameIndex)
        }
    }
    return fileName
}
