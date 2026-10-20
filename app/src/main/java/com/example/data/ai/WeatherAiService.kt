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
        return try {
            withTimeout(35_000L) {
                val prompt = buildOverviewPrompt(report, location, language, tempUnit)
                Log.d(TAG, "Generating AI overview with Gemini in ${language.displayName} (${language.code})")

                val response = structuredModel.generateContent(prompt)
                val jsonText = response.text ?: throw IllegalStateException("Gemini returned empty response text")

                val json = JSONObject(jsonText)
                WeatherAiOverview(
                    headline = json.optString("headline").ifBlank { getFallbackHeadline(location, report, language) },
                    narrative = json.optString("narrative").ifBlank { getFallbackNarrative(location, report, language, tempUnit) },
                    clothingAdvice = json.optString("clothingAdvice").ifBlank { getFallbackClothing(report, language) },
                    outdoorAdvice = json.optString("outdoorAdvice").ifBlank { getFallbackOutdoors(report, language) },
                    alertBadge = json.optString("alertBadge").ifBlank { getFallbackBadge(report, language) },
                    languageCode = language.code
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Gemini call failed or unavailable (${e.message}), generating localized atmospheric overview in ${language.displayName}")
            generateFallbackOverview(report, location, language, tempUnit)
        }
    }

    fun generateFallbackOverview(
        report: FullWeatherReport,
        location: CityLocation,
        language: AppLanguage,
        tempUnit: TemperatureUnit
    ): WeatherAiOverview {
        return WeatherAiOverview(
            headline = getFallbackHeadline(location, report, language),
            narrative = getFallbackNarrative(location, report, language, tempUnit),
            clothingAdvice = getFallbackClothing(report, language),
            outdoorAdvice = getFallbackOutdoors(report, language),
            alertBadge = getFallbackBadge(report, language),
            languageCode = language.code
        )
    }

    fun askQuestionStream(
        question: String,
        report: FullWeatherReport,
        location: CityLocation,
        language: AppLanguage,
        tempUnit: TemperatureUnit
    ): Flow<String> = flow {
        try {
            withTimeout(30_000L) {
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

                var receivedAny = false
                streamingModel.generateContentStream(contextPrompt).collect { chunk ->
                    val text = chunk.text.orEmpty()
                    if (text.isNotEmpty()) {
                        receivedAny = true
                        emit(text)
                    }
                }
                if (!receivedAny) {
                    emit(generateFallbackAnswer(question, report, location, language, tempUnit))
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Gemini Q&A stream failed (${e.message}), providing localized answer in ${language.displayName}")
            emit(generateFallbackAnswer(question, report, location, language, tempUnit))
        }
    }

    private fun getFallbackHeadline(
        location: CityLocation,
        report: FullWeatherReport,
        language: AppLanguage
    ): String {
        val cond = com.example.util.WeatherLocalization.getConditionTitle(report.current.condition, language)
        return when (language) {
            AppLanguage.SPANISH -> "$cond en ${location.name} hoy"
            AppLanguage.FRENCH -> "$cond à ${location.name} aujourd'hui"
            AppLanguage.GERMAN -> "$cond in ${location.name} heute"
            AppLanguage.ITALIAN -> "$cond a ${location.name} oggi"
            AppLanguage.HINDI -> "${location.name} में आज $cond"
            AppLanguage.JAPANESE -> "今日の${location.name}は$cond"
            AppLanguage.KOREAN -> "오늘 ${location.name}의 날씨는 $cond"
            AppLanguage.PORTUGUESE -> "$cond em ${location.name} hoje"
            AppLanguage.POLISH -> "$cond w ${location.name} dzisiaj"
            AppLanguage.ENGLISH -> "$cond in ${location.name} today"
        }
    }

    private fun getFallbackNarrative(
        location: CityLocation,
        report: FullWeatherReport,
        language: AppLanguage,
        tempUnit: TemperatureUnit
    ): String {
        val curT = tempUnit.formatValue(report.current.temperature)
        val high = tempUnit.formatValue(report.current.tempMax)
        val low = tempUnit.formatValue(report.current.tempMin)
        val cond = com.example.util.WeatherLocalization.getConditionTitle(report.current.condition, language)

        return when (language) {
            AppLanguage.SPANISH -> "El tiempo actual en ${location.name} presenta $cond con $curT. Se prevé una máxima de $high y una mínima de $low, con humedad del ${report.current.humidity}% y viento a ${Math.round(report.current.windSpeed)} km/h."
            AppLanguage.FRENCH -> "La météo actuelle à ${location.name} indique un ciel $cond avec $curT. Les températures atteindront un pic de $high pour une minimale de $low, avec ${report.current.humidity}% d'humidité."
            AppLanguage.GERMAN -> "Das aktuelle Wetter in ${location.name} zeigt $cond bei $curT. Die Höchsttemperatur liegt bei $high und die Tiefsttemperatur bei $low, mit einer Luftfeuchtigkeit von ${report.current.humidity}%."
            AppLanguage.ITALIAN -> "Il meteo attuale a ${location.name} presenta condizioni di $cond con $curT. È prevista una massima di $high e una minima di $low, con umidità al ${report.current.humidity}%."
            AppLanguage.HINDI -> "${location.name} में वर्तमान मौसम $cond के साथ $curT है। आज का अधिकतम तापमान $high और न्यूनतम $low रहने का अनुमान है, जिसमें आर्द्रता ${report.current.humidity}% है।"
            AppLanguage.JAPANESE -> "${location.name}の現在の天況は${cond}で、気温は${curT}です。最高気温は${high}、最低気温は${low}となる見込みで、湿度は${report.current.humidity}%です。"
            AppLanguage.KOREAN -> "${location.name}의 현재 날씨는 ${cond} 상태이며 기온은 ${curT}입니다. 최고 기온은 ${high}, 최저 기온은 ${low}로 예상되며 습도는 ${report.current.humidity}%입니다."
            AppLanguage.PORTUGUESE -> "O clima atual em ${location.name} apresenta $cond com $curT. Espera-se máxima de $high e mínima de $low, com umidade em ${report.current.humidity}% e vento de ${Math.round(report.current.windSpeed)} km/h."
            AppLanguage.POLISH -> "Aktualna pogoda w ${location.name} to $cond przy temperaturze $curT. Prognozowana temperatura maksymalna to $high, minimalna $low, a wilgotność wynosi ${report.current.humidity}%."
            AppLanguage.ENGLISH -> "Current atmospheric conditions in ${location.name} show $cond at $curT. Temperatures will reach a high of $high and a low of $low, with ${report.current.humidity}% humidity and winds at ${Math.round(report.current.windSpeed)} km/h."
        }
    }

    private fun getFallbackClothing(report: FullWeatherReport, language: AppLanguage): String {
        val temp = report.current.temperature
        val isRain = report.current.condition == WeatherCondition.RAIN ||
                report.current.condition == WeatherCondition.LIGHT_RAIN ||
                report.current.condition == WeatherCondition.THUNDERSTORM

        return when (language) {
            AppLanguage.SPANISH -> if (isRain) "Lleva paraguas o impermeable y calzado resistente al agua."
            else if (temp >= 26.0) "Usa ropa ligera y transpirable, gafas de sol y mantente hidratado."
            else if (temp >= 16.0) "Una camisa cómoda con una chaqueta ligera o suéter para la tarde."
            else "Vístete en capas con abrigo cálido, bufanda y guantes."

            AppLanguage.FRENCH -> if (isRain) "Prévoyez un parapluie ou un imperméable et des chaussures étanches."
            else if (temp >= 26.0) "Portez des vêtements légers, des lunettes de soleil et hydratez-vous bien."
            else if (temp >= 16.0) "Une tenue confortable avec une veste légère pour le soir."
            else "Habillez-vous chaudement en superposant les couches avec un manteau."

            AppLanguage.GERMAN -> if (isRain) "Regenschirm oder wasserdichte Jacke sowie festes Schuhwerk mitnehmen."
            else if (temp >= 26.0) "Leichte Sommerkleidung, Sonnenbrille und ausreichend trinken."
            else if (temp >= 16.0) "Bequeme Kleidung mit einer leichten Übergangsjacke."
            else "Warme Schichten, Winterjacke, Schal und Handschuhe empfohlen."

            AppLanguage.ITALIAN -> if (isRain) "Porta un ombrello o impermeabile e scarpe resistenti all'acqua."
            else if (temp >= 26.0) "Indossa abiti leggeri e traspiranti, occhiali da sole e bevi acqua."
            else if (temp >= 16.0) "Abbigliamento comodo con una giacca leggera per la sera."
            else "Vestiti a strati con cappotto caldo e sciarpa."

            AppLanguage.HINDI -> if (isRain) "छाता या रेनकोट साथ रखें और जलरोधी जूते पहनें।"
            else if (temp >= 26.0) "हल्के सूती कपड़े पहनें, धूप का चश्मा लगाएं और पानी पीते रहें।"
            else if (temp >= 16.0) "हल्की जैकेट या स्वेटर के साथ आरामदायक कपड़े पहनें।"
            else "गर्म कपड़े, जैकेट और मफलर पहनकर बाहर निकलें।"

            AppLanguage.JAPANESE -> if (isRain) "傘やレインコート、防水性のある靴をおすすめします。"
            else if (temp >= 26.0) "通気性の良い涼しい服装とサングラスを着用し、水分補給を心がけてください。"
            else if (temp >= 16.0) "過ごしやすい服装に、夕方用の薄手のジャケットを羽織ると安心です。"
            else "暖かいコートやマフラーなどでしっかり防寒対策をしてください。"

            AppLanguage.KOREAN -> if (isRain) "우산이나 방수 자켓을 챙기시고 방수 신발을 착용하세요."
            else if (temp >= 26.0) "가볍고 통풍이 잘되는 옷차림과 선글라스를 착용하시고 수분을 섭취하세요."
            else if (temp >= 16.0) "편안한 옷차림에 쌀쌀한 저녁을 대비해 가벼운 겉옷을 준비하세요."
            else "따뜻한 외투와 목도리로 보온에 유의하세요."

            AppLanguage.PORTUGUESE -> if (isRain) "Leve guarda-chuva ou capa impermeável e calçado resistente à água."
            else if (temp >= 26.0) "Use roupas leves e respiráveis, óculos de sol e mantenha-se hidratado."
            else if (temp >= 16.0) "Roupas confortáveis com uma jaqueta leve para o entardecer."
            else "Vista-se em camadas com casaco quente e cachecol."

            AppLanguage.POLISH -> if (isRain) "Zabierz parasol lub kurtkę przeciwdeszczową i nieprzemakalne buty."
            else if (temp >= 26.0) "Ubierz przewiewne, lekkie ubrania, okulary przeciwsłoneczne i pij wodę."
            else if (temp >= 16.0) "Wygodny strój z lekką kurtką lub swetrem na wieczór."
            else "Ubierz się ciepło na cebulkę z ciepłą kurtką i szalikiem."

            AppLanguage.ENGLISH -> if (isRain) "Carry an umbrella or raincoat and water-resistant footwear."
            else if (temp >= 26.0) "Wear lightweight, breathable clothing, sunglasses, and stay hydrated."
            else if (temp >= 16.0) "Comfortable layers with a light jacket or cardigan for evening."
            else "Layer up with a warm insulated coat, scarf, and gloves."
        }
    }

    private fun getFallbackOutdoors(report: FullWeatherReport, language: AppLanguage): String {
        val uv = report.current.uvIndex
        val isRain = report.current.condition == WeatherCondition.RAIN ||
                report.current.condition == WeatherCondition.LIGHT_RAIN ||
                report.current.condition == WeatherCondition.THUNDERSTORM

        return when (language) {
            AppLanguage.SPANISH -> if (isRain) "Ten precaución en carreteras húmedas y busca actividades bajo techo."
            else if (uv >= 6.0) "Aplica protector solar UV si sales y busca sombra en horas pico del mediodía."
            else "Excelente momento para caminar, hacer ejercicio o pasear al aire libre."

            AppLanguage.FRENCH -> if (isRain) "Chaussées glissantes, privilégiez les activités abritées."
            else if (uv >= 6.0) "Appliquez une crème solaire et évitez l'exposition prolongée en mi-journée."
            else "Excellente opportunité pour le sport, la marche ou les sorties en plein air."

            AppLanguage.GERMAN -> if (isRain) "Vorsicht bei nassen Straßen; Indoor-Aktivitäten empfohlen."
            else if (uv >= 6.0) "Sonnenschutz auftragen und mittags schattige Plätze aufsuchen."
            else "Hervorragende Bedingungen für Spaziergänge, Joggen und Freizeit im Freien."

            AppLanguage.ITALIAN -> if (isRain) "Attenzione all'asfalto bagnato, ideali attività al coperto."
            else if (uv >= 6.0) "Applica protezione solare ed evita il sole diretto nelle ore centrali."
            else "Ottimo momento per camminare, correre o godersi l'aria aperta."

            AppLanguage.HINDI -> if (isRain) "गीली सड़कों पर सावधानी बरतें और इनडोर गतिविधियों को प्राथमिकता दें।"
            else if (uv >= 6.0) "धूप में निकलते समय सनस्क्रीन लगाएं और दोपहर में छांव में रहें।"
            else "टहलने, दौड़ने या बाहरी गतिविधियों के लिए बहुत अच्छा समय है।"

            AppLanguage.JAPANESE -> if (isRain) "足元が滑りやすいため、屋内のアクティビティがおすすめです。"
            else if (uv >= 6.0) "日焼け止めを塗り、日差しの強い正午頃は日陰でお過ごしください。"
            else "ウォーキングやランニングなど、屋外での運動に最適なコンディションです。"

            AppLanguage.KOREAN -> if (isRain) "미끄러운 도로에 주의하시고 실내 활동을 권장합니다."
            else if (uv >= 6.0) "자외선 차단제를 바르고 한낮에는 그늘을 이용하세요."
            else "가벼운 산책이나 조깅 등 야외 활동을 즐기기에 매우 좋습니다."

            AppLanguage.PORTUGUESE -> if (isRain) "Cuidado com pistas molhadas; prefira programas em locais cobertos."
            else if (uv >= 6.0) "Aplique protetor solar e evite exposição direta ao meio-dia."
            else "Ótimo momento para caminhadas, passeios e esportes ao ar livre."

            AppLanguage.POLISH -> if (isRain) "Ostrożnie na śliskich nawierzchniach; polecane zajęcia pod dachem."
            else if (uv >= 6.0) "Zastosuj krem z filtrem UV i unikaj ostrego słońca w południe."
            else "Wspaniałe warunki na spacer, jogging i aktywność na świeżym powietrzu."

            AppLanguage.ENGLISH -> if (isRain) "Exercise caution on wet surfaces; indoor activities recommended."
            else if (uv >= 6.0) "Apply broad-spectrum UV sunscreen and seek shade during midday hours."
            else "Ideal conditions for walking, outdoor recreation, and commuting."
        }
    }

    private fun getFallbackBadge(report: FullWeatherReport, language: AppLanguage): String {
        return when (report.current.condition) {
            WeatherCondition.CLEAR_DAY, WeatherCondition.CLEAR_NIGHT -> when (language) {
                AppLanguage.SPANISH -> "Cielo despejado"
                AppLanguage.FRENCH -> "Ciel dégagé"
                AppLanguage.GERMAN -> "Klarer Himmel"
                AppLanguage.ITALIAN -> "Cielo sereno"
                AppLanguage.HINDI -> "साफ़ आकाश"
                AppLanguage.JAPANESE -> "快晴・日差し良好"
                AppLanguage.KOREAN -> "맑고 쾌청함"
                AppLanguage.PORTUGUESE -> "Céu limpo"
                AppLanguage.POLISH -> "Bezchmurnie"
                AppLanguage.ENGLISH -> "Clear Skies"
            }
            WeatherCondition.RAIN, WeatherCondition.LIGHT_RAIN, WeatherCondition.THUNDERSTORM -> when (language) {
                AppLanguage.SPANISH -> "Llevar paraguas"
                AppLanguage.FRENCH -> "Prendre un parapluie"
                AppLanguage.GERMAN -> "Regenschirm mitnehmen"
                AppLanguage.ITALIAN -> "Porta l'ombrello"
                AppLanguage.HINDI -> "छाता साथ रखें"
                AppLanguage.JAPANESE -> "傘をお持ちください"
                AppLanguage.KOREAN -> "우산 필수"
                AppLanguage.PORTUGUESE -> "Leve guarda-chuva"
                AppLanguage.POLISH -> "Weź parasol"
                AppLanguage.ENGLISH -> "Carry Umbrella"
            }
            else -> com.example.util.WeatherLocalization.getConditionTitle(report.current.condition, language)
        }
    }

    private fun generateFallbackAnswer(
        question: String,
        report: FullWeatherReport,
        location: CityLocation,
        language: AppLanguage,
        tempUnit: TemperatureUnit
    ): String {
        val curT = tempUnit.formatValue(report.current.temperature)
        val cond = com.example.util.WeatherLocalization.getConditionTitle(report.current.condition, language)
        val high = tempUnit.formatValue(report.current.tempMax)
        val low = tempUnit.formatValue(report.current.tempMin)

        val qLower = question.lowercase()
        val isClothing = qLower.contains("wear") || qLower.contains("ropa") || qLower.contains("porter") ||
                qLower.contains("anziehen") || qLower.contains("indoss") || qLower.contains("कपड़े") ||
                qLower.contains("服") || qLower.contains("옷") || qLower.contains("ubr") || qLower.contains("vest")
        val isRain = qLower.contains("rain") || qLower.contains("lluv") || qLower.contains("pluie") ||
                qLower.contains("regen") || qLower.contains("piogg") || qLower.contains("बारिश") ||
                qLower.contains("雨") || qLower.contains("비") || qLower.contains("chuv") || qLower.contains("deszcz")

        return when {
            isClothing -> getFallbackClothing(report, language)
            isRain -> {
                val hasRain = report.current.condition == WeatherCondition.RAIN ||
                        report.current.condition == WeatherCondition.LIGHT_RAIN ||
                        report.current.condition == WeatherCondition.THUNDERSTORM ||
                        report.hourly.any { it.rainProbability >= 40 }
                when (language) {
                    AppLanguage.SPANISH -> if (hasRain) "Sí, hay probabilidad de lluvia hoy. Se recomienda llevar paraguas o impermeable." else "No se esperan lluvias significativas hoy. El cielo estará principalmente $cond."
                    AppLanguage.FRENCH -> if (hasRain) "Oui, des averses sont probables aujourd'hui. Pensez à votre parapluie." else "Aucune précipitation majeure n'est prévue aujourd'hui. Le temps sera $cond."
                    AppLanguage.GERMAN -> if (hasRain) "Ja, heute besteht Regenwahrscheinlichkeit. Nehmen Sie einen Regenschirm mit." else "Größere Regenfälle sind heute nicht zu erwarten. Es bleibt überwiegend $cond."
                    AppLanguage.ITALIAN -> if (hasRain) "Sì, c'è possibilità di pioggia oggi. Porta con te un ombrello." else "Non sono previste precipitazioni significative oggi. Il tempo sarà prevalente $cond."
                    AppLanguage.HINDI -> if (hasRain) "हाँ, आज बारिश की संभावना है। छाता साथ रखें।" else "आज बारिश की कोई बड़ी संभावना नहीं है। मौसम मुख्य रूप से $cond रहेगा।"
                    AppLanguage.JAPANESE -> if (hasRain) "はい、今日は雨の降る可能性があります。折りたたみ傘をお持ちください。" else "大きな雨の心配はなさそうです。概ね${cond}の天気が続く見込みです。"
                    AppLanguage.KOREAN -> if (hasRain) "네, 오늘 비가 내릴 가능성이 있으니 우산을 챙기세요." else "오늘 큰 비 소식은 없습니다. 대체로 $cond 날씨가 이어지겠습니다."
                    AppLanguage.PORTUGUESE -> if (hasRain) "Sim, há probabilidade de chuva hoje. Recomenda-se levar guarda-chuva." else "Não há previsão de chuvas significativas hoje. O tempo estará predominantemente $cond."
                    AppLanguage.POLISH -> if (hasRain) "Tak, istnieje dziś prawdopodobieństwo opadów. Warto zabrać parasol." else "Nie przewiduje się dziś znaczących opadów deszczu. Będzie przeważnie $cond."
                    AppLanguage.ENGLISH -> if (hasRain) "Yes, there is precipitation expected today. Keeping an umbrella handy is advised." else "No major rainfall is expected today. Conditions are mainly $cond."
                }
            }
            else -> {
                when (language) {
                    AppLanguage.SPANISH -> "En ${location.name} la temperatura actual es de $curT con $cond. La máxima será de $high y la mínima de $low con vientos a ${Math.round(report.current.windSpeed)} km/h."
                    AppLanguage.FRENCH -> "À ${location.name}, la température actuelle est de $curT avec un temps $cond. Maximum prévu à $high et minimum à $low."
                    AppLanguage.GERMAN -> "In ${location.name} beträgt die aktuelle Temperatur $curT bei $cond. Die Höchsttemperatur erreicht $high und das Minimum $low."
                    AppLanguage.ITALIAN -> "A ${location.name} la temperatura attuale è $curT con cielo $cond. La massima sarà $high e la minima $low."
                    AppLanguage.HINDI -> "${location.name} में वर्तमान तापमान $curT है और मौसम $cond है। अधिकतम $high और न्यूनतम $low रहेगा।"
                    AppLanguage.JAPANESE -> "${location.name}の現在の気温は${curT}、${cond}です。最高気温は${high}、最低気温は${low}の予報です。"
                    AppLanguage.KOREAN -> "${location.name}의 현재 기온은 ${curT}이며 ${cond} 상태입니다. 최고 ${high}, 최저 ${low}로 예상됩니다."
                    AppLanguage.PORTUGUESE -> "Em ${location.name} a temperatura atual é de $curT com $cond. A máxima será de $high e a mínima de $low."
                    AppLanguage.POLISH -> "W ${location.name} aktualna temperatura wynosi $curT przy pogodzie: $cond. Maksymalna to $high, a minimalna $low."
                    AppLanguage.ENGLISH -> "In ${location.name}, current temperature is $curT with $cond conditions. High today will be $high and low $low with wind at ${Math.round(report.current.windSpeed)} km/h."
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
