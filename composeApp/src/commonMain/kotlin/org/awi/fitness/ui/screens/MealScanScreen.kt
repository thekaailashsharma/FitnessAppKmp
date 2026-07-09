package org.awi.fitness.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import org.awi.fitness.theme.OnGold
import org.jetbrains.compose.resources.painterResource
import fitnessappkmp.composeapp.generated.resources.Res
import fitnessappkmp.composeapp.generated.resources.card_fuel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowLeft
import compose.icons.tablericons.Camera
import compose.icons.tablericons.Check
import compose.icons.tablericons.Flame
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.awi.fitness.data.UserSettings
import org.awi.fitness.repository.GeminiRepository
import org.awi.fitness.repository.ScannedMeal
import org.awi.fitness.theme.GoldBright
import org.awi.fitness.theme.GoldPrimary
import org.awi.fitness.theme.Tajly
import org.awi.fitness.theme.TajlyTheme
import org.awi.fitness.ui.components.AuroraBackground
import org.awi.fitness.ui.components.GlassCard
import org.awi.fitness.ui.components.GoldButton
import org.awi.fitness.ui.components.LottieAnim
import org.awi.fitness.ui.components.StatRing
import org.awi.fitness.utils.currentTimeMillis
import org.awi.fitness.utils.decodeImageBitmap
import org.awi.fitness.utils.rememberImagePickerLauncher

/** One journaled AI meal-scan result. Local-only; stored as a JSON array in scanLog. */
@Serializable
private data class ScanLogEntry(
    val name: String,
    val calories: Int,
    val protein: Int,
    val carbs: Int,
    val fat: Int,
    val timestamp: Long
)

private sealed interface ScanState {
    data object Idle : ScanState
    data object Analyzing : ScanState
    data class Success(val meal: ScannedMeal, val saved: Boolean = false) : ScanState
    data object Error : ScanState
}

/**
 * AI meal-scan. Point the phone at a plate → the photo is sent to Gemini vision →
 * real estimated nutrition comes back and can be saved to the local journal.
 */
class MealScanScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val scope = rememberCoroutineScope()
        val repository = remember { GeminiRepository() }
        val userSettings = remember { UserSettings.getInstance() }
        val json = remember { Json { ignoreUnknownKeys = true; isLenient = true } }

        var state by remember { mutableStateOf<ScanState>(ScanState.Idle) }
        var pickedImage by remember { mutableStateOf<ByteArray?>(null) }

        val imagePicker = rememberImagePickerLauncher { bytes ->
            if (bytes == null) return@rememberImagePickerLauncher
            pickedImage = bytes
            state = ScanState.Analyzing
            scope.launch {
                val result = repository.analyzeMealPhoto(bytes)
                state = result.fold(
                    onSuccess = { ScanState.Success(it) },
                    onFailure = { ScanState.Error },
                )
            }
        }

        AuroraBackground(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
            ) {
                Spacer(Modifier.height(16.dp))
                TopBar(onBack = { navigator.pop() })
                Spacer(Modifier.height(8.dp))

                Text(
                    text = "Meal Scan",
                    color = TajlyTheme.colors.textHi,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "Point at your plate — AI reads the nutrition.",
                    color = TajlyTheme.colors.textMid,
                    style = MaterialTheme.typography.bodyMedium,
                )

                Spacer(Modifier.height(20.dp))

                Viewfinder(
                    imageBytes = pickedImage,
                    analyzing = state is ScanState.Analyzing,
                )

                Spacer(Modifier.height(24.dp))

                when (val s = state) {
                    ScanState.Idle -> IdleActions(onPick = { imagePicker.launch() })
                    ScanState.Analyzing -> AnalyzingBlock()
                    ScanState.Error -> ErrorBlock(onRetry = { imagePicker.launch() })
                    is ScanState.Success -> ResultBlock(
                        meal = s.meal,
                        saved = s.saved,
                        onSave = {
                            val entry = ScanLogEntry(
                                name = s.meal.name,
                                calories = s.meal.calories,
                                protein = s.meal.protein,
                                carbs = s.meal.carbs,
                                fat = s.meal.fat,
                                timestamp = currentTimeMillis(),
                            )
                            val current = try {
                                json.decodeFromString<List<ScanLogEntry>>(userSettings.scanLog)
                            } catch (_: Exception) {
                                emptyList()
                            }
                            userSettings.scanLog = json.encodeToString(current + entry)
                            state = s.copy(saved = true)
                        },
                        onScanAnother = {
                            pickedImage = null
                            state = ScanState.Idle
                        },
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun TopBar(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .androidxClickable(onBack),
        contentAlignment = Alignment.Center,
    ) {
        GlassCard(modifier = Modifier.fillMaxSize(), shape = RoundedCornerShape(14.dp)) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = TablerIcons.ArrowLeft,
                    contentDescription = "Back",
                    tint = TajlyTheme.colors.textHi,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
    }
}

/** Cinematic viewfinder: rounded corner brackets + a gold scan line that sweeps while analyzing. */
@Composable
private fun Viewfinder(imageBytes: ByteArray?, analyzing: Boolean) {
    val transition = rememberInfiniteTransition(label = "scan")
    val scanY by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scanY",
    )

    val bitmap = remember(imageBytes) { imageBytes?.let { decodeImageBitmap(it) } }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        shape = RoundedCornerShape(28.dp),
        goldTint = true,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (bitmap != null) {
                // A real photo was picked — show it, with the scan sweep while analyzing.
                Image(
                    bitmap = bitmap,
                    contentDescription = "Selected meal",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(28.dp)),
                )
                if (analyzing) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        val y = size.height * scanY
                        drawRect(
                            brush = Brush.horizontalGradient(
                                listOf(Color.Transparent, GoldBright.copy(alpha = 0.9f), Color.Transparent),
                            ),
                            topLeft = Offset(0f, y - 2f),
                            size = androidx.compose.ui.geometry.Size(size.width, 4f),
                        )
                        drawRect(
                            brush = Brush.verticalGradient(
                                listOf(Color.Transparent, GoldBright.copy(alpha = 0.18f), Color.Transparent),
                                startY = y - 40f, endY = y + 40f,
                            ),
                            topLeft = Offset(0f, y - 40f),
                            size = androidx.compose.ui.geometry.Size(size.width, 80f),
                        )
                    }
                }
            } else {
                // Idle — a warm food backdrop hero (NOT a fake camera viewfinder).
                Image(
                    painter = painterResource(Res.drawable.card_fuel),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(28.dp)),
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.25f), Color.Black.copy(alpha = 0.72f)),
                            )
                        ),
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp),
                ) {
                    Text("🍽️", fontSize = 40.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Snap or pick a photo of your plate",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "AI reads the calories & macros for you.",
                        color = Color.White.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                // "AI" ribbon, top-right.
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(14.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Tajly.GoldGradient)
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                ) {
                    Text(
                        "AI POWERED",
                        color = OnGold,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
            }
        }
    }
}

