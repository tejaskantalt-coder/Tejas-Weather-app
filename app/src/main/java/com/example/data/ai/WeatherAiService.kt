package com.example.data.ai

import android.util.Log
import com.example.data.model.*
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "WeatherAiService"

data class WeatherAiOverview(
    val headline: String,
    val narrative: String,
    val clothingAdvice: String,
    val outdoorAdvice: String,
    val alertBadge: String,
    val languageCode: String
)

class WeatherAiService {

    // Structured JSON Schema for weather overview
    private val overviewSchema = Schema.obj(
        properties = mapOf(
            "headline" to Schema.string(description = "Brief engaging weather headline in the target language"),
            "narrative" to Schema.string(description = "2-3 sentence comprehensive atmospheric summary for today and tonight in the target language"),
            "clothingAdvice" to Schema.string(description = "Specific clothing, footwear, and accessory recommendation (layers, jacket, sunglasses, umbrella) in the target language"),
            "outdoorAdvice" to Schema.string(description = "Advice for outdoor activities, commuting, sports, and UV/heat/rain precautions in the target language"),
            "alertBadge" to Schema.string(description = "Short 2-4 word highlight badge (e.g., 'Carry Umbrella', 'Ideal for Walking', 'High UV Index') in the target language")
        )
    )

    // Lazy initialization of Gemini 3.8 Flash model with JSON schema
    private val structuredModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = overviewSchema
            }
        )
    }

    // Lazy initialization of Gemini 3.8 Flash for streaming Q&A
    private val streamingModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    suspend fun generateOverview(
        report: FullWeatherReport,
        location: CityLocation,
        language: AppLanguage,
        tempUnit: TemperatureUnit
    ): WeatherAiOverview {
        return withTimeout(60_000L) {
            val prompt = buildOverviewPrompt(report, location, language, tempUnit)
            Log.d(TAG, "Generating AI overview in ${language.displayName} (${language.code})")

            val response = structuredModel.generateContent(prompt)
            val jsonText = response.text ?: throw IllegalStateException("Gemini returned empty response text")

            val json = JSONObject(jsonText)
            WeatherAiOverview(
                headline = json.optString("headline", "Weather Overview for ${location.name}"),
                narrative = json.optString("narrative", report.headlineSummary),
                clothingAdvice = json.optString("clothingAdvice", "Dress comfortably according to current temperature."),
                outdoorAdvice = json.optString("outdoorAdvice", "Enjoy your day outdoors while checking local updates."),
                alertBadge = json.optString("alertBadge", report.current.condition.title),
                languageCode = language.code
            )
        }
    }

    fun askQuestionStream(
        question: String,
        report: FullWeatherReport,
        location: CityLocation,
        language: AppLanguage,
        tempUnit: TemperatureUnit
    ): Flow<String> = flow {
        withTimeout(60_000L) {
            val contextPrompt = """
                You are an intelligent, friendly personal meteorologist and weather AI assistant.
                Location: ${location.name}, ${location.stateOrRegion.ifBlank { location.country }}
                Current Weather: ${report.current.condition.title}, ${tempUnit.formatValue(report.current.temperature)} (Feels like ${tempUnit.formatValue(report.current.feelsLike)})
                High: ${tempUnit.formatValue(report.current.tempMax)}, Low: ${tempUnit.formatValue(report.current.tempMin)}
                Humidity: ${report.current.humidity}%, Wind: ${report.current.windSpeed} km/h, UV Index: ${report.current.uvIndex}
                Summary: ${report.headlineSummary}

                CRITICAL LANGUAGE INSTRUCTION:
                The user has selected the language: ${language.displayName} (${language.nativeName}, code: '${language.code}').
                You MUST answer the question strictly and entirely in ${language.displayName} (${language.nativeName}).
                Do not switch to English unless the selected language is English.

                User Question: $question
                Provide a helpful, direct, and conversational answer in ${language.displayName} in 2-4 sentences.
            """.trimIndent()

            streamingModel.generateContentStream(contextPrompt).collect { chunk ->
                val text = chunk.text.orEmpty()
                if (text.isNotEmpty()) {
                    emit(text)
                }
            }
        }
    }

    private fun buildOverviewPrompt(
        report: FullWeatherReport,
        location: CityLocation,
        language: AppLanguage,
        tempUnit: TemperatureUnit
    ): String {
        val nextHoursSummary = report.hourly.take(6).joinToString(separator = "; ") {
            "${it.timeLabel}: ${tempUnit.formatValue(it.temperature)}, ${it.condition.title}, Rain: ${it.rainProbability}%"
        }

        return """
            You are an expert meteorologist providing a personalized atmospheric AI Weather Overview.

            Location: ${location.name}, ${location.stateOrRegion.ifBlank { location.country }} (Coordinates: ${location.latitude}, ${location.longitude})
            Current Condition: ${report.current.condition.title}
            Current Temperature: ${tempUnit.formatValue(report.current.temperature)} (Feels like ${tempUnit.formatValue(report.current.feelsLike)})
            Today's High / Low: ${tempUnit.formatValue(report.current.tempMax)} / ${tempUnit.formatValue(report.current.tempMin)}
            Humidity: ${report.current.humidity}%, Wind Speed: ${report.current.windSpeed} km/h, UV Index: ${report.current.uvIndex}
            Pressure: ${report.current.surfacePressure} hPa, Visibility: ${report.current.visibilityKm} km
            Next hours outlook: $nextHoursSummary

            CRITICAL LANGUAGE REQUIREMENT:
            Target Language: ${language.displayName} (${language.nativeName}, code: '${language.code}').
            You MUST generate ALL fields of the JSON output strictly and completely in ${language.displayName} (${language.nativeName}).
            Every single sentence must be written fluently in ${language.displayName}.
        """.trimIndent()
    }
}
