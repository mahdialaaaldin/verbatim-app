package com.verbatim.studio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
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
    onSampleText: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDarkTheme) SurfaceCardDark.copy(alpha = 0.55f) else SurfaceLight
    val cardBorder = if (isDarkTheme) BorderDarkSubtle.copy(alpha = 0.5f) else BorderLight
    val headerFooterBg = if (isDarkTheme) SurfaceDark.copy(alpha = 0.45f) else SurfaceVariantLight.copy(alpha = 0.6f)

    // Calculate Statistics
    val charCount = text.length
    val trimmed = text.trim()
    val wordCount = if (trimmed.isEmpty()) 0 else trimmed.split(Regex("\\s+")).size
    val readingMinutes = ceil(wordCount / 200.0).toInt().coerceAtLeast(1)

    val complexity = if (wordCount > 30) {
        val nonWhitespaceChars = text.replace(Regex("\\s+"), "").length
        val avgWordLength = nonWhitespaceChars.toDouble() / wordCount
        when {
            avgWordLength > 5.8 -> "Advanced"
            avgWordLength > 4.6 -> "Medium"
            else -> "Easy"
        }
    } else {
        "Simple"
    }

    val focusRequester = remember { FocusRequester() }
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        shadowElevation = if (isDarkTheme) 0.dp else 2.dp
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
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(IndigoPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = IndigoPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "ORIGINAL TEXT",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Quick Sample Text Button (if input empty)
                    if (text.isEmpty() && onSampleText != null) {
                        FilledTonalButton(
                            onClick = onSampleText,
                            contentPadding = PaddingValues(horizontal = 9.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isDarkTheme) SurfaceVariantDark else SurfaceVariantLight,
                                contentColor = IndigoPrimary
                            ),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Try Sample",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Paste Button
                    IconButton(
                        onClick = onPaste,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDarkTheme) SurfaceVariantDark.copy(alpha = 0.6f) else SurfaceVariantLight)
                            .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste from Clipboard",
                            tint = IndigoPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Clear Button (in header if text is not empty)
                    if (text.isNotEmpty()) {
                        IconButton(
                            onClick = onClear,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(RoseRed.copy(alpha = 0.1f))
                                .border(1.dp, RoseRed.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear Input",
                                tint = RoseRed,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = cardBorder, thickness = 1.dp)

            // Text Input Body with tap-to-focus
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        focusRequester.requestFocus()
                    }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                TextField(
                    value = text,
                    onValueChange = onTextChanged,
                    placeholder = {
                        Text(
                            text = "Write or paste the text you want to transform...",
                            color = if (isDarkTheme) TextMutedDark else TextMutedLight,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = IndigoPrimary,
                        selectionColors = TextSelectionColors(
                            handleColor = IndigoPrimary,
                            backgroundColor = IndigoPrimary.copy(alpha = 0.35f)
                        ),
                        focusedTextColor = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight,
                        unfocusedTextColor = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight,
                        focusedPlaceholderColor = if (isDarkTheme) TextMutedDark else TextMutedLight,
                        unfocusedPlaceholderColor = if (isDarkTheme) TextMutedDark else TextMutedLight
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight,
                        lineHeight = 22.sp,
                        fontSize = 14.5.sp
                    ),
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(focusRequester)
                )
            }

            HorizontalDivider(color = cardBorder, thickness = 1.dp)

            // Footer Statistics & Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerFooterBg)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
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
                        fontWeight = FontWeight.Medium,
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
                        fontWeight = FontWeight.Medium,
                        color = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight
                    )
                    if (wordCount > 0) {
                        Text(
                            text = "•",
                            fontSize = 11.sp,
                            color = if (isDarkTheme) TextMutedDark else TextMutedLight
                        )
                        Text(
                            text = "$readingMinutes min read",
                            fontSize = 11.sp,
                            color = if (isDarkTheme) TextMutedDark else TextMutedLight
                        )
                    }
                }

                if (text.isNotEmpty()) {
                    TextButton(
                        onClick = onClear,
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear",
                            tint = RoseRed,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "Clear",
                            color = RoseRed,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Text(
                        text = "READY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = if (isDarkTheme) TextMutedDark else TextMutedLight
                    )
                }
            }
        }
    }
}
