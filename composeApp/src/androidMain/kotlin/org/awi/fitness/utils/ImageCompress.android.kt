package org.awi.fitness.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

actual fun compressImage(bytes: ByteArray, maxDim: Int, quality: Int): ByteArray {
    return try {
        val source = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes
        val longest = max(source.width, source.height)
        val scaled = if (maxDim in 1 until longest) {
            val ratio = maxDim.toFloat() / longest.toFloat()
            val w = (source.width * ratio).roundToInt().coerceAtLeast(1)
            val h = (source.height * ratio).roundToInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(source, w, h, true)
        } else {
            source
        }

        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(1, 100), out)

        if (scaled !== source) scaled.recycle()
        source.recycle()

        val result = out.toByteArray()
        if (result.isNotEmpty()) result else bytes
    } catch (_: Throwable) {
        // Never break the upload path over a compression failure.
        bytes
    }
}
