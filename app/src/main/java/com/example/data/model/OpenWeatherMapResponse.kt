package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OpenWeatherCurrentResponse(
    @param:Json(name = "coord") val coord: OpenWeatherCoord? = null,
    @param:Json(name = "weather") val weather: List<OpenWeatherDescription>? = null,
    @param:Json(name = "main") val main: OpenWeatherMain? = null,
    @param:Json(name = "visibility") val visibility: Int? = null,
    @param:Json(name = "wind") val wind: OpenWeatherWind? = null,
    @param:Json(name = "clouds") val clouds: OpenWeatherClouds? = null,
    @param:Json(name = "rain") val rain: Map<String, Double>? = null,
    @param:Json(name = "snow") val snow: Map<String, Double>? = null,
    @param:Json(name = "dt") val dt: Long = 0L,
    @param:Json(name = "sys") val sys: OpenWeatherSys? = null,
    @param:Json(name = "timezone") val timezone: Int = 0,
    @param:Json(name = "id") val id: Long = 0L,
    @param:Json(name = "name") val name: String = "",
    @param:Json(name = "cod") val cod: Int = 200
)

@JsonClass(generateAdapter = true)
data class OpenWeatherForecastResponse(
    @param:Json(name = "cod") val cod: String? = null,
    @param:Json(name = "message") val message: Int? = null,
    @param:Json(name = "cnt") val cnt: Int = 0,
    @param:Json(name = "list") val list: List<OpenWeatherForecastItem>? = null,
    @param:Json(name = "city") val city: OpenWeatherCity? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherForecastItem(
    @param:Json(name = "dt") val dt: Long = 0L,
    @param:Json(name = "main") val main: OpenWeatherMain? = null,
    @param:Json(name = "weather") val weather: List<OpenWeatherDescription>? = null,
    @param:Json(name = "clouds") val clouds: OpenWeatherClouds? = null,
    @param:Json(name = "wind") val wind: OpenWeatherWind? = null,
    @param:Json(name = "visibility") val visibility: Int? = null,
    @param:Json(name = "pop") val pop: Double? = null,
    @param:Json(name = "rain") val rain: Map<String, Double>? = null,
    @param:Json(name = "dt_txt") val dtTxt: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherCoord(
    @param:Json(name = "lon") val lon: Double = 0.0,
    @param:Json(name = "lat") val lat: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class OpenWeatherDescription(
    @param:Json(name = "id") val id: Int = 800,
    @param:Json(name = "main") val main: String = "",
    @param:Json(name = "description") val description: String = "",
    @param:Json(name = "icon") val icon: String = ""
)

@JsonClass(generateAdapter = true)
data class OpenWeatherMain(
    @param:Json(name = "temp") val temp: Double = 20.0,
    @param:Json(name = "feels_like") val feelsLike: Double = 20.0,
    @param:Json(name = "temp_min") val tempMin: Double = 18.0,
    @param:Json(name = "temp_max") val tempMax: Double = 22.0,
    @param:Json(name = "pressure") val pressure: Double = 1013.0,
    @param:Json(name = "humidity") val humidity: Int = 50,
    @param:Json(name = "sea_level") val seaLevel: Double? = null,
    @param:Json(name = "grnd_level") val grndLevel: Double? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherWind(
    @param:Json(name = "speed") val speed: Double = 0.0,
    @param:Json(name = "deg") val deg: Int = 0,
    @param:Json(name = "gust") val gust: Double? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherClouds(
    @param:Json(name = "all") val all: Int = 0
)

@JsonClass(generateAdapter = true)
data class OpenWeatherSys(
    @param:Json(name = "type") val type: Int? = null,
    @param:Json(name = "id") val id: Long? = null,
    @param:Json(name = "country") val country: String? = null,
    @param:Json(name = "sunrise") val sunrise: Long = 0L,
    @param:Json(name = "sunset") val sunset: Long = 0L
)

@JsonClass(generateAdapter = true)
data class OpenWeatherCity(
    @param:Json(name = "id") val id: Long = 0L,
    @param:Json(name = "name") val name: String = "",
    @param:Json(name = "coord") val coord: OpenWeatherCoord? = null,
    @param:Json(name = "country") val country: String = "",
    @param:Json(name = "population") val population: Long? = null,
    @param:Json(name = "timezone") val timezone: Int = 0,
    @param:Json(name = "sunrise") val sunrise: Long? = null,
    @param:Json(name = "sunset") val sunset: Long? = null
)

@JsonClass(generateAdapter = true)
data class OpenWeatherGeocodingItem(
    @param:Json(name = "name") val name: String,
    @param:Json(name = "lat") val lat: Double,
    @param:Json(name = "lon") val lon: Double,
    @param:Json(name = "country") val country: String? = null,
    @param:Json(name = "state") val state: String? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingResponse(
    @param:Json(name = "results") val results: List<GeocodingResult>? = null
)

@JsonClass(generateAdapter = true)
data class GeocodingResult(
    @param:Json(name = "id") val id: Long,
    @param:Json(name = "name") val name: String,
    @param:Json(name = "latitude") val latitude: Double,
    @param:Json(name = "longitude") val longitude: Double,
    @param:Json(name = "country") val country: String? = null,
    @param:Json(name = "admin1") val admin1: String? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoForecastResponse(
    @param:Json(name = "current") val current: OpenMeteoCurrent? = null,
    @param:Json(name = "hourly") val hourly: OpenMeteoHourly? = null,
    @param:Json(name = "daily") val daily: OpenMeteoDaily? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoCurrent(
    @param:Json(name = "temperature_2m") val temperature2m: Double,
    @param:Json(name = "relative_humidity_2m") val relativeHumidity2m: Int,
    @param:Json(name = "apparent_temperature") val apparentTemperature: Double,
    @param:Json(name = "is_day") val isDay: Int,
    @param:Json(name = "precipitation") val precipitation: Double,
    @param:Json(name = "weather_code") val weatherCode: Int,
    @param:Json(name = "wind_speed_10m") val windSpeed10m: Double,
    @param:Json(name = "wind_direction_10m") val windDirection10m: Int,
    @param:Json(name = "surface_pressure") val surfacePressure: Double
)

@JsonClass(generateAdapter = true)
data class OpenMeteoHourly(
    @param:Json(name = "time") val time: List<String>? = null,
    @param:Json(name = "temperature_2m") val temperature2m: List<Double>? = null,
    @param:Json(name = "precipitation_probability") val precipitationProbability: List<Int>? = null,
    @param:Json(name = "precipitation") val precipitation: List<Double>? = null,
    @param:Json(name = "weather_code") val weatherCode: List<Int>? = null
)

@JsonClass(generateAdapter = true)
data class OpenMeteoDaily(
    @param:Json(name = "time") val time: List<String>? = null,
    @param:Json(name = "weather_code") val weatherCode: List<Int>? = null,
    @param:Json(name = "temperature_2m_max") val temperature2mMax: List<Double>? = null,
    @param:Json(name = "temperature_2m_min") val temperature2mMin: List<Double>? = null,
    @param:Json(name = "sunrise") val sunrise: List<String>? = null,
    @param:Json(name = "sunset") val sunset: List<String>? = null,
    @param:Json(name = "uv_index_max") val uvIndexMax: List<Double>? = null,
    @param:Json(name = "precipitation_probability_max") val precipitationProbabilityMax: List<Int>? = null
)
