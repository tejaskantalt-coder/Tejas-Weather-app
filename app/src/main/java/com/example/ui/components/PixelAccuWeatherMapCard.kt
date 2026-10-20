package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.accuweather.AccuWeatherMapProvider
import com.example.data.model.AccuWeatherMapLayer
import com.example.data.model.CityLocation
import com.example.data.model.CurrentWeather
import com.example.data.model.TemperatureUnit
import com.example.ui.theme.*

@Composable
fun PixelAccuWeatherMapCard(
    currentWeather: CurrentWeather,
    location: CityLocation,
    tempUnit: TemperatureUnit,
    onOpenFullMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedLayer by remember { mutableStateOf(AccuWeatherMapLayer.RADAR_PRECIPITATION) }

    val frames = remember(currentWeather.condition, location.name, currentWeather.temperature) {
        AccuWeatherMapProvider.generateFrames(
            condition = currentWeather.condition,
            cityName = location.name,
            temperature = currentWeather.temperature,
            windSpeed = currentWeather.windSpeed,
            windDirection = currentWeather.windDirection
        )
    }

    val nowFrame = frames.getOrElse(3) { frames.first() }

    val infiniteTransition = rememberInfiniteTransition(label = "RadarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SweepAngle"
    )

    val livePulse by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LivePulse"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = FrostedCardBg
        ),
        border = BorderStroke(1.dp, FrostedCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("pixel_accuweather_map_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Brand, Title, and LIVE Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(AccuWeatherOrange, AccuWeatherOrangeDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "☀️", fontSize = 16.sp)
                    }

                    Column {
                        Text(
                            text = stringResource(R.string.accuweather_map_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhitePrimary
                        )
                        Text(
                            text = stringResource(R.string.accuweather_branding),
                            style = MaterialTheme.typography.labelSmall,
                            color = AccuWeatherOrange,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AccuWeatherOrange.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(AccuWeatherOrange.copy(alpha = livePulse))
                            )
                            Text(
                                text = stringResource(R.string.accuweather_now).uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccuWeatherOrange
                            )
                        }
                    }

                    IconButton(
                        onClick = onOpenFullMap,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("accuweather_expand_map_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInFull,
                            contentDescription = stringResource(R.string.accuweather_fullscreen),
                            tint = TextWhitePrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // MinuteCast® Notification Banner
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = FrostedInnerBg,
                border = BorderStroke(1.dp, FrostedCardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = AccuWeatherOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = nowFrame.minuteCastSummary,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = TextWhiteSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Layer Selection Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AccuWeatherMapLayer.entries.forEach { layer ->
                    val isSelected = selectedLayer == layer
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedLayer = layer },
                        label = {
                            Text(
                                text = "${layer.iconEmoji} ${stringResource(layer.titleResId)}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = layer.themeColor.copy(alpha = 0.3f),
                            selectedLabelColor = Color.White,
                            containerColor = FrostedInnerBg,
                            labelColor = TextWhiteSecondary
                        ),
                        modifier = Modifier.weight(1f, fill = false)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Weather Map View Box with Doppler Radar & Geographical Overlays
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(AccuWeatherNavy)
                    .clickable { onOpenFullMap() }
                    .testTag("accuweather_map_preview_box")
            ) {
                AccuWeatherMapCanvas(
                    layer = selectedLayer,
                    frame = nowFrame,
                    sweepAngle = sweepAngle,
                    cityName = location.name,
                    tempUnit = tempUnit,
                    zoom = 1.0f,
                    panOffset = Offset.Zero,
                    interactive = false
                )

                // Top Left Overlay: City & Current Layer Info
                Column(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                ) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${location.name} • ${stringResource(selectedLayer.titleResId)}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Top Right: RealFeel® temperature badge
                Surface(
                    color = Color.Black.copy(alpha = 0.65f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Text(
                        text = "RealFeel® ${tempUnit.formatValue(currentWeather.feelsLike)}",
                        color = AccuWeatherOrange,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Bottom Center: Click to Interact Banner
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Tap to pan, zoom & track forecast",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${String.format("%.2f", location.latitude)}°N, ${String.format("%.2f", location.longitude)}°E",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextWhiteSecondary
                )

                TextButton(
                    onClick = {
                        val mapUrl = "https://www.accuweather.com/en/search-locations?query=${location.latitude},${location.longitude}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.testTag("accuweather_web_link_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AccuWeatherOrange
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "AccuWeather.com",
                        color = AccuWeatherOrange,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
