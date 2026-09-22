package com.verbatim.studio.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Difference
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verbatim.studio.diff.WordDiffHelper
import com.verbatim.studio.theme.*
import kotlin.math.ceil

@Composable
fun OutputPanel(
    rawInputText: String,
    rawOutputText: String,
    activePresetName: String?,
    isLoading: Boolean,
    isSpeaking: Boolean,
    showDiff: Boolean,
    onToggleDiff: () -> Unit,
    onToggleSpeak: () -> Unit,
    onCopy: () -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDarkTheme) SurfaceCardDark.copy(alpha = 0.45f) else SurfaceLight
    val cardBorder = if (isDarkTheme) BorderDarkSubtle.copy(alpha = 0.4f) else BorderLight
    val headerFooterBg = if (isDarkTheme) SurfaceDark.copy(alpha = 0.3f) else SurfaceVariantLight.copy(alpha = 0.5f)

    // Statistics
    val trimmed = rawOutputText.trim()
    val wordCount = if (trimmed.isEmpty() || rawOutputText == "Result will appear here...") 0 else trimmed.split(Regex("\\s+")).size
    val readingMinutes = ceil(wordCount / 200.0).toInt().coerceAtLeast(0)

    val complexity = if (wordCount > 40) {
        val nonWhitespaceChars = rawOutputText.replace(Regex("\\s+"), "").length
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
        Box(modifier = Modifier.fillMaxSize()) {
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
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = IndigoPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "OUTPUT",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight,
                            fontWeight = FontWeight.Bold
                        )

                        if (!activePresetName.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(IndigoPrimary.copy(alpha = 0.15f))
                                    .border(1.dp, IndigoPrimary.copy(alpha = 0.25f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Preset: $activePresetName",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IndigoPrimary
                                )
                            }
                        }
                    }

                    // Action Buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Diff Button
                        IconButton(
                            onClick = onToggleDiff,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (showDiff) IndigoPrimary else (if (isDarkTheme) SurfaceVariantDark.copy(alpha = 0.6f) else SurfaceVariantLight)
                                )
                                .border(1.dp, if (showDiff) IndigoPrimary else cardBorder, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Difference,
                                contentDescription = "Toggle Diff",
                                tint = if (showDiff) Color.White else (if (isDarkTheme) TextSecondaryDark else TextSecondaryLight),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // Speak Button
                        IconButton(
                            onClick = onToggleSpeak,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSpeaking) IndigoPrimary.copy(alpha = 0.2f) else (if (isDarkTheme) SurfaceVariantDark.copy(alpha = 0.6f) else SurfaceVariantLight)
                                )
                                .border(1.dp, if (isSpeaking) IndigoPrimary else cardBorder, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.StopCircle else Icons.Default.VolumeUp,
                                contentDescription = "Speak Text",
                                tint = if (isSpeaking) IndigoPrimary else (if (isDarkTheme) TextSecondaryDark else TextSecondaryLight),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        // Copy Button
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDarkTheme) SurfaceVariantDark.copy(alpha = 0.6f) else SurfaceVariantLight)
                                .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Output",
                                tint = IndigoPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = cardBorder)

                // Output Body
                val scrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(14.dp)
                ) {
                    if (rawOutputText.isEmpty() || rawOutputText == "Result will appear here...") {
                        Text(
                            text = "Result will appear here...",
                            fontStyle = FontStyle.Italic,
                            fontSize = 14.sp,
                            color = if (isDarkTheme) TextMutedDark else TextMutedLight
                        )
                    } else if (showDiff && rawInputText.isNotBlank()) {
                        val diffAnnotated = WordDiffHelper.buildDiffAnnotatedString(
                            oldStr = rawInputText,
                            newStr = rawOutputText,
                            isDark = isDarkTheme
                        )
                        Text(
                            text = diffAnnotated,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
                            )
                        )
                    } else {
                        Text(
                            text = rawOutputText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
                            )
                        )
                    }
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
                            text = "$readingMinutes min read",
                            fontSize = 11.sp,
                            color = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight
                        )
                        Text(
                            text = "•",
                            fontSize = 11.sp,
                            color = if (isDarkTheme) TextMutedDark else TextMutedLight
                        )
                        Text(
                            text = complexity,
                            fontSize = 11.sp,
                            color = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight
                        )
                    }

                    Text(
                        text = "PROCESSED",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = if (isDarkTheme) TextMutedDark else TextMutedLight
                    )
                }
            }

            // Loading Overlay with Dual Concentric Spinners
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isDarkTheme) Color(0xFF070A13).copy(alpha = 0.85f) else Color.White.copy(alpha = 0.85f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "spinners")
                        val angleForward by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "spin_forward"
                        )
                        val angleBackward by infiniteTransition.animateFloat(
                            initialValue = 360f,
                            targetValue = 0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "spin_backward"
                        )

                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { 0.7f },
                                modifier = Modifier
                                    .size(48.dp),
                                color = IndigoPrimary,
                                strokeWidth = 3.5.dp,
                                trackColor = IndigoPrimary.copy(alpha = 0.15f),
                            )
                            CircularProgressIndicator(
                                progress = { 0.5f },
                                modifier = Modifier
                                    .size(32.dp),
                                color = FuchsiaAccent,
                                strokeWidth = 3.dp,
                                trackColor = FuchsiaAccent.copy(alpha = 0.15f),
                            )
                        }

                        Text(
                            text = "GENERATING MAGIC...",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = IndigoPrimary
                        )
                    }
                }
            }
        }
    }
}
