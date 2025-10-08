package tech.hanasaki.momotalk_plus.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader
import org.w3c.files.get
import tech.hanasaki.momotalk_plus.core.domain.model.ImageData

@OptIn(ExperimentalWasmJsInterop::class)
@Composable
actual fun rememberImagePicker(onImagePicked: (ImageData?) -> Unit): () -> Unit {
    return remember {
        {
            val input = document.createElement("input") as HTMLInputElement
            input.type = "file"
            input.accept = "image/*"

            input.onchange = {
                val file = input.files?.get(0)
                if (file != null) {
                    val reader = FileReader()
                    reader.onload = {
                        try {
                            val arrayBuffer = reader.result as? org.khronos.webgl.ArrayBuffer
                            if (arrayBuffer != null) {
                                val byteArray = org.khronos.webgl.Int8Array(arrayBuffer)
                                val bytes = ByteArray(byteArray.length)
                                for (i in 0 until byteArray.length) {
                                    bytes[i] = byteArray[i]
                                }

                                onImagePicked(
                                    ImageData(
                                        fileName = file.name,
                                        byteArray = bytes,
                                        mimeType = file.type.ifEmpty { "image/jpeg" }
                                    )
                                )
                            } else {
                                onImagePicked(null)
                            }
                        } catch (e: Exception) {
                            onImagePicked(null)
                        }
                    }
                    reader.onerror = {
                        onImagePicked(null)
                    }
                    reader.readAsArrayBuffer(file)
                } else {
                    onImagePicked(null)
                }
            }

            input.oncancel = {
                onImagePicked(null)
            }

            input.click()
        }
    }
}