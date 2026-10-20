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
}
