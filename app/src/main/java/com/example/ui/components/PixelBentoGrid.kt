package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.CurrentWeather
import com.example.data.model.SunriseSunset
import com.example.ui.theme.*

@Composable
fun PixelBentoGrid(
    current: CurrentWeather,
    sunTimes: SunriseSunset,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pixel_bento_grid"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Row 1: Wind & UV Index
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BentoCard(
                icon = Icons.Default.Air,
                title = stringResource(R.string.wind),
                value = "${Math.round(current.windSpeed)} km/h",
                subtitle = "Direction: ${current.windDirection}°",
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(FrostedInnerBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = TextWhitePrimary,
                            modifier = Modifier
                                .size(14.dp)
                                .rotate(current.windDirection.toFloat())
                        )
                    }
                    Text(
                        text = getWindCategory(current.windSpeed),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextWhiteSecondary
                    )
                }
            }

            BentoCard(
                icon = Icons.Default.WbSunny,
                title = stringResource(R.string.uv_index),
                value = String.format("%.1f", current.uvIndex),
                subtitle = getUvCategory(current.uvIndex),
                modifier = Modifier.weight(1f)
            ) {
                LinearProgressIndicator(
                    progress = { (current.uvIndex / 12f).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = SunYellow,
                    trackColor = FrostedInnerBg
                )
            }
        }

        // Row 2: Sunrise/Sunset & Humidity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BentoCard(
                icon = Icons.Default.WbTwilight,
                title = stringResource(R.string.sun_times),
                value = sunTimes.sunset,
                subtitle = "Sunrise: ${sunTimes.sunrise}",
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = sunTimes.remainingDaylight,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextWhiteSecondary
                )
            }

            BentoCard(
                icon = Icons.Default.WaterDrop,
                title = stringResource(R.string.humidity),
                value = "${current.humidity}%",
                subtitle = "Dew point: ${Math.round(current.dewPoint)}°",
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (current.humidity > 70) "Humid conditions" else if (current.humidity < 35) "Dry air" else "Comfortable level",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextWhiteSecondary
                )
            }
        }

        // Row 3: Pressure & Visibility
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BentoCard(
                icon = Icons.Default.Compress,
                title = stringResource(R.string.pressure),
                value = "${Math.round(current.surfacePressure)} hPa",
                subtitle = if (current.surfacePressure > 1013) "High pressure" else "Low pressure",
                modifier = Modifier.weight(1f)
            )

            BentoCard(
                icon = Icons.Default.Visibility,
                title = stringResource(R.string.visibility),
                value = "${String.format("%.1f", current.visibilityKm)} km",
                subtitle = if (current.visibilityKm > 9) "Clear visibility" else "Reduced visibility",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BentoCard(
    icon: ImageVector,
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    content: @Composable (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = FrostedCardBg
        ),
        border = BorderStroke(1.dp, FrostedCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextWhiteSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhiteSecondary
                )
            }

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = TextWhitePrimary
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextWhiteSecondary
            )

            if (content != null) {
                Spacer(modifier = Modifier.height(4.dp))
                content()
            }
        }
    }
}

private fun getWindCategory(speedKmh: Double): String {
    return when {
        speedKmh < 10 -> "Light breeze"
        speedKmh < 25 -> "Moderate breeze"
        speedKmh < 45 -> "Strong breeze"
        else -> "High winds"
    }
}

private fun getUvCategory(uv: Double): String {
    return when {
        uv < 3 -> "Low"
        uv < 6 -> "Moderate"
        uv < 8 -> "Very High"
        else -> "Extreme"
    }
}
