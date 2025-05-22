package org.awi.fitness.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    color: Color = Color.Unspecified,
    onLinkClick: ((String) -> Unit)? = null
) {
    // Clean the markdown text by removing all single stars
    val cleanMarkdown = markdown.replace("\\*(?!\\*)".toRegex(), "")
    val sections = cleanMarkdown.split("\n\n")
    
    Column(modifier = modifier.fillMaxWidth()) {
        sections.forEach { section ->
            when {
                // Headers
                section.startsWith("## ") -> {
                    Text(
                        text = section.removePrefix("## ").trim(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                // Lists
                section.contains("\n- ") -> {
                    val items = section.split("\n- ")
                    items.forEachIndexed { index, item ->
                        if (index > 0 || !item.isBlank()) { // Skip first empty item if exists
                            Text(
                                text = buildAnnotatedString {
                                    append("• ")
                                    append(item.trim())
                                },
                                style = style,
                                color = color,
                                modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                            )
                        }
                    }
                }
                
                // Bold text between **
                section.contains("**") -> {
                    val parts = section.split("**")
                    Text(
                        text = buildAnnotatedString {
                            parts.forEachIndexed { index, part ->
                                if (index % 2 == 0) {
                                    append(part)
                                } else {
                                    pushStyle(style.copy(fontWeight = FontWeight.Bold).toSpanStyle())
                                    append(part)
                                    pop()
                                }
                            }
                        },
                        style = style,
                        color = color,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                
                // Regular paragraphs
                else -> {
                    Text(
                        text = section.trim(),
                        style = style,
                        color = color,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}