package org.awi.fitness.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import io.ktor.client.call.body
import io.ktor.client.request.get
import org.awi.fitness.network.KtorClient
import org.awi.fitness.utils.decodeImageBitmap
import org.awi.fitness.viewmodel.BackdropChoice
import org.awi.fitness.viewmodel.HomeBackdropCatalog
import org.awi.fitness.viewmodel.LocalBackdropViewModel
import org.jetbrains.compose.resources.painterResource

/**
 * Resolves the current [BackdropChoice] for [surface] from the composition-local
 * BackdropViewModel. Recomputes when the selection map changes.
 */
@Composable
fun rememberBackdrop(surface: String): BackdropChoice {
    val viewModel = LocalBackdropViewModel.current
    // Observe the selection flow so the backdrop updates after a refresh.
    val selection by viewModel.selection.collectAsState()
    // The user's Home backdrop pick (from settings) wins for the home surface, updating live.
    if (surface == "home") {
        val homeId by viewModel.homeBackdropSelection.collectAsState()
        HomeBackdropCatalog.resFor(homeId)?.let { return BackdropChoice.Bundled(it) }
    }
    return selection[surface] ?: viewModel.choiceFor(surface)
}

/**
 * Renders the backdrop for [surface]: a remote image (gracefully falling back to
 * the bundled drawable on any failure) or a bundled drawable. Always fills its
 * bounds with [ContentScale.Crop].
 */
@Composable
fun BackdropImage(surface: String, modifier: Modifier = Modifier) {
    when (val choice = rememberBackdrop(surface)) {
        is BackdropChoice.Remote -> {
            var bitmap by remember(choice.url) { mutableStateOf<ImageBitmap?>(null) }

            LaunchedEffect(choice.url) {
                bitmap = null
                if (choice.url.isBlank()) return@LaunchedEffect
                try {
                    val bytes = KtorClient.httpClient.get(choice.url).body<ByteArray>()
                    bitmap = decodeImageBitmap(bytes)
                } catch (_: Exception) {
                    bitmap = null
                }
            }

            val loaded = bitmap
            when {
                loaded != null -> {
                    Image(
                        bitmap = loaded,
                        contentDescription = null,
                        modifier = modifier,
                        contentScale = ContentScale.Crop
                    )
                }
                // Until the remote image resolves (or if it fails), show the
                // bundled fallback so there is never an empty surface.
                else -> {
                    Image(
                        painter = painterResource(choice.fallback),
                        contentDescription = null,
                        modifier = modifier,
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
        is BackdropChoice.Bundled -> {
            Image(
                painter = painterResource(choice.res),
                contentDescription = null,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        }
    }
}
