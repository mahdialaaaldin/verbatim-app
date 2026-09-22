package com.verbatim.studio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verbatim.studio.theme.*
import kotlin.math.ceil

@Composable
fun InputPanel(
    text: String,
    onTextChanged: (String) -> Unit,
    onPaste: () -> Unit,
    onClear: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDarkTheme) SurfaceCardDark.copy(alpha = 0.45f) else SurfaceLight
    val cardBorder = if (isDarkTheme) BorderDarkSubtle.copy(alpha = 0.4f) else BorderLight
    val headerFooterBg = if (isDarkTheme) SurfaceDark.copy(alpha = 0.3f) else SurfaceVariantLight.copy(alpha = 0.5f)

    // Calculate Statistics
    val charCount = text.length
    val trimmed = text.trim()
    val wordCount = if (trimmed.isEmpty()) 0 else trimmed.split(Regex("\\s+")).size
    val readingMinutes = ceil(wordCount / 200.0).toInt().coerceAtLeast(0)

    val complexity = if (wordCount > 40) {
        val nonWhitespaceChars = text.replace(Regex("\\s+"), "").length
        val avgWordLength = nonWhitespaceChars.toDouble() / wordCount
        when {
            avgWordLength > 5.8 -> "Advanced"
            avgWordLength > 4.6 -> "Medium"
            else -> "Easy"
        }
    } else {
        "Easy"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerFooterBg)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Keyboard,
                        contentDescription = null,
                        tint = if (isDarkTheme) TextMutedDark else TextMutedLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "ORIGINAL TEXT",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onPaste,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste from Clipboard",
                        tint = IndigoPrimary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            HorizontalDivider(color = cardBorder)

            // Text Input Body
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                TextField(
                    value = text,
                    onValueChange = onTextChanged,
                    placeholder = {
                        Text(
                            text = "Type or paste text here...",
                            color = if (isDarkTheme) TextMutedDark else TextMutedLight,
                            fontSize = 14.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight,
                        unfocusedTextColor = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
                    ),
                    modifier = Modifier.fillMaxSize()
                )
            }

            HorizontalDivider(color = cardBorder)

            // Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerFooterBg)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$charCount chars",
                        fontSize = 11.sp,
                        color = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight
                    )
                    Text(
                        text = "•",
                        fontSize = 11.sp,
                        color = if (isDarkTheme) TextMutedDark else TextMutedLight
                    )
                    Text(
                        text = "$wordCount words",
                        fontSize = 11.sp,
                        color = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight
                    )
                }

                TextButton(
                    onClick = onClear,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Clear",
                        tint = RoseRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Clear",
                        color = RoseRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
