package org.awi.fitness.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Check
import compose.icons.tablericons.MessageCircle
import org.awi.fitness.theme.GreenAccent
import org.jetbrains.compose.resources.painterResource
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarOnboardingBottomSheet(
    onSelectAvatar: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .height(500.dp) // Fixed height for impact
        ) {
            // Background Image with Overlay
            Image(
                painter = painterResource(Res.drawable.background1),
                contentDescription = "Onboarding Background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Dark overlay for text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.4f),
                                Color.Black.copy(alpha = 0.6f)
                            )
                        )
                    )
            )

            // Content
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                // Icon/Hero Image Placeholder
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = GreenAccent.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = TablerIcons.MessageCircle,
                        contentDescription = null,
                        tint = GreenAccent,
                        modifier = Modifier.size(64.dp)
                    )
                }

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Meet Your Fitness Buddy!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "A personal companion to keep you motivated, track your mood, and celebrate your wins.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = onSelectAvatar,
                    modifier = Modifier
                        .width(280.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenAccent,
                        contentColor = androidx.compose.ui.graphics.Color.Black
                    )
                ) {
                    Text(
                        text = "Choose Your Buddy",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                
                androidx.compose.material3.TextButton(onClick = onDismiss) {
                    Text("Maybe Later", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }
    }
}

