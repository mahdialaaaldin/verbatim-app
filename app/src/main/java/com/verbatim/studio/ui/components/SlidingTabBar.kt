package com.verbatim.studio.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
    hasOutput: Boolean = false,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val barBg = if (isDarkTheme) Color(0xFF030712).copy(alpha = 0.85f) else Color(0xFFE2E8F0).copy(alpha = 0.85f)
    val barBorder = if (isDarkTheme) BorderDarkSubtle.copy(alpha = 0.5f) else BorderLight

    BoxWithConstraints(
        modifier = modifier
            .widthIn(max = 290.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(barBg)
            .border(1.dp, barBorder, RoundedCornerShape(14.dp))
            .padding(3.dp)
    ) {
        val tabWidth = maxWidth / 2

        // Animated Sliding Indicator Pill with spring physics
        val indicatorOffset by animateFloatAsState(
            targetValue = if (selectedTab == 0) 0f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "tab_indicator"
        )

        Box(
            modifier = Modifier
                .offset(x = tabWidth * indicatorOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .clip(RoundedCornerShape(11.dp))
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
                    .clip(RoundedCornerShape(11.dp))
                    .clickable { onTabSelected(0) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "INPUT TEXT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = if (selectedTab == 0) Color.White else (if (isDarkTheme) TextSecondaryDark else TextSecondaryLight)
                )
            }

            // Output Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(11.dp))
                    .clickable { onTabSelected(1) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text(
                        text = "AI OUTPUT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = if (selectedTab == 1) Color.White else (if (isDarkTheme) TextSecondaryDark else TextSecondaryLight)
                    )

                    // Dot indicator if output is available or loading
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(FuchsiaAccent)
                        )
                    } else if (hasOutput && selectedTab != 1) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(EmeraldGreen)
                        )
                    }
                }
            }
        }
    }
}
