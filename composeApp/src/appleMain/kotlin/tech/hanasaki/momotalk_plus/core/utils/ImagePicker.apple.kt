package tech.hanasaki.momotalk_plus.core.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.launch
import platform.Foundation.NSDate
import platform.Foundation.getBytes
import platform.Foundation.timeIntervalSince1970
import platform.PhotosUI.*
import platform.UIKit.*
import platform.darwin.NSObject
import tech.hanasaki.momotalk_plus.core.domain.model.ImageData

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberImagePicker(onImagePicked: (ImageData?) -> Unit): () -> Unit {
    val scope = rememberCoroutineScope()
    val pickerDelegate = remember { ImagePickerDelegate() }

    DisposableEffect(Unit) {
        onDispose {
            pickerDelegate.cleanup()
        }
    }

    return remember {
        {
            scope.launch {
                val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
                if (rootViewController == null) {
                    onImagePicked(null)
                    return@launch
                }

                // 使用 PHPickerViewController (iOS 14+) - 更现代的方式
                if (PHPickerViewController.isAvailableForConfiguration()) {
                    val configuration = PHPickerConfiguration().apply {
                        selectionLimit = 1
                        filter = PHPickerFilter.imagesFilter
                    }

                    val picker = PHPickerViewController(configuration)

                    pickerDelegate.onImageSelected = { imageData ->
                        onImagePicked(imageData)
                    }

                    picker.delegate = pickerDelegate
                    rootViewController.presentViewController(picker, animated = true, completion = null)

                } else {
                    // 降级到 UIImagePickerController (iOS 14 以下)
                    if (UIImagePickerController.isSourceTypeAvailable(
                            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
                        )
                    ) {
                        val picker = UIImagePickerController()
                        picker.sourceType =
                            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
                        picker.allowsEditing = false

                        val legacyDelegate = UIImagePickerDelegate()
                        legacyDelegate.onImageSelected = { imageData ->
                            onImagePicked(imageData)
                        }

                        picker.delegate = legacyDelegate
                        rootViewController.presentViewController(picker, animated = true, completion = null)
                    } else {
                        onImagePicked(null)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun PHPickerViewController.Companion.isAvailableForConfiguration(): Boolean {
    return try {
        true
    } catch (_: Exception) {
        false
    }
}

@OptIn(ExperimentalForeignApi::class)
private class ImagePickerDelegate : NSObject(), PHPickerViewControllerDelegateProtocol {
    var onImageSelected: ((ImageData?) -> Unit)? = null

    override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {

        picker.dismissViewControllerAnimated(true, completion = null)

        if (didFinishPicking.isEmpty()) {
            onImageSelected?.invoke(null)
            return
        }

        val result = didFinishPicking.firstOrNull() as? PHPickerResult
        if (result == null) {
            onImageSelected?.invoke(null)
            return
        }

        val itemProvider = result.itemProvider

        // 检查是否包含图片
        if (itemProvider.hasItemConformingToTypeIdentifier("public.image")) {
            itemProvider.loadDataRepresentationForTypeIdentifier("public.image") { data, error ->
                if (error != null) {
                    onImageSelected?.invoke(null)
                    return@loadDataRepresentationForTypeIdentifier
                }

                if (data == null) {
                    onImageSelected?.invoke(null)
                    return@loadDataRepresentationForTypeIdentifier
                }

                // 从 NSData 创建 UIImage
                val uiImage = UIImage.imageWithData(data)
                if (uiImage == null) {
                    onImageSelected?.invoke(null)
                    return@loadDataRepresentationForTypeIdentifier
                }

                val imageData = convertUIImageToImageData(uiImage)
                onImageSelected?.invoke(imageData)
            }
        } else {
            onImageSelected?.invoke(null)
        }
    }

    fun cleanup() {
        onImageSelected = null
    }
}

@OptIn(ExperimentalForeignApi::class)
private class UIImagePickerDelegate : NSObject(), UIImagePickerControllerDelegateProtocol,
    UINavigationControllerDelegateProtocol {
    var onImageSelected: ((ImageData?) -> Unit)? = null

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        picker.dismissViewControllerAnimated(true, completion = null)

        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        if (image == null) {
            onImageSelected?.invoke(null)
            return
        }

        val imageData = convertUIImageToImageData(image)
        println("[ImagePicker] iOS - Image converted, size: ${imageData?.byteArray?.size ?: 0} bytes")
        onImageSelected?.invoke(imageData)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        onImageSelected?.invoke(null)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun convertUIImageToImageData(image: UIImage): ImageData? {
    return try {
        // 转换为 JPEG 格式，压缩质量 0.8
        val jpegData = UIImageJPEGRepresentation(image, 0.8)
        if (jpegData == null) {
            return null
        }

        // 将 NSData 转换为 ByteArray
        val length = jpegData.length.toInt()
        val bytes = ByteArray(length)

        bytes.usePinned { pinned ->
            jpegData.getBytes(pinned.addressOf(0), jpegData.length)
        }

        val fileName = "avatar_${NSDate().timeIntervalSince1970.toLong()}.jpg"

        ImageData(
            fileName = fileName,
            byteArray = bytes,
            mimeType = "image/jpeg"
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
