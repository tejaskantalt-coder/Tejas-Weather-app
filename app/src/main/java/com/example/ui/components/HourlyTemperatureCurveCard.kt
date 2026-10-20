package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HourlyForecast
import com.example.data.model.TemperatureUnit
import com.example.data.model.WeatherCondition
import com.example.ui.theme.*

@Composable
fun HourlyTemperatureCurveCard(
    headlineSummary: String,
    hourly: List<HourlyForecast>,
    tempUnit: TemperatureUnit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = FrostedCardBg
        ),
        border = BorderStroke(1.dp, FrostedCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("hourly_temperature_curve_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            // Headline Summary: "Showers early. Low 28°C."
            Text(
                text = headlineSummary,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextWhitePrimary,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            val scrollState = rememberScrollState()
            val itemWidth = 64.dp
            val totalPoints = hourly.size

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 14.dp)
            ) {
                // Row 1: Time Labels ("4:30 pm", "5:30 pm", "6:28 pm", ...)
                Row(modifier = Modifier.width(itemWidth * totalPoints)) {
                    hourly.forEach { item ->
                        Box(
                            modifier = Modifier.width(itemWidth),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = item.timeLabel,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = TextWhiteSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 2: Condition Icons / Sunset Badges
                Row(modifier = Modifier.width(itemWidth * totalPoints)) {
                    hourly.forEach { item ->
                        Box(
                            modifier = Modifier.width(itemWidth),
                            contentAlignment = Alignment.Center
                        ) {
                            if (item.isSunset) {
                                Icon(
                                    imageVector = Icons.Default.WbTwilight,
                                    contentDescription = "Sunset",
                                    tint = SunsetOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = getHourlyIconEmoji(item.condition),
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Row 3: Temperature or Sunset text ("36°", "35°", "Sunset", "30°", ...)
                Row(modifier = Modifier.width(itemWidth * totalPoints)) {
                    hourly.forEach { item ->
                        Box(
                            modifier = Modifier.width(itemWidth),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (item.isSunset) "Sunset" else tempUnit.formatValue(item.temperature),
                                fontSize = if (item.isSunset) 11.sp else 14.sp,
                                fontWeight = if (item.isSunset) FontWeight.Medium else FontWeight.SemiBold,
                                color = if (item.isSunset) SunsetOrange else TextWhitePrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Row 4: Connected Bezier Temperature Curve Canvas
                val minT = hourly.minOfOrNull { it.temperature } ?: 28.0
                val maxT = hourly.maxOfOrNull { it.temperature } ?: 38.0
                val rangeT = (maxT - minT).coerceAtLeast(1.0)

                val cachedPath = androidx.compose.runtime.remember { Path() }
                val cachedStroke = androidx.compose.runtime.remember { Stroke(width = 2.0f, cap = StrokeCap.Round) }
                val normTemps = androidx.compose.runtime.remember(hourly, minT, rangeT) {
                    FloatArray(hourly.size) { idx ->
                        ((hourly[idx].temperature - minT) / rangeT).toFloat().coerceIn(0f, 1f)
                    }
                }

                Canvas(
                    modifier = Modifier
                        .width(itemWidth * totalPoints)
                        .height(38.dp)
                ) {
                    val w = size.width
                    val h = size.height
                    val colWidth = w / totalPoints

                    if (totalPoints > 1) {
                        cachedPath.reset()
                        val firstY = h - (normTemps[0] * (h - 16f) + 8f)
                        cachedPath.moveTo(colWidth / 2f, firstY)

                        for (i in 0 until totalPoints - 1) {
                            val x0 = i * colWidth + colWidth / 2f
                            val y0 = h - (normTemps[i] * (h - 16f) + 8f)
                            val x1 = (i + 1) * colWidth + colWidth / 2f
                            val y1 = h - (normTemps[i + 1] * (h - 16f) + 8f)
                            val cx = (x0 + x1) / 2f
                            cachedPath.cubicTo(cx, y0, cx, y1, x1, y1)
                        }

                        drawPath(
                            path = cachedPath,
                            color = TempCurveYellow.copy(alpha = 0.85f),
                            style = cachedStroke
                        )

                        // Draw Circular White Node points with primitive coordinates
                        for (i in 0 until totalPoints) {
                            val px = i * colWidth + colWidth / 2f
                            val py = h - (normTemps[i] * (h - 16f) + 8f)
                            drawCircle(
                                color = TempCurveNode,
                                radius = 3.5f,
                                center = Offset(px, py)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Row 5: Rain Probabilities ("💧24%", "💧55%", "💧35%", ...)
                Row(modifier = Modifier.width(itemWidth * totalPoints)) {
                    hourly.forEach { item ->
                        Box(
                            modifier = Modifier.width(itemWidth),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!item.isSunset && item.rainProbability > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WaterDrop,
                                        contentDescription = null,
                                        tint = RainCyan,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = "${item.rainProbability}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextWhiteSecondary
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun getHourlyIconEmoji(condition: WeatherCondition): String {
    return when (condition) {
        WeatherCondition.CLEAR_DAY -> "🌤️"
        WeatherCondition.CLEAR_NIGHT -> "🌙"
        WeatherCondition.PARTLY_CLOUDY_DAY -> "🌤️"
        WeatherCondition.PARTLY_CLOUDY_NIGHT -> "☁️"
        WeatherCondition.OVERCAST -> "☁️"
        WeatherCondition.LIGHT_RAIN -> "🌧️"
        WeatherCondition.RAIN -> "🌧️"
        WeatherCondition.THUNDERSTORM -> "⛈️"
        WeatherCondition.SNOW -> "❄️"
        WeatherCondition.FOG -> "🌫️"
    }
}
