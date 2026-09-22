package com.verbatim.studio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.History
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
    val cardBg = if (isDarkTheme) SurfaceDark.copy(alpha = 0.9f) else SurfaceLight.copy(alpha = 0.95f)
    val cardBorder = if (isDarkTheme) BorderDarkSubtle.copy(alpha = 0.5f) else BorderLight
    val iconBtnBg = if (isDarkTheme) SurfaceVariantDark.copy(alpha = 0.5f) else SurfaceVariantLight
    val iconBtnTint = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        shadowElevation = 4.dp
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
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isDarkTheme) SurfaceVariantDark else SurfaceVariantLight)
                        .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "V",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = IndigoPrimary
                    )
                }

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Verbatim ",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
                        )
                        Text(
                            text = "Studio",
                            style = MaterialTheme.typography.titleMedium.copy(
                                brush = Brush.horizontalGradient(
                                    listOf(IndigoPrimary, FuchsiaAccent)
                                )
                            ),
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (hasApiKey) EmeraldGreen else RoseRed)
                        )
                        Text(
                            text = "Running: Google Gemini",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDarkTheme) TextMutedDark else TextMutedLight
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
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBtnBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme",
                        tint = iconBtnTint,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Incognito Toggle
                IconButton(
                    onClick = onToggleIncognito,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isIncognito) PurpleAccent.copy(alpha = 0.2f) else iconBtnBg
                        )
                        .border(
                            1.dp,
                            if (isIncognito) PurpleAccent.copy(alpha = 0.4f) else cardBorder,
                            RoundedCornerShape(10.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (isIncognito) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Incognito",
                        tint = if (isIncognito) PurpleAccent else iconBtnTint,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // History Toggle
                IconButton(
                    onClick = onOpenHistory,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBtnBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = iconBtnTint,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Settings Toggle
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBtnBg)
                        .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = iconBtnTint,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}
