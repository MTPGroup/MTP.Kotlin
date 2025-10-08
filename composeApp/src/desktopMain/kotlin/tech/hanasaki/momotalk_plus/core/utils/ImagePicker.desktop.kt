package tech.hanasaki.momotalk_plus.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import tech.hanasaki.momotalk_plus.core.domain.model.ImageData
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun rememberImagePicker(onImagePicked: (ImageData?) -> Unit): () -> Unit {
    return remember {
        {
            val fileChooser = JFileChooser()
            fileChooser.dialogTitle = "选择图片"
            fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY

            // Add image filters
            val imageFilter = FileNameExtensionFilter(
                "图片文件 (*.jpg, *.jpeg, *.png, *.gif, *.bmp)",
                "jpg", "jpeg", "png", "gif", "bmp"
            )
            fileChooser.fileFilter = imageFilter
            fileChooser.isAcceptAllFileFilterUsed = false

            val result = fileChooser.showOpenDialog(null)

            if (result == JFileChooser.APPROVE_OPTION) {
                val selectedFile: File = fileChooser.selectedFile
                val bytes = selectedFile.readBytes()
                val mimeType = when (selectedFile.extension.lowercase()) {
                    "jpg", "jpeg" -> "image/jpeg"
                    "png" -> "image/png"
                    "gif" -> "image/gif"
                    "bmp" -> "image/bmp"
                    else -> "image/jpeg"
                }

                onImagePicked(
                    ImageData(
                        fileName = selectedFile.name,
                        byteArray = bytes,
                        mimeType = mimeType
                    )
                )
            } else {
                onImagePicked(null)
            }
        }
    }
}