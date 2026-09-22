package com.verbatim.studio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verbatim.studio.model.HistoryEntry
import com.verbatim.studio.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBottomSheet(
    history: List<HistoryEntry>,
    historyMode: String,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onRestore: (HistoryEntry) -> Unit,
    onCopyOriginal: (String) -> Unit,
    onCopyEnhanced: (String) -> Unit,
    onDelete: (Long) -> Unit,
    onClearAll: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val sheetBg = if (isDarkTheme) Color(0xFF0A0F1D) else SurfaceLight
    val cardBg = if (isDarkTheme) Color(0xFF0F172A).copy(alpha = 0.6f) else SurfaceVariantLight
    val cardBorder = if (isDarkTheme) BorderDark else BorderLight

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = sheetBg,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = IndigoPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Revision History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
                    )
                }

                if (historyMode != "disabled") {
                    TextButton(
                        onClick = onClearAll,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "CLEAR ALL",
                            color = RoseRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Incognito Banner
            if (historyMode == "incognito") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(PurpleAccent.copy(alpha = 0.1f))
                        .border(1.dp, PurpleAccent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = PurpleAccent,
                        modifier = Modifier.size(22.dp)
                    )
                    Column {
                        Text(
                            text = "Incognito Mode Active",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PurpleAccent
                        )
                        Text(
                            text = "Transformations are currently not recorded in history.",
                            fontSize = 11.sp,
                            color = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // History Disabled Card
            if (historyMode == "disabled") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(RoseRed.copy(alpha = 0.05f))
                        .border(1.dp, RoseRed.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Block,
                        contentDescription = null,
                        tint = RoseRed,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = "History is Disabled",
                        fontWeight = FontWeight.Bold,
                        color = RoseRed,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "AI transformations are not being logged. You can re-enable history logging in settings.",
                        fontSize = 12.sp,
                        color = if (isDarkTheme) TextMutedDark else TextMutedLight
                    )
                    Button(
                        onClick = onOpenSettings,
                        colors = ButtonDefaults.buttonColors(containerColor = RoseRed.copy(alpha = 0.15f)),
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Text(text = "Open Settings", color = RoseRed, fontSize = 12.sp)
                    }
                }
            } else if (history.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = if (isDarkTheme) TextMutedDark else TextMutedLight,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = "No previous logs saved yet",
                            fontSize = 12.sp,
                            color = if (isDarkTheme) TextMutedDark else TextMutedLight
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                ) {
                    items(history, key = { it.id }) { entry ->
                        HistoryCard(
                            entry = entry,
                            cardBg = cardBg,
                            cardBorder = cardBorder,
                            isDarkTheme = isDarkTheme,
                            onRestore = { onRestore(entry) },
                            onCopyOriginal = { onCopyOriginal(entry.input) },
                            onCopyEnhanced = { onCopyEnhanced(entry.output) },
                            onDelete = { onDelete(entry.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HistoryCard(
    entry: HistoryEntry,
    cardBg: Color,
    cardBorder: Color,
    isDarkTheme: Boolean,
    onRestore: () -> Unit,
    onCopyOriginal: () -> Unit,
    onCopyEnhanced: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(IndigoPrimary.copy(alpha = 0.12f))
                    .border(1.dp, IndigoPrimary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = entry.tone.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = IndigoPrimary
                )
            }

            Text(
                text = "${entry.date} ${entry.timestamp}",
                fontSize = 10.sp,
                color = if (isDarkTheme) TextMutedDark else TextMutedLight
            )
        }

        Text(
            text = "\"${entry.input}\"",
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontStyle = FontStyle.Italic,
            fontSize = 11.sp,
            color = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight
        )

        HorizontalDivider(color = cardBorder, modifier = Modifier.padding(vertical = 2.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(
                onClick = onRestore,
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = null,
                    tint = IndigoPrimary,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Restore",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = IndigoPrimary
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TextButton(
                    onClick = onCopyOriginal,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text(
                        text = "Original",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight
                    )
                }

                Button(
                    onClick = onCopyEnhanced,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    modifier = Modifier.height(26.dp)
                ) {
                    Text(
                        text = "Enhanced",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = RoseRed.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
