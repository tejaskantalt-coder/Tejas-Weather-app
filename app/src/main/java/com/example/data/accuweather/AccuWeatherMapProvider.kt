package com.example.data.accuweather

import androidx.compose.ui.graphics.Color
import com.example.data.model.*
import kotlin.math.cos
import kotlin.math.sin

object AccuWeatherMapProvider {

    fun generateFrames(
        condition: WeatherCondition,
        cityName: String,
        temperature: Double,
        windSpeed: Double = 18.0,
        windDirection: Int = 220
    ): List<AccuWeatherMapFrame> {
        val rad = Math.toRadians(windDirection.toDouble())
        val driftX = (cos(rad) * 0.08f).toFloat()
        val driftY = (sin(rad) * 0.08f).toFloat()

        val timeOffsets = listOf(-60, -40, -20, 0, 20, 40)
        val timeLabels = listOf("-60m", "-40m", "-20m", "Now", "+20m", "+40m")

        val isStormy = condition == WeatherCondition.THUNDERSTORM
        val isRainy = condition == WeatherCondition.RAIN || condition == WeatherCondition.LIGHT_RAIN || isStormy
        val isCloudy = condition == WeatherCondition.OVERCAST || condition == WeatherCondition.PARTLY_CLOUDY_DAY || isRainy

        return timeOffsets.mapIndexed { index, offsetMin ->
            val progressStep = (index - 3) * 0.14f

            val echoes = generatePrecipitationEchoes(progressStep, driftX, driftY, isRainy, isStormy)
            val isotherms = generateTemperatureIsotherms(temperature, progressStep)
            val windVectors = generateWindStreamVectors(windSpeed, rad, progressStep)
            val clouds = generateCloudPatches(progressStep, driftX, driftY, isCloudy)
            val severe = generateSevereStormCells(progressStep, isStormy)

            val minuteCast = when {
                index == 3 && isStormy -> "AccuWeather MinuteCast®: Severe storms active. High lightning frequency."
                index == 3 && isRainy -> "AccuWeather MinuteCast®: Precipitation continuing for next 45 min."
                index == 3 -> "AccuWeather MinuteCast®: No precipitation expected for next 120 minutes."
                offsetMin > 0 -> "AccuWeather Forecast Projection (+${offsetMin} min)"
                else -> "AccuWeather Doppler Radar Archive (${offsetMin} min)"
            }

            AccuWeatherMapFrame(
                frameIndex = index,
                timeLabel = timeLabels[index],
                minutesOffset = offsetMin,
                precipitationEchoes = echoes,
                temperatureIsotherms = isotherms,
                windStreamVectors = windVectors,
                cloudBlobs = clouds,
                severeCells = severe,
                minuteCastSummary = minuteCast
            )
        }
    }

    private fun generatePrecipitationEchoes(
        step: Float,
        dx: Float,
        dy: Float,
        isRainy: Boolean,
        isStormy: Boolean
    ): List<AccuWeatherEcho> {
        val list = mutableListOf<AccuWeatherEcho>()
        val mult = if (isStormy) 1.5f else if (isRainy) 1.2f else 0.75f

        list.add(
            AccuWeatherEcho(
                relX = (-0.15f + step * dx * 2.2f).coerceIn(-0.85f, 0.85f),
                relY = (-0.12f + step * dy * 2.2f).coerceIn(-0.85f, 0.85f),
                radius = 0.36f * mult,
                intensityDbz = if (isStormy) 55 else if (isRainy) 40 else 25,
                color = if (isStormy) Color(0xFFD32F2F) else if (isRainy) Color(0xFFFBC02D) else Color(0xFF4CAF50)
            )
        )

        list.add(
            AccuWeatherEcho(
                relX = (0.28f + step * dx * 2.0f).coerceIn(-0.85f, 0.85f),
                relY = (-0.30f + step * dy * 2.0f).coerceIn(-0.85f, 0.85f),
                radius = 0.28f * mult,
                intensityDbz = if (isStormy) 48 else 32,
                color = if (isStormy) Color(0xFFF57C00) else Color(0xFF388E3C)
            )
        )

        list.add(
            AccuWeatherEcho(
                relX = (-0.40f + step * dx * 2.5f).coerceIn(-0.85f, 0.85f),
                relY = (0.35f + step * dy * 2.5f).coerceIn(-0.85f, 0.85f),
                radius = 0.30f * mult,
                intensityDbz = 20,
                color = Color(0xFF29B6F6)
            )
        )

        if (isStormy || isRainy) {
            list.add(
                AccuWeatherEcho(
                    relX = (-0.12f + step * dx * 2.2f).coerceIn(-0.85f, 0.85f),
                    relY = (-0.09f + step * dy * 2.2f).coerceIn(-0.85f, 0.85f),
                    radius = 0.16f * mult,
                    intensityDbz = 62,
                    color = Color(0xFF9C27B0)
                )
            )
        }

        return list
    }

