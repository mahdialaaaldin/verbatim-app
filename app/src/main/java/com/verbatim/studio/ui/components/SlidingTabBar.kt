package com.verbatim.studio.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.verbatim.studio.theme.*

@Composable
fun SlidingTabBar(
    selectedTab: Int, // 0 for Input, 1 for Output
    onTabSelected: (Int) -> Unit,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val barBg = if (isDarkTheme) Color(0xFF020617).copy(alpha = 0.8f) else Color(0xFFE2E8F0).copy(alpha = 0.8f)
    val barBorder = if (isDarkTheme) BorderDarkSubtle.copy(alpha = 0.6f) else BorderLight

    BoxWithConstraints(
        modifier = modifier
            .widthIn(max = 280.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(barBg)
            .border(1.dp, barBorder, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        val tabWidth = maxWidth / 2

        // Animated Sliding Indicator Pill
        val indicatorOffset by animateFloatAsState(
            targetValue = if (selectedTab == 0) 0f else 1f,
            animationSpec = tween(durationMillis = 250),
            label = "tab_indicator"
        )

        Box(
            modifier = Modifier
                .offset(x = tabWidth * indicatorOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(8.dp))
                .background(IndigoPrimary)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Input Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onTabSelected(0) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "INPUT TEXT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = if (selectedTab == 0) Color.White else (if (isDarkTheme) TextSecondaryDark else TextSecondaryLight)
                )
            }

            // Output Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onTabSelected(1) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "OUTPUT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    color = if (selectedTab == 1) Color.White else (if (isDarkTheme) TextSecondaryDark else TextSecondaryLight)
                )
            }
        }
    }
}
