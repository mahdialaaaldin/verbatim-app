package com.verbatim.studio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verbatim.studio.model.Preset
import com.verbatim.studio.theme.*
import kotlinx.coroutines.launch

@Composable
fun PresetCarousel(
    builtInPresets: List<Preset>,
    customPresets: List<Preset>,
    isDarkTheme: Boolean,
    onSelectPreset: (Preset) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val allPresets = builtInPresets + customPresets

    Column(modifier = modifier.fillMaxWidth()) {
        // Top row: Label & Scroll buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = IndigoPrimary,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "ACTION PRESETS",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDarkTheme) TextMutedDark else TextMutedLight,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = {
                        scope.launch {
                            val current = listState.firstVisibleItemIndex
                            listState.animateScrollToItem(maxOf(0, current - 2))
                        }
                    },
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isDarkTheme) SurfaceVariantDark.copy(alpha = 0.6f) else SurfaceVariantLight)
                        .border(1.dp, if (isDarkTheme) BorderDark else BorderLight, RoundedCornerShape(6.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronLeft,
                        contentDescription = "Scroll Left",
                        tint = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight,
                        modifier = Modifier.size(14.dp)
                    )
                }

                IconButton(
                    onClick = {
                        scope.launch {
                            val current = listState.firstVisibleItemIndex
                            listState.animateScrollToItem(minOf(allPresets.size - 1, current + 2))
                        }
                    },
                    modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isDarkTheme) SurfaceVariantDark.copy(alpha = 0.6f) else SurfaceVariantLight)
                        .border(1.dp, if (isDarkTheme) BorderDark else BorderLight, RoundedCornerShape(6.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Scroll Right",
                        tint = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Carousel List
        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(allPresets, key = { it.id }) { preset ->
                PresetChip(
                    preset = preset,
                    isDarkTheme = isDarkTheme,
                    onClick = { onSelectPreset(preset) }
                )
            }
        }
    }
}

@Composable
private fun PresetChip(
    preset: Preset,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val bg = if (preset.isCustom) {
        if (isDarkTheme) FuchsiaAccent.copy(alpha = 0.12f) else FuchsiaAccent.copy(alpha = 0.08f)
    } else {
        if (isDarkTheme) preset.darkBg else preset.lightBg
    }

    val border = if (preset.isCustom) {
        if (isDarkTheme) FuchsiaAccent.copy(alpha = 0.35f) else FuchsiaAccent.copy(alpha = 0.25f)
    } else {
        if (isDarkTheme) preset.darkBorder else preset.lightBorder
    }

    val textColor = if (preset.isCustom) {
        if (isDarkTheme) FuchsiaAccent else FuchsiaDark
    } else {
        preset.textColor
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = getPresetIcon(preset.id, preset.isCustom),
            contentDescription = null,
            tint = textColor,
            modifier = Modifier.size(14.dp)
        )

        Text(
            text = preset.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )

        if (preset.isCustom) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isDarkTheme) FuchsiaAccent.copy(alpha = 0.25f) else FuchsiaAccent.copy(alpha = 0.15f))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "CUSTOM",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

private fun getPresetIcon(id: String, isCustom: Boolean): ImageVector {
    if (isCustom) return Icons.Default.Tune
    return when (id) {
        "improve" -> Icons.Default.AutoFixHigh
        "professional" -> Icons.Default.Work
        "casual" -> Icons.AutoMirrored.Default.Chat
        "summarize" -> Icons.Default.Compress
        "bullet" -> Icons.Default.FormatListBulleted
        "expand" -> Icons.Default.OpenWith
        "sarcastic" -> Icons.Default.Face
        "prompt" -> Icons.Default.SmartToy
        else -> Icons.Default.AutoFixHigh
    }
}
