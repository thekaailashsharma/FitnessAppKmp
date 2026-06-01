@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package org.awi.fitness.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGRect
import platform.Foundation.NSData
import platform.posix.memcpy
import platform.UIKit.UIApplication
import platform.UIKit.UIImage
import platform.UIKit.UIImageJPEGRepresentation
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UIImagePickerControllerEditedImage
import platform.UIKit.UIImagePickerControllerOriginalImage
import platform.UIKit.UIImagePickerControllerSourceType
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject

/**
 * Retains the delegate object alive for the lifetime of the picker.
 * UIImagePickerController's delegate is a weak reference — we must keep it alive ourselves.
 */
private var activeDelegate: ImagePickerDelegate? = null

private class ImagePickerDelegate(
    private val onImagePicked: (ByteArray?) -> Unit
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>
    ) {
        val image = (didFinishPickingMediaWithInfo[UIImagePickerControllerEditedImage]
            ?: didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage]) as? UIImage

        val bytes: ByteArray? = image?.let { uiImage ->
            UIImageJPEGRepresentation(uiImage, 0.85)?.toByteArray()
        }

        picker.dismissViewControllerAnimated(true, completion = null)
        activeDelegate = null
        onImagePicked(bytes)
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true, completion = null)
        activeDelegate = null
        onImagePicked(null)
    }
}

private fun NSData.toByteArray(): ByteArray {
    val len = this.length.toInt()
    if (len == 0) return ByteArray(0)
    return ByteArray(len).also { dest ->
        dest.usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytes, length)
        }
    }
}

@Composable
actual fun rememberImagePickerLauncher(
    onImagePicked: (ByteArray?) -> Unit
): ImagePickerLauncher {
    val callback = remember { onImagePicked }
    return remember {
        object : ImagePickerLauncher {
            override fun launch() {
                val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
                    ?: return

                val picker = UIImagePickerController()
                picker.sourceType = UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
                picker.allowsEditing = false

                val delegate = ImagePickerDelegate(callback)
                activeDelegate = delegate
                picker.delegate = delegate

                rootVC.presentViewController(picker, animated = true, completion = null)
            }
        }
    }
}