@Composable
private fun IdleActions(onPick: () -> Unit) {
    GoldButton(
        text = "Take / choose a photo",
        onClick = onPick,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun AnalyzingBlock() {
    GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            LottieAnim("lottie_loading.json", Modifier.size(64.dp))
            Spacer(Modifier.width(14.dp))
            Text(
                text = "Reading your plate…",
                color = TajlyTheme.colors.textHi,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

@Composable
private fun ErrorBlock(onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
            Text(
                text = "Couldn't read that — try another photo",
                color = TajlyTheme.colors.textHi,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(20.dp),
            )
        }
        Spacer(Modifier.height(14.dp))
        GoldButton(text = "Try again", onClick = onRetry, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun ResultBlock(
    meal: ScannedMeal,
    saved: Boolean,
    onSave: () -> Unit,
    onScanAnother: () -> Unit,
) {
    AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
        Column {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                goldTint = true,
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = meal.name,
                        color = TajlyTheme.colors.textHi,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(18.dp))

                    StatRing(
                        progress = (meal.calories / 800f).coerceIn(0f, 1f),
                        diameter = 132.dp,
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = TablerIcons.Flame,
                                contentDescription = null,
                                tint = GoldBright,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = "${meal.calories}",
                                color = TajlyTheme.colors.textHi,
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "kcal",
                                color = TajlyTheme.colors.textMid,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        MacroPill("Protein", meal.protein, Tajly.Teal)
                        MacroPill("Carbs", meal.carbs, GoldPrimary)
                        MacroPill("Fat", meal.fat, Tajly.Coral)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (saved) {
                GlassCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = TablerIcons.Check,
                            contentDescription = null,
                            tint = GoldBright,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Saved to journal",
                            color = TajlyTheme.colors.textHi,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                GoldButton(
                    text = "Scan another",
                    onClick = onScanAnother,
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                GoldButton(
                    text = "Save to journal",
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
private fun MacroPill(label: String, grams: Int, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "${grams}g",
            color = accent,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            color = TajlyTheme.colors.textMid,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

// Single-purpose click helper (uses the explicitly-imported foundation.clickable).
private fun Modifier.androidxClickable(onClick: () -> Unit): Modifier =
    this.clickable(onClickLabel = null) { onClick() }
