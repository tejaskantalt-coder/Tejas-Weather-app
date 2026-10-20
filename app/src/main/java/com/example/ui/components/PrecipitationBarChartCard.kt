package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HourlyForecast
import com.example.ui.theme.*

@Composable
fun PrecipitationBarChartCard(
    hourly: List<HourlyForecast>,
    modifier: Modifier = Modifier
) {
    // Filter out sunset event for clean precipitation bar chart
    val rainItems = hourly.filter { !it.isSunset }.take(6)
    val maxPrecip = (rainItems.maxOfOrNull { it.precipitationMm } ?: 2.0).coerceAtLeast(1.5)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = FrostedCardBg
        ),
        border = BorderStroke(1.dp, FrostedCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("precipitation_bar_chart_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val scrollState = rememberScrollState()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                verticalAlignment = Alignment.Bottom
            ) {
                // Left Axis Icon / Labels
                Column(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .height(110.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        tint = RainCyan.copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "mm",
                        fontSize = 10.sp,
                        color = TextWhiteTertiary,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Bar Columns
                rainItems.forEach { item ->
                    val frac = (item.precipitationMm / maxPrecip).toFloat().coerceIn(0.04f, 1f)
                    val barHeight = (frac * 60f).dp.coerceAtLeast(6.dp)

                    Column(
                        modifier = Modifier
                            .width(58.dp)
                            .height(110.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        // Time Label at top of column
                        Text(
                            text = item.timeLabel,
                            fontSize = 10.sp,
                            color = TextWhiteSecondary,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // Vertical Gradient Precipitation Bar
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(RainBarTop, RainBarBottom)
                                    )
                                )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Amount Label ("0.0", "1.49", "0.02 mm")
                        Text(
                            text = String.format("%.2f", item.precipitationMm),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhitePrimary
                        )
                    }
                }
            }
        }
    }
}
