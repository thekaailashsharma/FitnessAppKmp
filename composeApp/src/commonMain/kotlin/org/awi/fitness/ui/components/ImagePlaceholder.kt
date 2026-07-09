package org.awi.fitness.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Photo
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.awi.fitness.network.KtorClient
import org.awi.fitness.utils.decodeImageBitmap

// Process-wide in-memory cache of decoded images, keyed by URL. Loads run on the Compose main
//    coroutine, so single-threaded access is safe. This makes a re-entered screen (e.g. switching
//    tabs) show avatars instantly instead of re-downloading + re-decoding every time.
private const val IMAGE_CACHE_MAX = 150
private val imageBitmapCache = LinkedHashMap<String, ImageBitmap>()

private fun cachedImage(url: String): ImageBitmap? = imageBitmapCache[url]

private fun cacheImage(url: String, bmp: ImageBitmap) {
    imageBitmapCache.remove(url)
    imageBitmapCache[url] = bmp
    if (imageBitmapCache.size > IMAGE_CACHE_MAX) {
        val it = imageBitmapCache.keys.iterator()
        if (it.hasNext()) { it.next(); it.remove() } // evict oldest
    }
}

@Composable
fun ImagePlaceholder(
    modifier: Modifier = Modifier,
    url: String? = null,
    imageBytes: ByteArray? = null,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    showInitial: Boolean = false,
    initial: String = "",
    tint: Color = MaterialTheme.colorScheme.primary
) {
    // Seed synchronously from the cache so re-entry shows the image with zero flicker/spinner.
    var bitmap by remember(url, imageBytes) {
        mutableStateOf(if (!url.isNullOrBlank()) cachedImage(url) else null)
    }
    var loading by remember(url, imageBytes) { mutableStateOf(false) }

    LaunchedEffect(url, imageBytes) {
        when {
            imageBytes != null -> {
                bitmap = decodeImageBitmap(imageBytes)
            }
            !url.isNullOrBlank() -> {
                val cached = cachedImage(url)
                if (cached != null) {
                    bitmap = cached
                } else {
                    bitmap = null
                    loading = true
                    try {
                        val bytes = KtorClient.httpClient.get(url).body<ByteArray>()
                        val bmp = decodeImageBitmap(bytes)
                        if (bmp != null) {
                            cacheImage(url, bmp)
                            bitmap = bmp
                        }
                    } catch (_: Exception) {
                        bitmap = null
                    } finally {
                        loading = false
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier.background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        when {
            bitmap != null -> {
                Image(
                    bitmap = bitmap!!,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
            loading -> {
                // Keep the surface from looking broken while bytes are in flight: for avatars
                // (showInitial) hold the initial and float a subtle spinner over it; otherwise
                // just show the spinner.
                if (showInitial && initial.isNotBlank()) {
                    Text(
                        text = initial.first().uppercaseChar().toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = tint
                    )
                }
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = tint,
                    strokeWidth = 2.dp
                )
            }
            showInitial && initial.isNotBlank() -> {
                Text(
                    text = initial.first().uppercaseChar().toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = tint
                )
            }
            else -> {
                Icon(
                    imageVector = TablerIcons.Photo,
                    contentDescription = contentDescription,
                    tint = tint.copy(alpha = 0.5f)
                )
            }
        }
    }
}
