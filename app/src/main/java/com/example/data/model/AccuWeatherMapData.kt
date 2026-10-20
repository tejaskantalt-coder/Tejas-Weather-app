package com.example.data.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.ui.theme.AccuWeatherOrange

enum class AccuWeatherMapLayer(
    val id: String,
    @StringRes val titleResId: Int,
    val defaultTitle: String,
    val iconEmoji: String,
    val themeColor: Color,
    val unit: String
) {
    RADAR_PRECIPITATION(
        id = "radar_rain",
        titleResId = R.string.accuweather_layer_radar,
        defaultTitle = "Radar & Rain",
        iconEmoji = "🌧️",
        themeColor = Color(0xFF0288D1),
        unit = "dBZ"
    ),
    CLOUDS_SATELLITE(
        id = "clouds_sat",
        titleResId = R.string.accuweather_layer_clouds,
        defaultTitle = "Clouds & Satellite",
        iconEmoji = "☁️",
        themeColor = Color(0xFF78909C),
        unit = "%"
    ),
    TEMPERATURE(
        id = "temperature",
        titleResId = R.string.accuweather_layer_temp,
        defaultTitle = "Temperature",
        iconEmoji = "🌡️",
        themeColor = AccuWeatherOrange,
        unit = "°"
    ),
    WIND_STREAM(
        id = "wind_stream",
        titleResId = R.string.accuweather_layer_wind,
        defaultTitle = "Wind Stream",
        iconEmoji = "💨",
        themeColor = Color(0xFF26A69A),
        unit = "km/h"
    ),
    SEVERE_STORMS(
        id = "severe_storms",
        titleResId = R.string.accuweather_layer_severe,
        defaultTitle = "Severe Storms",
        iconEmoji = "⚡",
        themeColor = Color(0xFFE53935),
        unit = "alerts"
    );

    companion object {
        fun fromId(id: String): AccuWeatherMapLayer = entries.find { it.id == id } ?: RADAR_PRECIPITATION
    }
}

data class AccuWeatherMapFrame(
    val frameIndex: Int,
    val timeLabel: String,
    val minutesOffset: Int,
    val precipitationEchoes: List<AccuWeatherEcho>,
    val temperatureIsotherms: List<AccuWeatherIsotherm>,
    val windStreamVectors: List<AccuWeatherWindVector>,
    val cloudBlobs: List<AccuWeatherCloudPatch>,
    val severeCells: List<AccuWeatherSevereCell>,
    val minuteCastSummary: String
)

data class AccuWeatherEcho(
    val relX: Float,
    val relY: Float,
    val radius: Float,
    val intensityDbz: Int,
    val color: Color
)

data class AccuWeatherIsotherm(
    val relX: Float,
    val relY: Float,
    val radius: Float,
    val tempCelsius: Double,
    val color: Color
)

data class AccuWeatherWindVector(
    val startRelX: Float,
    val startRelY: Float,
    val endRelX: Float,
    val endRelY: Float,
    val speedKmh: Double
)

data class AccuWeatherCloudPatch(
    val relX: Float,
    val relY: Float,
    val radiusX: Float,
    val radiusY: Float,
    val opacity: Float
)

data class AccuWeatherSevereCell(
    val relX: Float,
    val relY: Float,
    val cellType: String,
    val lightningCount: Int,
    val hailProbability: Int
)
