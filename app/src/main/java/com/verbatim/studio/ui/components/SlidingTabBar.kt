package com.verbatim.studio.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material3.Icon
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
    selectedTab: Int, // 0 for Split (Both), 1 for Input, 2 for Output
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
            .widthIn(max = 320.dp)
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(barBg)
            .border(1.dp, barBorder, RoundedCornerShape(12.dp))
            .padding(3.dp)
    ) {
        val tabWidth = maxWidth / 3

        // Animated Sliding Indicator Pill with spring physics
        val indicatorOffset by animateFloatAsState(
            targetValue = selectedTab.toFloat(),
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
                .clip(RoundedCornerShape(9.dp))
                .background(IndigoPrimary)
        )

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 0: Split View (Both Input and Output)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(9.dp))
                    .clickable { onTabSelected(0) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Splitscreen,
                        contentDescription = "Split View",
                        tint = if (selectedTab == 0) Color.White else (if (isDarkTheme) TextSecondaryDark else TextSecondaryLight),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "BOTH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = if (selectedTab == 0) Color.White else (if (isDarkTheme) TextSecondaryDark else TextSecondaryLight)
                    )
                }
            }

            // Tab 1: Input Tab Only
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(9.dp))
                    .clickable { onTabSelected(1) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "INPUT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = if (selectedTab == 1) Color.White else (if (isDarkTheme) TextSecondaryDark else TextSecondaryLight)
                )
            }

            // Tab 2: Output Tab Only
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(9.dp))
                    .clickable { onTabSelected(2) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "OUTPUT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.6.sp,
                        color = if (selectedTab == 2) Color.White else (if (isDarkTheme) TextSecondaryDark else TextSecondaryLight)
                    )

                    // Dot indicator if output is available or loading
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(FuchsiaAccent)
                        )
                    } else if (hasOutput && selectedTab != 2) {
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
