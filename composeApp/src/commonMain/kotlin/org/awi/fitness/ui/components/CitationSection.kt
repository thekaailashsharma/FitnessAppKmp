package org.awi.fitness.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import compose.icons.TablerIcons
import compose.icons.tablericons.ExternalLink
import org.awi.fitness.data.StringKey
import org.awi.fitness.theme.GreenAccent
import org.awi.fitness.utils.Citation
import org.awi.fitness.utils.Citations
import org.awi.fitness.viewmodel.LanguageViewModel

@Composable
fun CitationSection(
    citations: List<Citation>,
    languageViewModel: LanguageViewModel,
    modifier: Modifier = Modifier,
    showDisclaimer: Boolean = true
) {
    var showCitations by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    Column(modifier = modifier) {
        TextButton(
            onClick = { showCitations = !showCitations },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = languageViewModel.getString(
                    if (showCitations) StringKey.HIDE_CITATIONS else StringKey.VIEW_CITATIONS
                ),
                color = GreenAccent
            )
        }

        AnimatedVisibility(
            visible = showCitations,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = languageViewModel.getString(StringKey.MEDICAL_CITATIONS),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    citations.forEach { citation ->
                        Column {
                            Text(
                                text = citation.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val annotatedString = buildAnnotatedString {
                                    pushStyle(
                                        SpanStyle(
                                            color = GreenAccent,
                                            textDecoration = TextDecoration.Underline
                                        )
                                    )
                                    append("View Source")
                                    pop()
                                }
                                
                                ClickableText(
                                    text = annotatedString,
                                    onClick = { uriHandler.openUri(citation.sourceUrl) },
                                    style = MaterialTheme.typography.bodySmall
                                )
                                
                                Icon(
                                    imageVector = TablerIcons.ExternalLink,
                                    contentDescription = "External Link",
                                    tint = GreenAccent,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }

                    if (showDisclaimer) {
                        Column {
                            Text(
                                text = Citations.MEDICAL_DISCLAIMER.text,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val annotatedString = buildAnnotatedString {
                                    pushStyle(
                                        SpanStyle(
                                            color = GreenAccent,
                                            textDecoration = TextDecoration.Underline
                                        )
                                    )
                                    append("Learn More")
                                    pop()
                                }
                                
                                ClickableText(
                                    text = annotatedString,
                                    onClick = { uriHandler.openUri(Citations.MEDICAL_DISCLAIMER.sourceUrl) },
                                    style = MaterialTheme.typography.bodySmall
                                )
                                
                                Icon(
                                    imageVector = TablerIcons.ExternalLink,
                                    contentDescription = "External Link",
                                    tint = GreenAccent,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
} 