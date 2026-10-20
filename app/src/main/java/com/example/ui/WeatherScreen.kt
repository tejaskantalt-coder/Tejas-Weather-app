package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.*
import com.example.ui.theme.SkyBlueBottom
import com.example.ui.theme.SkyBlueMiddle
import com.example.ui.theme.SkyBlueTop

@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val report = uiState.weatherReport

    val skyGradient = listOf(SkyBlueTop, SkyBlueMiddle, SkyBlueBottom)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(skyGradient))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Header & Modern Hero Section (matching screenshot)
            if (report != null) {
                ModernWeatherHero(
                    location = uiState.selectedLocation,
                    current = report.current,
                    tempUnit = uiState.tempUnit,
                    currentLanguage = uiState.currentLanguage,
                    onOpenSearch = { viewModel.openSearchDialog() },
                    onOpenLanguageSheet = { viewModel.openLanguageSheet() },
                    onToggleTempUnit = { viewModel.toggleTempUnit() }
                )
            }

            if (uiState.isLoading && report == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onBackground)
                }
            } else if (report != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
                ) {
                    // 2. Hourly Temperature Curve Card with Smooth Bezier & Sunset Node
                    if (report.hourly.isNotEmpty()) {
                        item(key = "hourly_temp_curve", contentType = "temp_curve") {
                            HourlyTemperatureCurveCard(
                                headlineSummary = report.headlineSummary,
                                hourly = report.hourly,
                                tempUnit = uiState.tempUnit
                            )
                        }
                    }

                    // 3. Precipitation Vertical Bar Chart Card (matching screenshot)
                    if (report.hourly.isNotEmpty()) {
                        item(key = "precipitation_bar_chart", contentType = "precip_chart") {
                            PrecipitationBarChartCard(
                                hourly = report.hourly
                            )
                        }
                    }

                    // 4. Real Weather Map using AccuWeather
                    item(key = "accuweather_map_card", contentType = "map_card") {
                        PixelAccuWeatherMapCard(
                            currentWeather = report.current,
                            location = uiState.selectedLocation,
                            tempUnit = uiState.tempUnit,
                            onOpenFullMap = { viewModel.openAccuWeatherMap() }
                        )
                    }

                    // 5. Atmospheric Bento Grid (Wind, UV, Sun times, Humidity, Pressure, Visibility)
                    item(key = "atmospheric_bento_grid", contentType = "bento_grid") {
                        PixelBentoGrid(
                            current = report.current,
                            sunTimes = report.sunTimes
                        )
                    }
                }
            } else if (uiState.errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = uiState.errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Button(onClick = { viewModel.refreshWeather() }) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Retry")
                        }
                    }
                }
            }
        }

        // Modals
        if (uiState.isSearchDialogOpen) {
            CitySearchDialog(
                searchQuery = uiState.searchQuery,
                searchResults = uiState.searchResults,
                isSearching = uiState.isSearching,
                onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                onCitySelected = { viewModel.selectLocation(it) },
                onUseCurrentLocation = { viewModel.loadWeatherForCurrentLocation() },
                onDismiss = { viewModel.closeSearchDialog() }
            )
        }

        if (uiState.isLanguageSheetOpen) {
            LanguageSelectionSheet(
                currentLanguage = uiState.currentLanguage,
                onLanguageSelected = { viewModel.setLanguage(it) },
                onDismiss = { viewModel.closeLanguageSheet() }
            )
        }

        if (uiState.isAccuWeatherMapOpen && report != null) {
            AccuWeatherInteractiveMapSheet(
                currentWeather = report.current,
                location = uiState.selectedLocation,
                tempUnit = uiState.tempUnit,
                onDismiss = { viewModel.closeAccuWeatherMap() }
            )
        }
    }
}
