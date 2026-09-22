package com.verbatim.studio.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verbatim.studio.diff.WordDiffHelper
import com.verbatim.studio.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    onUseAsInput: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDarkTheme) SurfaceCardDark.copy(alpha = 0.55f) else SurfaceLight
    val cardBorder = if (isDarkTheme) BorderDarkSubtle.copy(alpha = 0.5f) else BorderLight
    val headerFooterBg = if (isDarkTheme) SurfaceDark.copy(alpha = 0.45f) else SurfaceVariantLight.copy(alpha = 0.6f)

    val scope = rememberCoroutineScope()
    var justCopied by remember { mutableStateOf(false) }

    // Statistics
    val hasValidOutput = rawOutputText.isNotBlank() && rawOutputText != "Result will appear here..."
    val trimmed = rawOutputText.trim()
    val wordCount = if (!hasValidOutput) 0 else trimmed.split(Regex("\\s+")).size
    val readingMinutes = ceil(wordCount / 200.0).toInt().coerceAtLeast(1)

    val complexity = if (wordCount > 30) {
        val nonWhitespaceChars = rawOutputText.replace(Regex("\\s+"), "").length
        val avgWordLength = nonWhitespaceChars.toDouble() / wordCount
        when {
            avgWordLength > 5.8 -> "Advanced"
            avgWordLength > 4.6 -> "Medium"
            else -> "Easy"
        }
    } else {
        "Simple"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        shadowElevation = if (isDarkTheme) 0.dp else 2.dp
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
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(RoundedCornerShape(7.dp))
                                .background(IndigoPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = IndigoPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "AI OUTPUT",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
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
                                    text = activePresetName,
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
                        // Use as Input Button (iterative editing)
                        if (hasValidOutput && onUseAsInput != null) {
                            IconButton(
                                onClick = onUseAsInput,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDarkTheme) SurfaceVariantDark.copy(alpha = 0.6f) else SurfaceVariantLight)
                                    .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ArrowUpward,
                                    contentDescription = "Use as Input",
                                    tint = IndigoPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        // Diff Button
                        if (hasValidOutput && rawInputText.isNotBlank()) {
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
                        }

                        // Speak Button
                        if (hasValidOutput) {
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
                        }

                        // Share Button
                        if (hasValidOutput && onShare != null) {
                            IconButton(
                                onClick = onShare,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDarkTheme) SurfaceVariantDark.copy(alpha = 0.6f) else SurfaceVariantLight)
                                    .border(1.dp, cardBorder, RoundedCornerShape(8.dp))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share Output",
                                    tint = IndigoPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        // Copy Button with animated feedback
                        IconButton(
                            onClick = {
                                if (hasValidOutput) {
                                    onCopy()
                                    justCopied = true
                                    scope.launch {
                                        delay(1800)
                                        justCopied = false
                                    }
                                }
                            },
                            enabled = hasValidOutput,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (justCopied) EmeraldGreen.copy(alpha = 0.2f)
                                    else if (isDarkTheme) SurfaceVariantDark.copy(alpha = 0.6f) else SurfaceVariantLight
                                )
                                .border(
                                    1.dp,
                                    if (justCopied) EmeraldGreen else cardBorder,
                                    RoundedCornerShape(8.dp)
                                )
                        ) {
                            Icon(
                                imageVector = if (justCopied) Icons.Default.Done else Icons.Default.ContentCopy,
                                contentDescription = "Copy Output",
                                tint = if (justCopied) EmeraldGreen else (if (hasValidOutput) IndigoPrimary else (if (isDarkTheme) TextMutedDark else TextMutedLight)),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = cardBorder, thickness = 1.dp)

                // Output Body
                val scrollState = rememberScrollState()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                    if (!hasValidOutput) {
                        // Polished Empty State (Optimized for split view and compact phones)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(IndigoPrimary.copy(alpha = 0.08f))
                                    .border(1.dp, IndigoPrimary.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = IndigoPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Text(
                                text = "Ready to Transform",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
                            )

                            Text(
                                text = "Type or paste your text in the input panel, then choose any preset above to polish, rewrite, summarize, or expand.",
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center,
                                color = if (isDarkTheme) TextMutedDark else TextMutedLight,
                                modifier = Modifier.fillMaxWidth(0.9f),
                                lineHeight = 17.sp
                            )
                        }
                    } else if (showDiff && rawInputText.isNotBlank()) {
                        val diffAnnotated = WordDiffHelper.buildDiffAnnotatedString(
                            oldStr = rawInputText,
                            newStr = rawOutputText,
                            isDark = isDarkTheme
                        )
                        Text(
                            text = diffAnnotated,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight,
                                lineHeight = 24.sp,
                                fontSize = 14.5.sp
                            )
                        )
                    } else {
                        Text(
                            text = rawOutputText,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight,
                                lineHeight = 24.sp,
                                fontSize = 14.5.sp
                            )
                        )
                    }
                }

                HorizontalDivider(color = cardBorder, thickness = 1.dp)

                // Footer Statistics
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
                            text = if (hasValidOutput) "$wordCount words" else "0 words",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight
                        )
                        if (hasValidOutput) {
                            Text(
                                text = "•",
                                fontSize = 11.sp,
                                color = if (isDarkTheme) TextMutedDark else TextMutedLight
                            )
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
                    }

                    Text(
                        text = if (hasValidOutput) "ENHANCED" else "WAITING",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = if (hasValidOutput) IndigoPrimary else (if (isDarkTheme) TextMutedDark else TextMutedLight)
                    )
                }
            }

            // Modern Loading Overlay with Animated Pulsing Steps
            AnimatedVisibility(
                visible = isLoading,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            if (isDarkTheme) Color(0xFF070A13).copy(alpha = 0.88f) else Color.White.copy(alpha = 0.92f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse_spin")
                        val rotation by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = 360f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "spin"
                        )

                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(52.dp),
                                color = IndigoPrimary,
                                strokeWidth = 3.5.dp,
                                trackColor = IndigoPrimary.copy(alpha = 0.15f)
                            )
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = FuchsiaAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Polishing with Gemini AI...",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
                            )
                            Text(
                                text = "Crafting tone and refining vocabulary",
                                fontSize = 11.sp,
                                color = if (isDarkTheme) TextMutedDark else TextMutedLight
                            )
                        }
                    }
                }
            }
        }
    }
}
