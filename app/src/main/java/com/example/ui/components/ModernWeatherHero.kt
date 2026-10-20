package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.CityLocation
import com.example.data.model.CurrentWeather
import com.example.data.model.TemperatureUnit
import com.example.ui.theme.FrostedCardBg
import com.example.ui.theme.SunYellow
import com.example.ui.theme.TextWhitePrimary
import com.example.ui.theme.TextWhiteSecondary
import com.example.ui.theme.TextWhiteTertiary

@Composable
fun ModernWeatherHero(
    location: CityLocation,
    current: CurrentWeather,
    tempUnit: TemperatureUnit,
    currentLanguage: AppLanguage,
    onOpenSearch: () -> Unit,
    onOpenLanguageSheet: () -> Unit,
    onToggleTempUnit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        // Top Navigation Bar: "⌂ Home" & Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onOpenSearch() }
                    .padding(vertical = 4.dp, horizontal = 6.dp)
                    .testTag("location_home_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    tint = TextWhitePrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (location.name.isNotBlank()) location.name else stringResource(R.string.home_label),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextWhitePrimary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Change location",
                    tint = TextWhiteSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Action Buttons: Unit Toggle, Language, Search
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Unit Toggle Button (°C / °F)
                Surface(
                    shape = CircleShape,
                    color = FrostedCardBg,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onToggleTempUnit() }
                        .testTag("unit_toggle_button")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (tempUnit == TemperatureUnit.CELSIUS) "°C" else "°F",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextWhitePrimary
                        )
                    }
                }

                // Language Selector Button
                Surface(
                    shape = CircleShape,
                    color = FrostedCardBg,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onOpenLanguageSheet() }
                        .testTag("language_selector_button")
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentLanguage.flag,
                            fontSize = 14.sp
                        )
                    }
                }

                // Search Icon Button
                IconButton(
                    onClick = onOpenSearch,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(FrostedCardBg)
                        .testTag("search_icon_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = stringResource(R.string.search_cities_hint),
                        tint = TextWhitePrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Weather Info & Character Art Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            // Left Column: Temperature, Condition, High/Low, Feels like, Day & Time
            Column(
                modifier = Modifier.weight(1.1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Big 37° Display
                Text(
                    text = tempUnit.formatValue(current.temperature),
                    fontSize = 76.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextWhitePrimary,
                    lineHeight = 80.sp
                )

                // Condition Title
                Text(
                    text = current.condition.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextWhitePrimary,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // ↑ High / ↓ Low (e.g. ↑38° / ↓28°)
                Text(
                    text = "↑${tempUnit.formatValue(current.tempMax)} / ↓${tempUnit.formatValue(current.tempMin)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextWhiteSecondary
                )

                // Feels like
                Text(
                    text = "${stringResource(R.string.feels_like)} ${tempUnit.formatValue(current.feelsLike)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextWhiteSecondary
                )

                // Day & Time (e.g. Tue, 3:52 pm)
                Text(
                    text = current.dayAndTimeString,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextWhiteTertiary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // Right Column: Dynamic Animated Weather Art
            Box(
                modifier = Modifier
                    .weight(0.95f)
                    .height(230.dp)
                    .testTag("weather_hero_artwork"),
                contentAlignment = Alignment.Center
            ) {
                AnimatedWeatherArt(
                    condition = current.condition,
                    isDay = current.isDay,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
