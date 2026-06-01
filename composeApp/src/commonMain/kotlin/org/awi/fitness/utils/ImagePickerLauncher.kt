package org.awi.fitness.utils

import androidx.compose.runtime.Composable

/**
 * Platform-agnostic handle for launching the native photo picker.
 * Obtain via [rememberImagePickerLauncher].
 */
interface ImagePickerLauncher {
    fun launch()
}

/**
 * Creates a platform-specific image picker launcher that calls [onImagePicked] with
 * the raw JPEG bytes of the selected photo, or null if the user cancelled.
 */
@Composable
expect fun rememberImagePickerLauncher(
    onImagePicked: (ByteArray?) -> Unit
): ImagePickerLauncher
