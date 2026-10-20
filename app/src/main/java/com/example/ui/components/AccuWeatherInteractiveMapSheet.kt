package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.accuweather.AccuWeatherMapProvider
import com.example.data.model.*
import com.example.ui.theme.AccuWeatherNavy
import com.example.ui.theme.AccuWeatherOrange
import kotlinx.coroutines.delay

@Composable
fun AccuWeatherInteractiveMapSheet(
    currentWeather: CurrentWeather,
    location: CityLocation,
    tempUnit: TemperatureUnit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onDismiss() }

    val context = LocalContext.current
    var selectedLayer by remember { mutableStateOf(AccuWeatherMapLayer.RADAR_PRECIPITATION) }
    var currentFrameIndex by remember { mutableIntStateOf(3) }
    var isPlaying by remember { mutableStateOf(false) }

    var zoom by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val frames = remember(currentWeather.condition, location.name, currentWeather.temperature) {
        AccuWeatherMapProvider.generateFrames(
            condition = currentWeather.condition,
            cityName = location.name,
            temperature = currentWeather.temperature,
            windSpeed = currentWeather.windSpeed,
            windDirection = currentWeather.windDirection
        )
    }

    val activeFrame = frames.getOrElse(currentFrameIndex) { frames.first() }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1200)
            currentFrameIndex = (currentFrameIndex + 1) % frames.size
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "InteractiveSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Sweep"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(AccuWeatherNavy)
                .testTag("accuweather_interactive_map_screen")
        ) {
            AccuWeatherMapCanvas(
                layer = selectedLayer,
                frame = activeFrame,
                sweepAngle = sweepAngle,
                cityName = location.name,
                tempUnit = tempUnit,
                zoom = zoom,
                panOffset = panOffset,
                interactive = true,
                onTransform = { pan, zoomFactor ->
                    panOffset += pan
                    zoom = (zoom * zoomFactor).coerceIn(0.5f, 4.0f)
                }
            )

            // Top Header App Bar
            Surface(
                color = Color.Black.copy(alpha = 0.75f),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("accuweather_map_close_button")
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = location.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = stringResource(R.string.accuweather_branding),
                                style = MaterialTheme.typography.labelSmall,
                                color = AccuWeatherOrange,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        IconButton(
                            onClick = {
                                val mapUrl = "https://www.accuweather.com/en/search-locations?query=${location.latitude},${location.longitude}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapUrl))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.testTag("accuweather_external_link_button")
                        ) {
                            Icon(imageVector = Icons.Default.OpenInNew, contentDescription = "Open in browser", tint = AccuWeatherOrange)
                        }
                    }

                    // Layer Switcher Row
                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(scrollState)
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AccuWeatherMapLayer.entries.forEach { layer ->
                            val isSelected = selectedLayer == layer
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) layer.themeColor else Color.White.copy(alpha = 0.12f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { selectedLayer = layer }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(text = layer.iconEmoji, fontSize = 14.sp)
                                    Text(
                                        text = stringResource(layer.titleResId),
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Floating Controls: Zoom & Recenter
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FloatingActionButton(
                    onClick = { zoom = (zoom * 1.25f).coerceAtMost(4.0f) },
                    containerColor = Color.Black.copy(alpha = 0.7f),
                    contentColor = Color.White,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Zoom In")
                }

                FloatingActionButton(
                    onClick = { zoom = (zoom / 1.25f).coerceAtLeast(0.5f) },
                    containerColor = Color.Black.copy(alpha = 0.7f),
                    contentColor = Color.White,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Zoom Out")
                }

                FloatingActionButton(
                    onClick = {
                        zoom = 1.0f
                        panOffset = Offset.Zero
                    },
                    containerColor = AccuWeatherOrange,
                    contentColor = Color.White,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(imageVector = Icons.Default.MyLocation, contentDescription = "Recenter")
                }
            }

            // Bottom Panel: MinuteCast Summary, Timeline Scrubber & Legend
            Surface(
                color = Color.Black.copy(alpha = 0.85f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = activeFrame.minuteCastSummary,
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { isPlaying = !isPlaying },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(AccuWeatherOrange)
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            frames.forEachIndexed { index, frame ->
                                val isSelected = currentFrameIndex == index
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            if (isSelected) AccuWeatherOrange else Color.White.copy(alpha = 0.15f)
                                        )
                                        .clickable {
                                            currentFrameIndex = index
                                            isPlaying = false
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = frame.timeLabel,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AccuWeatherLayerLegend(layer = selectedLayer)
                }
            }
        }
    }
}

@Composable
private fun AccuWeatherLayerLegend(layer: AccuWeatherMapLayer) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.accuweather_legend_intensity),
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "${layer.unit} scale",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        when (layer) {
            AccuWeatherMapLayer.RADAR_PRECIPITATION -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF81C784),
                                    Color(0xFF388E3C),
                                    Color(0xFFFBC02D),
                                    Color(0xFFF57C00),
                                    Color(0xFFD32F2F),
                                    Color(0xFF9C27B0)
                                )
                            )
                        )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Light (15)", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    Text("Moderate (35)", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    Text("Heavy (50)", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    Text("Severe (65+ dBZ)", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                }
            }
            AccuWeatherMapLayer.TEMPERATURE -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF1976D2),
                                    Color(0xFF0097A7),
                                    Color(0xFF388E3C),
                                    Color(0xFFFFA000),
                                    Color(0xFFD32F2F)
                                )
                            )
                        )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("< 0°C", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    Text("10°C", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    Text("20°C", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    Text("> 32°C", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                }
            }
            AccuWeatherMapLayer.WIND_STREAM -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF80CBC4),
                                    Color(0xFF26A69A),
                                    Color(0xFF00796B),
                                    Color(0xFF004D40)
                                )
                            )
                        )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Calm (<5)", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    Text("Moderate (15-30)", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    Text("Gale (>60 km/h)", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                }
            }
            AccuWeatherMapLayer.CLOUDS_SATELLITE -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.1f),
                                    Color.White.copy(alpha = 0.5f),
                                    Color.White
                                )
                            )
                        )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Clear (0%)", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    Text("Scattered (50%)", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    Text("Overcast (100%)", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                }
            }
            AccuWeatherMapLayer.SEVERE_STORMS -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("⚠️ Severe Storm Warning", color = Color(0xFFEF5350), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("⚡ Lightning Risk", color = Color(0xFFFFD54F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("🌪️ Rotation Tracking", color = Color(0xFFAB47BC), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
