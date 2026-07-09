package org.awi.fitness.utils

import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.Rect
import org.jetbrains.skia.Surface
import kotlin.math.max

/**
 * Downscale + JPEG-compress via Skia (already used by ImageDecode on iOS) — no UIKit/NSData
 * bridging. Best-effort: returns the original bytes on any failure so uploads never break.
 */
actual fun compressImage(bytes: ByteArray, maxDim: Int, quality: Int): ByteArray {
    return try {
        val img = Image.makeFromEncoded(bytes)
        val longest = max(img.width, img.height)
        val scaled = if (longest > maxDim && longest > 0) {
            val ratio = maxDim.toFloat() / longest
            val nw = (img.width * ratio).toInt().coerceAtLeast(1)
            val nh = (img.height * ratio).toInt().coerceAtLeast(1)
            val surface = Surface.makeRasterN32Premul(nw, nh)
            surface.canvas.drawImageRect(img, Rect.makeWH(nw.toFloat(), nh.toFloat()))
            surface.makeImageSnapshot()
        } else {
            img
        }
        val data = scaled.encodeToData(EncodedImageFormat.JPEG, quality.coerceIn(1, 100)) ?: return bytes
        val out = data.bytes
        if (out.isNotEmpty()) out else bytes
    } catch (_: Throwable) {
        bytes
    }
}
