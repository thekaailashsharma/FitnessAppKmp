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
    var bitmap by remember(url, imageBytes) { mutableStateOf<ImageBitmap?>(null) }
    var loading by remember(url, imageBytes) { mutableStateOf(false) }

    LaunchedEffect(url, imageBytes) {
        bitmap = null
        when {
            imageBytes != null -> {
                bitmap = decodeImageBitmap(imageBytes)
            }
            !url.isNullOrBlank() -> {
                loading = true
                try {
                    val bytes = KtorClient.httpClient.get(url).body<ByteArray>()
                    bitmap = decodeImageBitmap(bytes)
                } catch (_: Exception) {
                    bitmap = null
                } finally {
                    loading = false
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