    private fun generateTemperatureIsotherms(
        baseTemp: Double,
        step: Float
    ): List<AccuWeatherIsotherm> {
        return listOf(
            AccuWeatherIsotherm(-0.4f + step * 0.05f, -0.3f, 0.55f, baseTemp - 2.5, getTempColor(baseTemp - 2.5)),
            AccuWeatherIsotherm(0.0f + step * 0.04f, 0.0f, 0.50f, baseTemp, getTempColor(baseTemp)),
            AccuWeatherIsotherm(0.35f + step * 0.03f, 0.35f, 0.52f, baseTemp + 2.0, getTempColor(baseTemp + 2.0)),
            AccuWeatherIsotherm(-0.2f + step * 0.04f, 0.45f, 0.48f, baseTemp + 1.2, getTempColor(baseTemp + 1.2))
        )
    }

    private fun getTempColor(temp: Double): Color {
        return when {
            temp < 0 -> Color(0xFF1976D2)
            temp < 10 -> Color(0xFF0097A7)
            temp < 20 -> Color(0xFF388E3C)
            temp < 28 -> Color(0xFFFFA000)
            else -> Color(0xFFD32F2F)
        }
    }

    private fun generateWindStreamVectors(
        speed: Double,
        angleRad: Double,
        step: Float
    ): List<AccuWeatherWindVector> {
        val vectors = mutableListOf<AccuWeatherWindVector>()
        val cosA = cos(angleRad).toFloat()
        val sinA = sin(angleRad).toFloat()
        val length = 0.14f

        val gridPoints = listOf(
            Pair(-0.5f, -0.5f), Pair(-0.1f, -0.5f), Pair(0.3f, -0.5f),
            Pair(-0.5f, -0.1f), Pair(-0.1f, -0.1f), Pair(0.3f, -0.1f),
            Pair(-0.5f, 0.3f), Pair(-0.1f, 0.3f), Pair(0.3f, 0.3f)
        )

        for (pt in gridPoints) {
            val sx = pt.first + step * 0.05f
            val sy = pt.second + step * 0.05f
            vectors.add(
                AccuWeatherWindVector(
                    startRelX = sx,
                    startRelY = sy,
                    endRelX = sx + cosA * length,
                    endRelY = sy + sinA * length,
                    speedKmh = speed
                )
            )
        }
        return vectors
    }

    private fun generateCloudPatches(
        step: Float,
        dx: Float,
        dy: Float,
        isCloudy: Boolean
    ): List<AccuWeatherCloudPatch> {
        val count = if (isCloudy) 6 else 3
        val patches = mutableListOf<AccuWeatherCloudPatch>()
        val raw = listOf(
            Triple(-0.3f, -0.2f, 0.48f),
            Triple(0.2f, -0.3f, 0.42f),
            Triple(-0.1f, 0.3f, 0.52f),
            Triple(0.4f, 0.2f, 0.38f),
            Triple(-0.4f, 0.1f, 0.44f),
            Triple(0.1f, -0.4f, 0.36f)
        )

        for (i in 0 until count) {
            val (rx, ry, radius) = raw[i % raw.size]
            patches.add(
                AccuWeatherCloudPatch(
                    relX = (rx + step * dx * 1.6f).coerceIn(-0.85f, 0.85f),
                    relY = (ry + step * dy * 1.6f).coerceIn(-0.85f, 0.85f),
                    radiusX = radius * 1.15f,
                    radiusY = radius * 0.95f,
                    opacity = if (isCloudy) 0.55f else 0.30f
                )
            )
        }
        return patches
    }

    private fun generateSevereStormCells(
        step: Float,
        isStormy: Boolean
    ): List<AccuWeatherSevereCell> {
        if (!isStormy) return emptyList()
        return listOf(
            AccuWeatherSevereCell(
                relX = (-0.18f + step * 0.25f).coerceIn(-0.8f, 0.8f),
                relY = (-0.14f + step * 0.22f).coerceIn(-0.8f, 0.8f),
                cellType = "Severe Thunderstorm Warning",
                lightningCount = 14,
                hailProbability = 60
            )
        )
    }
}
