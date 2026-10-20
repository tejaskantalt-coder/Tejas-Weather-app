package com.example.data.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.ui.theme.*

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT;

    fun format(celsius: Double): String {
        return when (this) {
            CELSIUS -> "${Math.round(celsius)}°C"
            FAHRENHEIT -> "${Math.round((celsius * 9 / 5) + 32)}°F"
        }
    }

    fun formatValue(celsius: Double): String {
        return when (this) {
            CELSIUS -> "${Math.round(celsius)}°"
            FAHRENHEIT -> "${Math.round((celsius * 9 / 5) + 32)}°"
        }
    }
}

enum class WeatherCondition(
    val title: String,
    val skyGradient: List<Color>,
    @get:StringRes val titleResId: Int = R.string.condition_clear
) {
    CLEAR_DAY(
        title = "Sunny",
        skyGradient = listOf(SkyBlueTop, SkyBlueMiddle, SkyBlueBottom),
        titleResId = R.string.condition_sunny
    ),
    CLEAR_NIGHT(
        title = "Clear",
        skyGradient = listOf(Color(0xFF141927), Color(0xFF222B42), Color(0xFF354363)),
        titleResId = R.string.condition_clear
    ),
    PARTLY_CLOUDY_DAY(
        title = "Partly Cloudy",
        skyGradient = listOf(SkyBlueTop, SkyBlueMiddle, SkyBlueBottom),
        titleResId = R.string.condition_partly_cloudy
    ),
    PARTLY_CLOUDY_NIGHT(
        title = "Partly Cloudy",
        skyGradient = listOf(Color(0xFF1E2538), Color(0xFF2F3B54), Color(0xFF455573)),
        titleResId = R.string.condition_partly_cloudy
    ),
    OVERCAST(
        title = "Cloudy",
        skyGradient = listOf(Color(0xFF5B7184), Color(0xFF7F95A8), Color(0xFFAEC1D1)),
        titleResId = R.string.condition_cloudy
    ),
    LIGHT_RAIN(
        title = "Light Rain",
        skyGradient = listOf(Color(0xFF436077), Color(0xFF67859D), Color(0xFF98B3C9)),
        titleResId = R.string.condition_light_rain
    ),
    RAIN(
        title = "Rain",
        skyGradient = listOf(Color(0xFF334E63), Color(0xFF547188), Color(0xFF86A3B9)),
        titleResId = R.string.condition_rain
    ),
    THUNDERSTORM(
        title = "Thunderstorm",
        skyGradient = listOf(Color(0xFF2D293E), Color(0xFF463E5E), Color(0xFF766D94)),
        titleResId = R.string.condition_thunderstorm
    ),
    SNOW(
        title = "Snow",
        skyGradient = listOf(Color(0xFF6D859B), Color(0xFF95ACBF), Color(0xFFC7D7E5)),
        titleResId = R.string.condition_snow
    ),
    FOG(
        title = "Foggy",
        skyGradient = listOf(Color(0xFF6F7E8C), Color(0xFF91A0AD), Color(0xFFC2CED7)),
        titleResId = R.string.condition_fog
    );

    companion object {
        fun fromWmoCode(code: Int, isDay: Boolean): WeatherCondition {
            return when (code) {
                0 -> if (isDay) CLEAR_DAY else CLEAR_NIGHT
                1, 2 -> if (isDay) PARTLY_CLOUDY_DAY else PARTLY_CLOUDY_NIGHT
                3 -> OVERCAST
                45, 48 -> FOG
                51, 53, 55, 80 -> LIGHT_RAIN
                61, 63, 65, 81, 82 -> RAIN
                71, 73, 75, 77, 85, 86 -> SNOW
                95, 96, 99 -> THUNDERSTORM
                else -> if (isDay) PARTLY_CLOUDY_DAY else PARTLY_CLOUDY_NIGHT
            }
        }
    }
}

data class CityLocation(
    val name: String,
    val country: String,
    val stateOrRegion: String = "",
    val latitude: Double,
    val longitude: Double,
    val isFavorite: Boolean = false
) {
    val fullSubtitle: String
        get() = listOfNotNull(
            stateOrRegion.takeIf { it.isNotBlank() && it != name },
            country.takeIf { it.isNotBlank() }
        ).joinToString(", ")
}

data class CurrentWeather(
    val temperature: Double,
    val feelsLike: Double,
    val tempMin: Double,
    val tempMax: Double,
    val humidity: Int,
    val windSpeed: Double,
    val windDirection: Int,
    val surfacePressure: Double,
    val uvIndex: Double,
    val visibilityKm: Double = 10.0,
    val dewPoint: Double = 14.0,
    val aqi: Int = 32,
    val condition: WeatherCondition,
    val isDay: Boolean,
    val dayAndTimeString: String = "Tue, 3:52 pm",
    val updateTime: String = "3:52 pm"
)

data class HourlyForecast(
    val timeLabel: String,
    val temperature: Double,
    val rainProbability: Int,
    val precipitationMm: Double = 0.0,
    val condition: WeatherCondition,
    val isDay: Boolean = true,
    val isSunset: Boolean = false,
    val sunsetLabel: String = ""
)

data class DailyForecast(
    val dateLabel: String,
    val dayName: String,
    val tempMin: Double,
    val tempMax: Double,
    val rainProbability: Int,
    val uvIndex: Double,
    val condition: WeatherCondition,
    val summary: String
)

data class SunriseSunset(
    val sunrise: String,
    val sunset: String,
    val daylightProgress: Float = 0.6f,
    val remainingDaylight: String = "4h 25m left"
)

data class FullWeatherReport(
    val location: CityLocation,
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
    val sunTimes: SunriseSunset,
    val headlineSummary: String = "Showers early. Low 28°C."
)
