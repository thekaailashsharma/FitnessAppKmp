package org.awi.fitness.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.awi.fitness.data.UserSettings
import org.awi.fitness.data.AvatarData
import org.jetbrains.compose.resources.painterResource
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.*

/**
 * Component that displays the avatar image for the fitness buddy
 * Loads from Compose Resources with emoji fallback
 */
@Composable
fun AvatarImage(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    mood: org.awi.fitness.model.AvatarMood = org.awi.fitness.model.AvatarMood.HAPPY,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    imageUrl: String? = null
) {
    val userSettings = UserSettings.getInstance()
    val selectedAvatarId = userSettings.selectedAvatarId
    
    // Resolve resource name: priority to passed imageUrl, then lookup from selected ID
    val resourceName = imageUrl ?: selectedAvatarId?.let { id ->
        AvatarData.avatars.find { it.id == id }?.imageUrl
    }
    
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (resourceName != null) {
            // Map resource name to actual resource
            val drawableResource = when (resourceName) {
                "avatar1" -> Res.drawable.avatar1
                "avatar2" -> Res.drawable.avatar2
                "avatar3" -> Res.drawable.avatar3
                "avatar4" -> Res.drawable.avatar4
                "avatar5" -> Res.drawable.avatar5
                "avatar6" -> Res.drawable.avatar6
                else -> null
            }
            
            if (drawableResource != null) {
                Image(
                    painter = painterResource(drawableResource),
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Fallback to emoji if resource not found
                showEmojiFallback(mood)
            }
        } else {
            // Use emoji characters for different moods (fallback)
            showEmojiFallback(mood)
        }
    }
}

@Composable
private fun showEmojiFallback(mood: org.awi.fitness.model.AvatarMood) {
    val emoji = when (mood) {
        org.awi.fitness.model.AvatarMood.HAPPY -> "😊"
        org.awi.fitness.model.AvatarMood.ENCOURAGING -> "💪"
        org.awi.fitness.model.AvatarMood.CALM -> "😌"
        org.awi.fitness.model.AvatarMood.CONCERNED -> "🤔"
        org.awi.fitness.model.AvatarMood.EXCITED -> "🎉"
    }
    
    Text(
        text = emoji,
        style = MaterialTheme.typography.headlineLarge,
        textAlign = TextAlign.Center
    )
}
