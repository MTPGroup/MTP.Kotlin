package tech.hanasaki.momotalk_plus.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import tech.hanasaki.momotalk_plus.core.domain.model.ImageData

@Composable
actual fun rememberImagePicker(onImagePicked: (ImageData?) -> Unit): () -> Unit {
    // WasmJs implementation - same as Web
    return remember {
        {
            println("[ImagePicker] WasmJs image picker not yet fully implemented")
            onImagePicked(null)
        }
    }
}

