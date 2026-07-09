package org.awi.fitness.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import fitnessappkmp.composeapp.generated.resources.Res
import io.github.alexzhirkevich.compottie.Compottie
import io.github.alexzhirkevich.compottie.LottieCompositionSpec
import io.github.alexzhirkevich.compottie.rememberLottieComposition
import io.github.alexzhirkevich.compottie.rememberLottiePainter

/**
 * Lightweight Lottie player. Loads a `.json` animation bundled under composeResources/files
 * and renders it. Reserve for hand-drawn micro-moments (streak flame, goal confetti, empty/
 * loading/success, level-up) — everything else uses tasteful Compose animation.
 *
 * Usage: LottieAnim("lottie_flame.json", Modifier.size(40.dp))
 */
@Composable
fun LottieAnim(
    file: String,
    modifier: Modifier = Modifier,
    iterations: Int = Compottie.IterateForever,
    contentScale: ContentScale = ContentScale.Fit,
) {
    val composition by rememberLottieComposition {
        LottieCompositionSpec.JsonString(
            Res.readBytes("files/$file").decodeToString()
        )
    }
    Image(
        painter = rememberLottiePainter(
            composition = composition,
            iterations = iterations,
        ),
        contentDescription = null,
        modifier = modifier,
        contentScale = contentScale,
    )
}
