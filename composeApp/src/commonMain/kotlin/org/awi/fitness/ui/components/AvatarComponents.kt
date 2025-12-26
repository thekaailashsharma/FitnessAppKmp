package org.awi.fitness.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.Blockquote
import compose.icons.tablericons.MessageCircle
import compose.icons.tablericons.Flame
import org.awi.fitness.model.AvatarMood
import org.awi.fitness.model.ConversationTopic
import org.awi.fitness.model.MotivationalQuote
import org.awi.fitness.theme.GreenAccent
import org.awi.fitness.theme.TextGray

/**
 * Avatar component that displays different expressions based on mood
 */
@Composable
fun Avatar(
    mood: AvatarMood,
    modifier: Modifier = Modifier,
    isAnimating: Boolean = false,
    size: androidx.compose.ui.unit.Dp = 100.dp,
    avatarImageUrl: String? = null
) {
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.05f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Box(
        modifier = modifier
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        AvatarImage(
            mood = mood,
            size = size,
            imageUrl = avatarImageUrl
        )
    }
}

/**
 * Message bubble for avatar conversation
 */
@Composable
fun MessageBubble(
    message: String,
    isFromAvatar: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromAvatar) Arrangement.Start else Arrangement.End
    ) {
        if (isFromAvatar) {
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(0.75f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(0.75f),
                colors = CardDefaults.cardColors(
                    containerColor = GreenAccent,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
            ) {
                Text(
                    text = message,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

/**
 * Suggested response chip that user can click
 */
@Composable
fun SuggestedResponseChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .padding(4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Topic card for conversation topics
 */
@Composable
fun TopicCard(
    topic: ConversationTopic,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = TablerIcons.MessageCircle,
                    contentDescription = null,
                    tint = GreenAccent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = topic.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = topic.description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray
            )
        }
    }
}

/**
 * Quote card for motivational quotes
 */
@Composable
fun QuoteCard(
    quote: MotivationalQuote,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = TablerIcons.Blockquote,
                contentDescription = null,
                tint = GreenAccent,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "\"${quote.content}\"",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "— ${quote.author}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextGray,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

/**
 * Action button for avatar conversation
 */
@Composable
fun AvatarActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    isSelected: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(8.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isSelected) GreenAccent else MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) GreenAccent else Color.Transparent,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) GreenAccent else TextGray
        )
    }
}

/**
 * Typing indicator for when the avatar is typing
 */
@Composable
fun TypingIndicator(modifier: Modifier = Modifier) {
    var dotPosition by remember { mutableStateOf(0) }
    
    Row(
        modifier = modifier
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0..2) {
            val scale by animateFloatAsState(
                targetValue = if (i == dotPosition) 1.5f else 1f,
                animationSpec = tween(300)
            )
            
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(GreenAccent)
            )
        }
        
        // Animate the dots in sequence
        androidx.compose.runtime.LaunchedEffect(Unit) {
            while (true) {
                kotlinx.coroutines.delay(300)
                dotPosition = (dotPosition + 1) % 3
            }
        }
    }
}

/**
 * Action bar for avatar conversation
 */
@Composable
fun AvatarActionBar(
    onTopicsClick: () -> Unit,
    onQuotesClick: () -> Unit,
    showTopics: Boolean,
    showQuotes: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AvatarActionButton(
            icon = TablerIcons.MessageCircle,
            label = "Topics",
            onClick = onTopicsClick,
            isSelected = showTopics
        )
        
        AvatarActionButton(
            icon = TablerIcons.Blockquote,
            label = "Quotes",
            onClick = onQuotesClick,
            isSelected = showQuotes
        )
        
        AvatarActionButton(
            icon = TablerIcons.Flame,
            label = "Insights",
            onClick = { /* To be implemented */ }
        )
    }
}
