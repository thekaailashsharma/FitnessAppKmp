package org.awi.fitness.utils

/**
 * Downscales [bytes] so its largest dimension is at most [maxDim] and re-encodes it as
 * JPEG at [quality] (1-100). Intended to run before uploading a picked photo so avatars
 * (~512px) and post images (~1080px) stay small and upload fast.
 *
 * Implementations MUST be forgiving: if decoding/encoding fails, or the image is already
 * smaller than [maxDim], they return bytes that are safe to upload (the original bytes as a
 * fallback) rather than throwing, so a compression hiccup never breaks the upload flow.
 */
expect fun compressImage(bytes: ByteArray, maxDim: Int = 1080, quality: Int = 80): ByteArray
