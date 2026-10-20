package com.example

import com.example.data.accuweather.AccuWeatherMapProvider
import com.example.data.model.WeatherCondition
import org.junit.Assert.*
import org.junit.Test

class WeatherAppTest {

    @Test
    fun testGenerateFramesForThunderstorm() {
        val frames = AccuWeatherMapProvider.generateFrames(
            condition = WeatherCondition.THUNDERSTORM,
            cityName = "New York",
            temperature = 37.0,
            windSpeed = 25.0,
            windDirection = 180
        )

        assertEquals(6, frames.size)
        val nowFrame = frames[3]
        assertEquals("Now", nowFrame.timeLabel)
        assertTrue(nowFrame.minuteCastSummary.contains("MinuteCast"))
        assertTrue(nowFrame.severeCells.isNotEmpty())
        assertTrue(nowFrame.precipitationEchoes.isNotEmpty())
    }

    @Test
    fun testGenerateFramesForPartlyCloudy() {
        val frames = AccuWeatherMapProvider.generateFrames(
            condition = WeatherCondition.PARTLY_CLOUDY_DAY,
            cityName = "Home",
            temperature = 37.0,
            windSpeed = 14.0,
            windDirection = 120
        )

        assertEquals(6, frames.size)
        val nowFrame = frames[3]
        assertTrue(nowFrame.temperatureIsotherms.isNotEmpty())
        assertTrue(nowFrame.windStreamVectors.isNotEmpty())
        assertTrue(nowFrame.cloudBlobs.isNotEmpty())
    }

    @Test
    fun testWeatherLocalizationLanguages() {
        val spanish = com.example.util.WeatherLocalization.getConditionTitle(WeatherCondition.CLEAR_DAY, com.example.data.model.AppLanguage.SPANISH)
        assertEquals("Soleado", spanish)

        val french = com.example.util.WeatherLocalization.getConditionTitle(WeatherCondition.CLEAR_DAY, com.example.data.model.AppLanguage.FRENCH)
        assertEquals("Ensoleillé", french)

        val german = com.example.util.WeatherLocalization.getConditionTitle(WeatherCondition.CLEAR_DAY, com.example.data.model.AppLanguage.GERMAN)
        assertEquals("Sonnig", german)

        val polish = com.example.util.WeatherLocalization.getSunsetLabel(com.example.data.model.AppLanguage.POLISH)
        assertEquals("Zachód słońca", polish)
    }

    @Test
    fun testMultilingualAiOverviewFallback() {
        val aiService = com.example.data.ai.WeatherAiService()
        val location = com.example.data.model.CityLocation("Madrid", "Spain", "Madrid", 40.4168, -3.7038)
        val currentWeather = com.example.data.model.CurrentWeather(
            temperature = 32.0,
            feelsLike = 34.0,
            tempMin = 22.0,
            tempMax = 35.0,
            humidity = 40,
            windSpeed = 12.0,
            windDirection = 90,
            surfacePressure = 1015.0,
            uvIndex = 8.0,
            visibilityKm = 10.0,
            dewPoint = 14.0,
            aqi = 25,
            condition = WeatherCondition.CLEAR_DAY,
            isDay = true,
            dayAndTimeString = "Mon, 2:00 PM",
            updateTime = "2:00 PM"
        )
        val report = com.example.data.model.FullWeatherReport(
            location = location,
            current = currentWeather,
            hourly = emptyList(),
            daily = emptyList(),
            sunTimes = com.example.data.model.SunriseSunset("07:00", "21:30", 0.5f, "4h remaining"),
            headlineSummary = "Sunny day"
        )

        val spanishOverview = aiService.generateFallbackOverview(
            report = report,
            location = location,
            language = com.example.data.model.AppLanguage.SPANISH,
            tempUnit = com.example.data.model.TemperatureUnit.CELSIUS
        )
        assertEquals("es", spanishOverview.languageCode)
        assertTrue(spanishOverview.headline.contains("Soleado") || spanishOverview.headline.contains("Madrid"))
        assertTrue(spanishOverview.clothingAdvice.isNotBlank())
        assertTrue(spanishOverview.outdoorAdvice.isNotBlank())

        val japaneseOverview = aiService.generateFallbackOverview(
            report = report,
            location = location,
            language = com.example.data.model.AppLanguage.JAPANESE,
            tempUnit = com.example.data.model.TemperatureUnit.CELSIUS
        )
        assertEquals("ja", japaneseOverview.languageCode)
        assertTrue(japaneseOverview.headline.contains("Madrid"))
    }
}
