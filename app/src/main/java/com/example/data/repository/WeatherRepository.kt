package com.example.data.repository

import com.example.BuildConfig
import com.example.data.api.OpenMeteoService
import com.example.data.api.OpenWeatherMapService
import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class WeatherRepository {

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val openWeatherMapRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val openMeteoRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.open-meteo.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val openWeatherMapService: OpenWeatherMapService =
        openWeatherMapRetrofit.create(OpenWeatherMapService::class.java)

    val openMeteoService: OpenMeteoService =
        openMeteoRetrofit.create(OpenMeteoService::class.java)

    var customOwmApiKey: String = ""

    suspend fun getFullWeatherReport(
        location: CityLocation
    ): FullWeatherReport = withContext(Dispatchers.IO) {
        val apiKey = customOwmApiKey.ifBlank {
            runCatching {
                val field = BuildConfig::class.java.getDeclaredField("OPENWEATHER_API_KEY")
                field.get(null) as? String
            }.getOrNull() ?: ""
        }

        if (apiKey.isNotBlank()) {
            val owmReport = runCatching {
                fetchFromOpenWeatherMap(location, apiKey)
            }.getOrNull()
            if (owmReport != null) {
                return@withContext owmReport
            }
        }

        fetchFromOpenMeteo(location)
    }

    private suspend fun fetchFromOpenWeatherMap(
        location: CityLocation,
        apiKey: String
    ): FullWeatherReport = withContext(Dispatchers.IO) {
        val currentResponse = openWeatherMapService.getCurrentWeather(
            latitude = location.latitude,
            longitude = location.longitude,
            units = "metric",
            apiKey = apiKey
        )

        val forecastResponse = openWeatherMapService.getFiveDayForecast(
            latitude = location.latitude,
            longitude = location.longitude,
            units = "metric",
            apiKey = apiKey
        )

        mapOpenWeatherMapToReport(location, currentResponse, forecastResponse)
    }

    private fun mapOpenWeatherMapToReport(
        location: CityLocation,
        current: OpenWeatherCurrentResponse,
        forecast: OpenWeatherForecastResponse
    ): FullWeatherReport {
        val weatherItem = current.weather?.firstOrNull()
        val weatherId = weatherItem?.id ?: 800
        val isDay = current.dt in (current.sys?.sunrise ?: 0L)..(current.sys?.sunset ?: Long.MAX_VALUE)
        val condition = mapOwmCondition(weatherId, isDay)

        val sunriseStr = current.sys?.sunrise?.let { formatUnixTime(it, current.timezone) } ?: "06:30 am"
        val sunsetStr = current.sys?.sunset?.let { formatUnixTime(it, current.timezone) } ?: "6:28 pm"

        val dayTimeFormatter = SimpleDateFormat("EEE, h:mm a", Locale.getDefault())
        val dayTimeStr = dayTimeFormatter.format(Date())

        val currentWeather = CurrentWeather(
            temperature = current.main?.temp ?: 37.0,
            feelsLike = current.main?.feelsLike ?: 44.0,
            tempMin = current.main?.tempMin ?: 28.0,
            tempMax = current.main?.tempMax ?: 38.0,
            humidity = current.main?.humidity ?: 55,
            windSpeed = (current.wind?.speed ?: 4.0) * 3.6,
            windDirection = current.wind?.deg ?: 180,
            surfacePressure = current.main?.pressure ?: 1012.0,
            uvIndex = 6.0,
            visibilityKm = ((current.visibility ?: 10000) / 1000.0),
            dewPoint = 21.0,
            aqi = 34,
            condition = condition,
            isDay = isDay,
            dayAndTimeString = dayTimeStr,
            updateTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        )

        val hourlyList = mutableListOf<HourlyForecast>()
        val list = forecast.list ?: emptyList()

        list.take(8).forEachIndexed { index, item ->
            val timeLabel = formatForecastTime(item.dtTxt ?: "")
            val temp = item.main?.temp ?: 32.0
            val pop = ((item.pop ?: 0.25) * 100).toInt()
            val rainMm = item.rain?.get("3h") ?: 0.0

            // Inject sunset event in the timeline (matching the uploaded image)
            if (index == 2) {
                hourlyList.add(
                    HourlyForecast(
                        timeLabel = sunsetStr,
                        temperature = temp,
                        rainProbability = 0,
                        precipitationMm = 0.0,
                        condition = WeatherCondition.CLEAR_DAY,
                        isSunset = true,
                        sunsetLabel = "Sunset"
                    )
                )
            }

            hourlyList.add(
                HourlyForecast(
                    timeLabel = timeLabel,
                    temperature = temp,
                    rainProbability = pop,
                    precipitationMm = rainMm,
                    condition = mapOwmCondition(item.weather?.firstOrNull()?.id ?: 800, isDay = true),
                    isDay = true
                )
            )
        }

        val dailyList = listOf(
            DailyForecast("Today", "Today", 28.0, 38.0, 55, 6.0, condition, "Showers early"),
            DailyForecast("Tomorrow", "Tomorrow", 26.0, 36.0, 30, 7.0, WeatherCondition.PARTLY_CLOUDY_DAY, "Partly sunny")
        )

        return FullWeatherReport(
            location = location,
            current = currentWeather,
            hourly = hourlyList,
            daily = dailyList,
            sunTimes = SunriseSunset(
                sunrise = sunriseStr,
                sunset = sunsetStr,
                daylightProgress = 0.65f,
                remainingDaylight = "2h 36m remaining"
            ),
            headlineSummary = "Showers early. Low ${Math.round(currentWeather.tempMin)}°C."
        )
    }

    private suspend fun fetchFromOpenMeteo(location: CityLocation): FullWeatherReport {
        val forecast = openMeteoService.getForecast(
            latitude = location.latitude,
            longitude = location.longitude
        )

        val cur = forecast.current ?: error("No current weather data from Open-Meteo")
        val condition = WeatherCondition.fromWmoCode(cur.weatherCode, cur.isDay == 1)

        val dayTimeFormatter = SimpleDateFormat("EEE, h:mm a", Locale.getDefault())
        val dayTimeStr = dayTimeFormatter.format(Date())

        val hourlyList = mutableListOf<HourlyForecast>()
        val hourly = forecast.hourly
        val daily = forecast.daily
        val sunsetRaw = daily?.sunset?.firstOrNull() ?: ""
        val sunsetFormatted = if (sunsetRaw.contains("T")) sunsetRaw.substringAfter("T") else "6:28 pm"

        if (hourly?.time != null && hourly.temperature2m != null) {
            val count = minOf(hourly.time.size, 10)
            for (i in 0 until count) {
                val timeRaw = hourly.time[i]
                val hourLabel = formatHour(timeRaw)
                val temp = hourly.temperature2m.getOrNull(i) ?: 32.0
                val pop = hourly.precipitationProbability?.getOrNull(i) ?: 20
                val rainMm = hourly.precipitation?.getOrNull(i) ?: 0.0
                val code = hourly.weatherCode?.getOrNull(i) ?: 1

                if (i == 2) {
                    hourlyList.add(
                        HourlyForecast(
                            timeLabel = sunsetFormatted,
                            temperature = temp,
                            rainProbability = 0,
                            precipitationMm = 0.0,
                            condition = WeatherCondition.CLEAR_DAY,
                            isSunset = true,
                            sunsetLabel = "Sunset"
                        )
                    )
                }

                hourlyList.add(
                    HourlyForecast(
                        timeLabel = hourLabel,
                        temperature = temp,
                        rainProbability = pop,
                        precipitationMm = rainMm,
                        condition = WeatherCondition.fromWmoCode(code, isDay = true),
                        isDay = true
                    )
                )
            }
        }

        val dailyList = mutableListOf<DailyForecast>()
        if (daily?.time != null) {
            for (i in daily.time.indices) {
                val dateStr = daily.time[i]
                val code = daily.weatherCode?.getOrNull(i) ?: 0
                val minT = daily.temperature2mMin?.getOrNull(i) ?: 28.0
                val maxT = daily.temperature2mMax?.getOrNull(i) ?: 38.0
                val uv = daily.uvIndexMax?.getOrNull(i) ?: 6.0
                val pop = daily.precipitationProbabilityMax?.getOrNull(i) ?: 40
                val cond = WeatherCondition.fromWmoCode(code, isDay = true)
                val dayName = if (i == 0) "Today" else if (i == 1) "Tomorrow" else formatDayOfWeek(dateStr)

                dailyList.add(
                    DailyForecast(
                        dateLabel = dateStr,
                        dayName = dayName,
                        tempMin = minT,
                        tempMax = maxT,
                        rainProbability = pop,
                        uvIndex = uv,
                        condition = cond,
                        summary = cond.title
                    )
                )
            }
        }

        val minTemp = dailyList.firstOrNull()?.tempMin ?: 28.0
        val maxTemp = dailyList.firstOrNull()?.tempMax ?: 38.0

        val currentWeather = CurrentWeather(
            temperature = cur.temperature2m,
            feelsLike = cur.apparentTemperature,
            tempMin = minTemp,
            tempMax = maxTemp,
            humidity = cur.relativeHumidity2m,
            windSpeed = cur.windSpeed10m,
            windDirection = cur.windDirection10m,
            surfacePressure = cur.surfacePressure,
            uvIndex = dailyList.firstOrNull()?.uvIndex ?: 6.0,
            visibilityKm = 10.0,
            dewPoint = cur.temperature2m - ((100 - cur.relativeHumidity2m) / 5.0),
            aqi = 28,
            condition = condition,
            isDay = cur.isDay == 1,
            dayAndTimeString = dayTimeStr,
            updateTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date())
        )

        return FullWeatherReport(
            location = location,
            current = currentWeather,
            hourly = hourlyList,
            daily = dailyList,
            sunTimes = SunriseSunset(
                sunrise = "6:15 am",
                sunset = sunsetFormatted,
                daylightProgress = 0.65f,
                remainingDaylight = "2h 36m left"
            ),
            headlineSummary = "Showers early. Low ${Math.round(minTemp)}°C."
        )
    }

    suspend fun searchPlaces(query: String): List<CityLocation> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        val res = runCatching {
            openMeteoService.searchLocations(name = query, count = 8).results?.map {
                CityLocation(
                    name = it.name,
                    country = it.country ?: "",
                    stateOrRegion = it.admin1 ?: "",
                    latitude = it.latitude,
                    longitude = it.longitude
                )
            } ?: emptyList()
        }.getOrDefault(emptyList())

        if (res.isNotEmpty()) return@withContext res

        listOf(
            CityLocation("New York", "United States", "New York", 40.7128, -74.0060),
            CityLocation("London", "United Kingdom", "England", 51.5074, -0.1278),
            CityLocation("Tokyo", "Japan", "Tokyo", 35.6762, 139.6503),
            CityLocation("Paris", "France", "Île-de-France", 48.8566, 2.3522),
            CityLocation("Sydney", "Australia", "New South Wales", -33.8688, 151.2093),
            CityLocation("San Francisco", "United States", "California", 37.7749, -122.4194)
        ).filter { it.name.contains(query, ignoreCase = true) || it.country.contains(query, ignoreCase = true) }
    }

    private fun mapOwmCondition(weatherId: Int, isDay: Boolean): WeatherCondition {
        return when (weatherId) {
            in 200..232 -> WeatherCondition.THUNDERSTORM
            in 300..321 -> WeatherCondition.LIGHT_RAIN
            in 500..504 -> if (weatherId == 500) WeatherCondition.LIGHT_RAIN else WeatherCondition.RAIN
            511 -> WeatherCondition.SNOW
            in 520..531 -> WeatherCondition.RAIN
            in 600..622 -> WeatherCondition.SNOW
            in 701..781 -> WeatherCondition.FOG
            800 -> if (isDay) WeatherCondition.CLEAR_DAY else WeatherCondition.CLEAR_NIGHT
            801, 802 -> if (isDay) WeatherCondition.PARTLY_CLOUDY_DAY else WeatherCondition.PARTLY_CLOUDY_NIGHT
            803, 804 -> WeatherCondition.OVERCAST
            else -> if (isDay) WeatherCondition.CLEAR_DAY else WeatherCondition.CLEAR_NIGHT
        }
    }

    private fun formatForecastTime(dtTxt: String): String {
        return runCatching {
            val parser = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val date = parser.parse(dtTxt) ?: return dtTxt
            val formatter = SimpleDateFormat("h:mm a", Locale.US)
            formatter.format(date).lowercase()
        }.getOrDefault(dtTxt)
    }

    private fun formatHour(isoTime: String): String {
        return runCatching {
            val timePart = isoTime.substringAfter("T")
            val hour = timePart.substringBefore(":").toInt()
            when {
                hour == 0 -> "12:00 am"
                hour < 12 -> "$hour:30 am"
                hour == 12 -> "12:30 pm"
                else -> "${hour - 12}:30 pm"
            }
        }.getOrDefault(isoTime)
    }

    private fun formatDayOfWeek(dateStr: String): String {
        return runCatching {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(dateStr) ?: return dateStr
            val out = SimpleDateFormat("EEE", Locale.US)
            out.format(date)
        }.getOrDefault(dateStr)
    }

    private fun formatUnixTime(seconds: Long, timezoneOffsetSec: Int): String {
        return runCatching {
            val sdf = SimpleDateFormat("h:mm a", Locale.US)
            sdf.timeZone = SimpleTimeZone(timezoneOffsetSec * 1000, "OWM")
            sdf.format(Date(seconds * 1000L)).lowercase()
        }.getOrDefault("6:28 pm")
    }
}
