package tech.hanasaki.momotalk_plus.core.utils

import androidx.compose.runtime.Composable
import tech.hanasaki.momotalk_plus.core.domain.model.ImageData

@Composable
expect fun rememberImagePicker(onImagePicked: (ImageData?) -> Unit): () -> Unit
