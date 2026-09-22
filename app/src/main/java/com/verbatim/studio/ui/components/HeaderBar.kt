package com.verbatim.studio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verbatim.studio.theme.*

@Composable
fun HeaderBar(
    hasApiKey: Boolean,
    isDarkTheme: Boolean,
    isIncognito: Boolean,
    onToggleTheme: () -> Unit,
    onToggleIncognito: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = if (isDarkTheme) SurfaceDark.copy(alpha = 0.85f) else SurfaceLight.copy(alpha = 0.95f)
    val cardBorder = if (isDarkTheme) BorderDarkSubtle.copy(alpha = 0.5f) else BorderLight
    val iconBtnBg = if (isDarkTheme) SurfaceVariantDark.copy(alpha = 0.55f) else SurfaceVariantLight
    val iconBtnTint = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        shadowElevation = if (isDarkTheme) 0.dp else 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Logo & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Logo Badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(IndigoPrimary, FuchsiaAccent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "V",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Verbatim ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
                        )
                        Text(
                            text = "Studio",
                            style = MaterialTheme.typography.titleMedium.copy(
                                brush = Brush.horizontalGradient(
                                    listOf(IndigoPrimary, FuchsiaAccent)
                                )
                            ),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Interactive Status Chip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onOpenSettings)
                            .padding(vertical = 1.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.5.dp)
                                .clip(CircleShape)
                                .background(if (hasApiKey) EmeraldGreen else AmberYellow)
                        )
                        Text(
                            text = if (hasApiKey) "Gemini Active" else "Tap to Add Key",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (hasApiKey) FontWeight.Normal else FontWeight.SemiBold,
                            color = if (hasApiKey) {
                                if (isDarkTheme) TextMutedDark else TextMutedLight
                            } else {
                                if (isDarkTheme) AmberYellow else Color(0xFFB45309)
                            }
                        )
                    }
                }
            }

            // Right: Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Theme Toggle
                IconButton(
                    onClick = onToggleTheme,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBtnBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = iconBtnTint,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Incognito Toggle
                IconButton(
                    onClick = onToggleIncognito,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isIncognito) PurpleAccent.copy(alpha = 0.2f) else iconBtnBg
                        )
                        .border(
                            1.dp,
                            if (isIncognito) PurpleAccent.copy(alpha = 0.45f) else cardBorder,
                            RoundedCornerShape(10.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (isIncognito) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Incognito",
                        tint = if (isIncognito) PurpleAccent else iconBtnTint,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // History Toggle
                IconButton(
                    onClick = onOpenHistory,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBtnBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Revision History",
                        tint = iconBtnTint,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Settings Toggle
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (!hasApiKey) AmberYellow.copy(alpha = 0.15f) else iconBtnBg
                        )
                        .border(
                            1.dp,
                            if (!hasApiKey) AmberYellow.copy(alpha = 0.4f) else cardBorder,
                            RoundedCornerShape(10.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = if (!hasApiKey) AmberYellow else iconBtnTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
