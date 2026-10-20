package com.example.util

import com.example.data.model.AppLanguage
import com.example.data.model.WeatherCondition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object WeatherLocalization {

    fun getConditionTitle(condition: WeatherCondition, language: AppLanguage): String {
        return when (language) {
            AppLanguage.SPANISH -> when (condition) {
                WeatherCondition.CLEAR_DAY -> "Soleado"
                WeatherCondition.CLEAR_NIGHT -> "Despejado"
                WeatherCondition.PARTLY_CLOUDY_DAY -> "Parcialmente nublado"
                WeatherCondition.PARTLY_CLOUDY_NIGHT -> "Parcialmente nublado"
                WeatherCondition.OVERCAST -> "Nublado"
                WeatherCondition.LIGHT_RAIN -> "Lluvia ligera"
                WeatherCondition.RAIN -> "Lluvia"
                WeatherCondition.THUNDERSTORM -> "Tormenta eléctrica"
                WeatherCondition.SNOW -> "Nieve"
                WeatherCondition.FOG -> "Niebla"
            }
            AppLanguage.FRENCH -> when (condition) {
                WeatherCondition.CLEAR_DAY -> "Ensoleillé"
                WeatherCondition.CLEAR_NIGHT -> "Dégagé"
                WeatherCondition.PARTLY_CLOUDY_DAY -> "Partiellement nuageux"
                WeatherCondition.PARTLY_CLOUDY_NIGHT -> "Partiellement nuageux"
                WeatherCondition.OVERCAST -> "Couvert"
                WeatherCondition.LIGHT_RAIN -> "Pluie légère"
                WeatherCondition.RAIN -> "Pluie"
                WeatherCondition.THUNDERSTORM -> "Orage"
                WeatherCondition.SNOW -> "Neige"
                WeatherCondition.FOG -> "Brouillard"
            }
            AppLanguage.GERMAN -> when (condition) {
                WeatherCondition.CLEAR_DAY -> "Sonnig"
                WeatherCondition.CLEAR_NIGHT -> "Klar"
                WeatherCondition.PARTLY_CLOUDY_DAY -> "Teilweise bewölkt"
                WeatherCondition.PARTLY_CLOUDY_NIGHT -> "Teilweise bewölkt"
                WeatherCondition.OVERCAST -> "Bewölkt"
                WeatherCondition.LIGHT_RAIN -> "Leichter Regen"
                WeatherCondition.RAIN -> "Regen"
                WeatherCondition.THUNDERSTORM -> "Gewitter"
                WeatherCondition.SNOW -> "Schnee"
                WeatherCondition.FOG -> "Nebelig"
            }
            AppLanguage.ITALIAN -> when (condition) {
                WeatherCondition.CLEAR_DAY -> "Soleggiato"
                WeatherCondition.CLEAR_NIGHT -> "Sereno"
                WeatherCondition.PARTLY_CLOUDY_DAY -> "Parzialmente nuvoloso"
                WeatherCondition.PARTLY_CLOUDY_NIGHT -> "Parzialmente nuvoloso"
                WeatherCondition.OVERCAST -> "Nuvoloso"
                WeatherCondition.LIGHT_RAIN -> "Pioggia leggera"
                WeatherCondition.RAIN -> "Pioggia"
                WeatherCondition.THUNDERSTORM -> "Temporale"
                WeatherCondition.SNOW -> "Neve"
                WeatherCondition.FOG -> "Nebbia"
            }
            AppLanguage.HINDI -> when (condition) {
                WeatherCondition.CLEAR_DAY -> "धूप खिली"
                WeatherCondition.CLEAR_NIGHT -> "साफ़ रात"
                WeatherCondition.PARTLY_CLOUDY_DAY -> "आंशिक बादल"
                WeatherCondition.PARTLY_CLOUDY_NIGHT -> "हल्के बादल"
                WeatherCondition.OVERCAST -> "घने बादल"
                WeatherCondition.LIGHT_RAIN -> "हल्की बारिश"
                WeatherCondition.RAIN -> "बारिश"
                WeatherCondition.THUNDERSTORM -> "आंधी-तूफान"
                WeatherCondition.SNOW -> "बर्फबारी"
                WeatherCondition.FOG -> "कोहरा"
            }
            AppLanguage.JAPANESE -> when (condition) {
                WeatherCondition.CLEAR_DAY -> "快晴"
                WeatherCondition.CLEAR_NIGHT -> "晴れ"
                WeatherCondition.PARTLY_CLOUDY_DAY -> "晴れ時々曇り"
                WeatherCondition.PARTLY_CLOUDY_NIGHT -> "晴れ時々曇り"
                WeatherCondition.OVERCAST -> "曇り"
                WeatherCondition.LIGHT_RAIN -> "小雨"
                WeatherCondition.RAIN -> "雨"
                WeatherCondition.THUNDERSTORM -> "雷雨"
                WeatherCondition.SNOW -> "雪"
                WeatherCondition.FOG -> "霧"
            }
            AppLanguage.KOREAN -> when (condition) {
                WeatherCondition.CLEAR_DAY -> "맑음"
                WeatherCondition.CLEAR_NIGHT -> "맑은 밤"
                WeatherCondition.PARTLY_CLOUDY_DAY -> "구름 조금"
                WeatherCondition.PARTLY_CLOUDY_NIGHT -> "구름 조금"
                WeatherCondition.OVERCAST -> "흐림"
                WeatherCondition.LIGHT_RAIN -> "약한 비"
                WeatherCondition.RAIN -> "비"
                WeatherCondition.THUNDERSTORM -> "천둥번개"
                WeatherCondition.SNOW -> "눈"
                WeatherCondition.FOG -> "안개"
            }
            AppLanguage.PORTUGUESE -> when (condition) {
                WeatherCondition.CLEAR_DAY -> "Ensolarado"
                WeatherCondition.CLEAR_NIGHT -> "Limpo"
                WeatherCondition.PARTLY_CLOUDY_DAY -> "Parcialmente nublado"
                WeatherCondition.PARTLY_CLOUDY_NIGHT -> "Parcialmente nublado"
                WeatherCondition.OVERCAST -> "Nublado"
                WeatherCondition.LIGHT_RAIN -> "Chuva fraca"
                WeatherCondition.RAIN -> "Chuva"
                WeatherCondition.THUNDERSTORM -> "Tempestade"
                WeatherCondition.SNOW -> "Neve"
                WeatherCondition.FOG -> "Nevoeiro"
            }
            AppLanguage.POLISH -> when (condition) {
                WeatherCondition.CLEAR_DAY -> "Słonecznie"
                WeatherCondition.CLEAR_NIGHT -> "Bezchmurnie"
                WeatherCondition.PARTLY_CLOUDY_DAY -> "Częściowo słonecznie"
                WeatherCondition.PARTLY_CLOUDY_NIGHT -> "Częściowo pochmurno"
                WeatherCondition.OVERCAST -> "Pochmurno"
                WeatherCondition.LIGHT_RAIN -> "Lekki deszcz"
                WeatherCondition.RAIN -> "Deszcz"
                WeatherCondition.THUNDERSTORM -> "Burza"
                WeatherCondition.SNOW -> "Śnieg"
                WeatherCondition.FOG -> "Mgła"
            }
            AppLanguage.ENGLISH -> condition.title
        }
    }

    fun getFeelsLikeLabel(language: AppLanguage): String {
        return when (language) {
            AppLanguage.SPANISH -> "Sensación"
            AppLanguage.FRENCH -> "Ressenti"
            AppLanguage.GERMAN -> "Gefühlt"
            AppLanguage.ITALIAN -> "Percepita"
            AppLanguage.HINDI -> "महसूस"
            AppLanguage.JAPANESE -> "体感"
            AppLanguage.KOREAN -> "체감"
            AppLanguage.PORTUGUESE -> "Sensação"
            AppLanguage.POLISH -> "Odczuwalna"
            AppLanguage.ENGLISH -> "Feels like"
        }
    }

    fun getHomeLabel(language: AppLanguage): String {
        return when (language) {
            AppLanguage.SPANISH -> "Inicio"
            AppLanguage.FRENCH -> "Accueil"
            AppLanguage.GERMAN -> "Startseite"
            AppLanguage.ITALIAN -> "Home"
            AppLanguage.HINDI -> "होम"
            AppLanguage.JAPANESE -> "ホーム"
            AppLanguage.KOREAN -> "홈"
            AppLanguage.PORTUGUESE -> "Início"
            AppLanguage.POLISH -> "Główna"
            AppLanguage.ENGLISH -> "Home"
        }
    }

    fun getHourlyForecastTitle(language: AppLanguage): String {
        return when (language) {
            AppLanguage.SPANISH -> "Pronóstico por horas"
            AppLanguage.FRENCH -> "Prévisions horaires"
            AppLanguage.GERMAN -> "Stündliche Vorhersage"
            AppLanguage.ITALIAN -> "Previsioni orarie"
            AppLanguage.HINDI -> "प्रति घंटा पूर्वानुमान"
            AppLanguage.JAPANESE -> "1時間ごとの天気予報"
            AppLanguage.KOREAN -> "시간별 날씨 예보"
            AppLanguage.PORTUGUESE -> "Previsão horária"
            AppLanguage.POLISH -> "Prognoza godzinowa"
            AppLanguage.ENGLISH -> "Hourly Forecast"
        }
    }

    fun getPrecipitationTitle(language: AppLanguage): String {
        return when (language) {
            AppLanguage.SPANISH -> "Precipitación"
            AppLanguage.FRENCH -> "Précipitations"
            AppLanguage.GERMAN -> "Niederschlag"
            AppLanguage.ITALIAN -> "Precipitazioni"
            AppLanguage.HINDI -> "वर्षा"
            AppLanguage.JAPANESE -> "降水量"
            AppLanguage.KOREAN -> "강수량"
            AppLanguage.PORTUGUESE -> "Precipitação"
            AppLanguage.POLISH -> "Opady"
            AppLanguage.ENGLISH -> "Precipitation"
        }
    }

    fun getAccuWeatherMapTitle(language: AppLanguage): String {
        return when (language) {
            AppLanguage.SPANISH -> "Mapa meteorológico AccuWeather"
            AppLanguage.FRENCH -> "Carte météo AccuWeather"
            AppLanguage.GERMAN -> "AccuWeather Wetterkarte"
            AppLanguage.ITALIAN -> "Mappa meteo AccuWeather"
            AppLanguage.HINDI -> "AccuWeather मौसम मानचित्र"
            AppLanguage.JAPANESE -> "AccuWeather 気象レーダーマップ"
            AppLanguage.KOREAN -> "AccuWeather 기상 지도"
            AppLanguage.PORTUGUESE -> "Mapa meteorológico AccuWeather"
            AppLanguage.POLISH -> "Mapa pogodowa AccuWeather"
            AppLanguage.ENGLISH -> "AccuWeather Weather Map"
        }
    }

    fun getBentoTitle(key: String, language: AppLanguage): String {
        return when (key) {
            "wind" -> when (language) {
                AppLanguage.SPANISH -> "Viento"
                AppLanguage.FRENCH -> "Vent"
                AppLanguage.GERMAN -> "Wind"
                AppLanguage.ITALIAN -> "Vento"
                AppLanguage.HINDI -> "हवा"
                AppLanguage.JAPANESE -> "風速"
                AppLanguage.KOREAN -> "바람"
                AppLanguage.PORTUGUESE -> "Vento"
                AppLanguage.POLISH -> "Wiatr"
                AppLanguage.ENGLISH -> "Wind"
            }
            "humidity" -> when (language) {
                AppLanguage.SPANISH -> "Humedad"
                AppLanguage.FRENCH -> "Humidité"
                AppLanguage.GERMAN -> "Feuchtigkeit"
                AppLanguage.ITALIAN -> "Umidità"
                AppLanguage.HINDI -> "नमी"
                AppLanguage.JAPANESE -> "湿度"
                AppLanguage.KOREAN -> "습도"
                AppLanguage.PORTUGUESE -> "Umidade"
                AppLanguage.POLISH -> "Wilgotność"
                AppLanguage.ENGLISH -> "Humidity"
            }
            "uv" -> when (language) {
                AppLanguage.SPANISH -> "Índice UV"
                AppLanguage.FRENCH -> "Indice UV"
                AppLanguage.GERMAN -> "UV-Index"
                AppLanguage.ITALIAN -> "Indice UV"
                AppLanguage.HINDI -> "यूवी सूचकांक"
                AppLanguage.JAPANESE -> "UVインデックス"
                AppLanguage.KOREAN -> "자외선 지수"
                AppLanguage.PORTUGUESE -> "Índice UV"
                AppLanguage.POLISH -> "Indeks UV"
                AppLanguage.ENGLISH -> "UV Index"
            }
            "pressure" -> when (language) {
                AppLanguage.SPANISH -> "Presión"
                AppLanguage.FRENCH -> "Pression"
                AppLanguage.GERMAN -> "Luftdruck"
                AppLanguage.ITALIAN -> "Pressione"
                AppLanguage.HINDI -> "दबाव"
                AppLanguage.JAPANESE -> "気圧"
                AppLanguage.KOREAN -> "기압"
                AppLanguage.PORTUGUESE -> "Pressão"
                AppLanguage.POLISH -> "Ciśnienie"
                AppLanguage.ENGLISH -> "Pressure"
            }
            "visibility" -> when (language) {
                AppLanguage.SPANISH -> "Visibilidad"
                AppLanguage.FRENCH -> "Visibilité"
                AppLanguage.GERMAN -> "Sichtweite"
                AppLanguage.ITALIAN -> "Visibilità"
                AppLanguage.HINDI -> "दृश्यता"
                AppLanguage.JAPANESE -> "視程"
                AppLanguage.KOREAN -> "가시거리"
                AppLanguage.PORTUGUESE -> "Visibilidade"
                AppLanguage.POLISH -> "Widoczność"
                AppLanguage.ENGLISH -> "Visibility"
            }
            "suntimes" -> when (language) {
                AppLanguage.SPANISH -> "Amanecer y Puesta de sol"
                AppLanguage.FRENCH -> "Lever et Coucher du soleil"
                AppLanguage.GERMAN -> "Sonnenauf- und -untergang"
                AppLanguage.ITALIAN -> "Alba e Tramonto"
                AppLanguage.HINDI -> "सूर्योदय और सूर्यास्त"
                AppLanguage.JAPANESE -> "日の出と日没"
                AppLanguage.KOREAN -> "일출 및 일몰"
                AppLanguage.PORTUGUESE -> "Nascer e Pôr do sol"
                AppLanguage.POLISH -> "Wschód i Zachód słońca"
                AppLanguage.ENGLISH -> "Sunrise & Sunset"
            }
            else -> key
        }
    }

    fun formatLocalizedDayTime(date: Date, language: AppLanguage): String {
        val locale = Locale(language.code)
        val sdf = SimpleDateFormat("EEE, h:mm a", locale)
        return sdf.format(date)
    }

    fun getHeadlineSummary(
        condition: WeatherCondition,
        tempMax: Double,
        tempMin: Double,
        language: AppLanguage,
        tempUnit: com.example.data.model.TemperatureUnit
    ): String {
        val high = tempUnit.formatValue(tempMax)
        val low = tempUnit.formatValue(tempMin)
        return when (language) {
            AppLanguage.SPANISH -> when (condition) {
                WeatherCondition.CLEAR_DAY, WeatherCondition.CLEAR_NIGHT -> "Cielo despejado hoy. Máx $high, Mín $low."
                WeatherCondition.PARTLY_CLOUDY_DAY, WeatherCondition.PARTLY_CLOUDY_NIGHT -> "Parcialmente nublado. Máx $high, Mín $low."
                WeatherCondition.OVERCAST -> "Cielo cubierto hoy. Máx $high, Mín $low."
                WeatherCondition.LIGHT_RAIN, WeatherCondition.RAIN -> "Chubascos intermitentes. Máx $high, Mín $low."
                WeatherCondition.THUNDERSTORM -> "Tormentas con posibles chubascos. Máx $high, Mín $low."
                WeatherCondition.SNOW -> "Nevadas y frío. Máx $high, Mín $low."
                WeatherCondition.FOG -> "Niebla y visibilidad reducida. Máx $high, Mín $low."
            }
            AppLanguage.FRENCH -> when (condition) {
                WeatherCondition.CLEAR_DAY, WeatherCondition.CLEAR_NIGHT -> "Ciel dégagé aujourd'hui. Max $high, Min $low."
                WeatherCondition.PARTLY_CLOUDY_DAY, WeatherCondition.PARTLY_CLOUDY_NIGHT -> "Partiellement nuageux. Max $high, Min $low."
                WeatherCondition.OVERCAST -> "Ciel couvert aujourd'hui. Max $high, Min $low."
                WeatherCondition.LIGHT_RAIN, WeatherCondition.RAIN -> "Averses prévues. Max $high, Min $low."
                WeatherCondition.THUNDERSTORM -> "Risque d'orages. Max $high, Min $low."
                WeatherCondition.SNOW -> "Chutes de neige. Max $high, Min $low."
                WeatherCondition.FOG -> "Brouillard matinal. Max $high, Min $low."
            }
            AppLanguage.GERMAN -> when (condition) {
                WeatherCondition.CLEAR_DAY, WeatherCondition.CLEAR_NIGHT -> "Klarer Himmel heute. Höchstwert $high, Tiefstwert $low."
                WeatherCondition.PARTLY_CLOUDY_DAY, WeatherCondition.PARTLY_CLOUDY_NIGHT -> "Teilweise bewölkt. Höchstwert $high, Tiefstwert $low."
                WeatherCondition.OVERCAST -> "Bedeckter Himmel. Höchstwert $high, Tiefstwert $low."
                WeatherCondition.LIGHT_RAIN, WeatherCondition.RAIN -> "Regenschauer erwartet. Höchstwert $high, Tiefstwert $low."
                WeatherCondition.THUNDERSTORM -> "Gewittergefahr. Höchstwert $high, Tiefstwert $low."
                WeatherCondition.SNOW -> "Schneefall und kalt. Höchstwert $high, Tiefstwert $low."
                WeatherCondition.FOG -> "Nebelig mit eingeschränkter Sicht. Höchstwert $high, Tiefstwert $low."
            }
            AppLanguage.ITALIAN -> when (condition) {
                WeatherCondition.CLEAR_DAY, WeatherCondition.CLEAR_NIGHT -> "Cielo sereno oggi. Max $high, Min $low."
                WeatherCondition.PARTLY_CLOUDY_DAY, WeatherCondition.PARTLY_CLOUDY_NIGHT -> "Parzialmente nuvoloso. Max $high, Min $low."
                WeatherCondition.OVERCAST -> "Cielo coperto. Max $high, Min $low."
                WeatherCondition.LIGHT_RAIN, WeatherCondition.RAIN -> "Rovesci sparsi. Max $high, Min $low."
                WeatherCondition.THUNDERSTORM -> "Temporali in arrivo. Max $high, Min $low."
                WeatherCondition.SNOW -> "Nevicate diffuse. Max $high, Min $low."
                WeatherCondition.FOG -> "Nebbia con visibilità ridotta. Max $high, Min $low."
            }
            AppLanguage.HINDI -> when (condition) {
                WeatherCondition.CLEAR_DAY, WeatherCondition.CLEAR_NIGHT -> "आज साफ़ धूप खिली रहेगी। अधिकतम $high, न्यूनतम $low।"
                WeatherCondition.PARTLY_CLOUDY_DAY, WeatherCondition.PARTLY_CLOUDY_NIGHT -> "आंशिक रूप से बादल छाए रहेंगे। अधिकतम $high, न्यूनतम $low।"
                WeatherCondition.OVERCAST -> "घने बादल छाए रहेंगे। अधिकतम $high, न्यूनतम $low।"
                WeatherCondition.LIGHT_RAIN, WeatherCondition.RAIN -> "हल्की बारिश के आसार हैं। अधिकतम $high, न्यूनतम $low।"
                WeatherCondition.THUNDERSTORM -> "आंधी-तूफान की संभावना। अधिकतम $high, न्यूनतम $low।"
                WeatherCondition.SNOW -> "बर्फबारी और ठंड। अधिकतम $high, न्यूनतम $low।"
                WeatherCondition.FOG -> "घना कोहरा और कम दृश्यता। अधिकतम $high, न्यूनतम $low।"
            }
            AppLanguage.JAPANESE -> when (condition) {
                WeatherCondition.CLEAR_DAY, WeatherCondition.CLEAR_NIGHT -> "一日を通して快晴。最高 $high、最低 $low。"
                WeatherCondition.PARTLY_CLOUDY_DAY, WeatherCondition.PARTLY_CLOUDY_NIGHT -> "晴れ時々曇りの天気。最高 $high、最低 $low。"
                WeatherCondition.OVERCAST -> "雲が広がりやすい一日。最高 $high、最低 $low。"
                WeatherCondition.LIGHT_RAIN, WeatherCondition.RAIN -> "所により雨が降る予報。最高 $high、最低 $low。"
                WeatherCondition.THUNDERSTORM -> "雷雨にご注意ください。最高 $high、最低 $low。"
                WeatherCondition.SNOW -> "雪が降る見込み。最高 $high、最低 $low。"
                WeatherCondition.FOG -> "霧が発生しやすい見込み。最高 $high、最低 $low。"
            }
            AppLanguage.KOREAN -> when (condition) {
                WeatherCondition.CLEAR_DAY, WeatherCondition.CLEAR_NIGHT -> "하루 종일 맑은 날씨. 최고 $high, 최저 $low."
                WeatherCondition.PARTLY_CLOUDY_DAY, WeatherCondition.PARTLY_CLOUDY_NIGHT -> "구름 조금 낀 날씨. 최고 $high, 최저 $low."
                WeatherCondition.OVERCAST -> "대체로 흐린 날씨. 최고 $high, 최저 $low."
                WeatherCondition.LIGHT_RAIN, WeatherCondition.RAIN -> "가끔 비가 내릴 예정. 최고 $high, 최저 $low."
                WeatherCondition.THUNDERSTORM -> "천둥번개를 동반한 비. 최고 $high, 최저 $low."
                WeatherCondition.SNOW -> "눈이 내리는 쌀쌀한 날씨. 최고 $high, 최저 $low."
                WeatherCondition.FOG -> "짙은 안개 주의. 최고 $high, 최저 $low."
            }
            AppLanguage.PORTUGUESE -> when (condition) {
                WeatherCondition.CLEAR_DAY, WeatherCondition.CLEAR_NIGHT -> "Céu limpo hoje. Máx $high, Mín $low."
                WeatherCondition.PARTLY_CLOUDY_DAY, WeatherCondition.PARTLY_CLOUDY_NIGHT -> "Parcialmente nublado. Máx $high, Mín $low."
                WeatherCondition.OVERCAST -> "Céu encoberto hoje. Máx $high, Mín $low."
                WeatherCondition.LIGHT_RAIN, WeatherCondition.RAIN -> "Pancadas de chuva ocasionais. Máx $high, Mín $low."
                WeatherCondition.THUNDERSTORM -> "Tempestades previstas. Máx $high, Mín $low."
                WeatherCondition.SNOW -> "Neve e tempo frio. Máx $high, Mín $low."
                WeatherCondition.FOG -> "Nevoeiro e baixa visibilidade. Máx $high, Mín $low."
            }
            AppLanguage.POLISH -> when (condition) {
                WeatherCondition.CLEAR_DAY, WeatherCondition.CLEAR_NIGHT -> "Bezchmurne niebo przez cały dzień. Maks. $high, Min. $low."
                WeatherCondition.PARTLY_CLOUDY_DAY, WeatherCondition.PARTLY_CLOUDY_NIGHT -> "Częściowo słonecznie. Maks. $high, Min. $low."
                WeatherCondition.OVERCAST -> "Pochmurno przez cały dzień. Maks. $high, Min. $low."
                WeatherCondition.LIGHT_RAIN, WeatherCondition.RAIN -> "Przelotne opady deszczu. Maks. $high, Min. $low."
                WeatherCondition.THUNDERSTORM -> "Możliwe burze z deszczem. Maks. $high, Min. $low."
                WeatherCondition.SNOW -> "Opady śniegu i chłód. Maks. $high, Min. $low."
                WeatherCondition.FOG -> "Mgła i ograniczona widoczność. Maks. $high, Min. $low."
            }
            AppLanguage.ENGLISH -> when (condition) {
                WeatherCondition.CLEAR_DAY, WeatherCondition.CLEAR_NIGHT -> "Sunny and clear throughout the day. High $high, Low $low."
                WeatherCondition.PARTLY_CLOUDY_DAY, WeatherCondition.PARTLY_CLOUDY_NIGHT -> "Partly cloudy with pleasant conditions. High $high, Low $low."
                WeatherCondition.OVERCAST -> "Overcast skies throughout the day. High $high, Low $low."
                WeatherCondition.LIGHT_RAIN, WeatherCondition.RAIN -> "Showers early. High $high, Low $low."
                WeatherCondition.THUNDERSTORM -> "Scattered thunderstorms expected. High $high, Low $low."
                WeatherCondition.SNOW -> "Snow showers with freezing temps. High $high, Low $low."
                WeatherCondition.FOG -> "Dense fog and reduced visibility. High $high, Low $low."
            }
        }
    }

    fun getSunsetLabel(language: AppLanguage): String = when (language) {
        AppLanguage.SPANISH -> "Puesta de sol"
        AppLanguage.FRENCH -> "Coucher"
        AppLanguage.GERMAN -> "Sonnenuntergang"
        AppLanguage.ITALIAN -> "Tramonto"
        AppLanguage.HINDI -> "सूर्यास्त"
        AppLanguage.JAPANESE -> "日没"
        AppLanguage.KOREAN -> "일몰"
        AppLanguage.PORTUGUESE -> "Pôr do sol"
        AppLanguage.POLISH -> "Zachód słońca"
        AppLanguage.ENGLISH -> "Sunset"
    }

    fun getSunriseLabel(language: AppLanguage): String = when (language) {
        AppLanguage.SPANISH -> "Amanecer"
        AppLanguage.FRENCH -> "Lever"
        AppLanguage.GERMAN -> "Sonnenaufgang"
        AppLanguage.ITALIAN -> "Alba"
        AppLanguage.HINDI -> "सूर्योदय"
        AppLanguage.JAPANESE -> "日の出"
        AppLanguage.KOREAN -> "일출"
        AppLanguage.PORTUGUESE -> "Nascer do sol"
        AppLanguage.POLISH -> "Wschód słońca"
        AppLanguage.ENGLISH -> "Sunrise"
    }

    fun getAiOverviewTitle(language: AppLanguage): String = when (language) {
        AppLanguage.SPANISH -> "Resumen Meteorológico Gemini AI"
        AppLanguage.FRENCH -> "Aperçu Météo Gemini AI"
        AppLanguage.GERMAN -> "Gemini KI-Wetterübersicht"
        AppLanguage.ITALIAN -> "Panoramica Meteo Gemini AI"
        AppLanguage.HINDI -> "Gemini AI मौसम अवलोकन"
        AppLanguage.JAPANESE -> "Gemini AI 気象サマリー"
        AppLanguage.KOREAN -> "Gemini AI 날씨 요약"
        AppLanguage.PORTUGUESE -> "Visão Geral Meteorológica Gemini AI"
        AppLanguage.POLISH -> "Przegląd Pogodowy Gemini AI"
        AppLanguage.ENGLISH -> "Gemini AI Weather Overview"
    }

    fun getAiOverviewSubtitle(language: AppLanguage): String = when (language) {
        AppLanguage.SPANISH -> "Inteligencia atmosférica con Gemini 3.8"
        AppLanguage.FRENCH -> "Intelligence atmosphérique propulsée par Gemini 3.8"
        AppLanguage.GERMAN -> "Atmosphärische Intelligenz mit Gemini 3.8"
        AppLanguage.ITALIAN -> "Intelligenza atmosferica con Gemini 3.8"
        AppLanguage.HINDI -> "Gemini 3.8 द्वारा संचालित वायुमंडलीय बुद्धिमत्ता"
        AppLanguage.JAPANESE -> "Gemini 3.8 による大気インテリジェンス"
        AppLanguage.KOREAN -> "Gemini 3.8 기반 대기 기상 분석"
        AppLanguage.PORTUGUESE -> "Inteligência atmosférica por Gemini 3.8"
        AppLanguage.POLISH -> "Analiza atmosferyczna napędzana przez Gemini 3.8"
        AppLanguage.ENGLISH -> "Atmospheric intelligence powered by Gemini 3.8"
    }

    fun getAskAiTitle(language: AppLanguage): String = when (language) {
        AppLanguage.SPANISH -> "Pregunta a Gemini sobre el clima"
        AppLanguage.FRENCH -> "Demandez à Gemini sur la météo"
        AppLanguage.GERMAN -> "Frage Gemini nach dem heutigen Wetter"
        AppLanguage.ITALIAN -> "Chiedi a Gemini sul meteo di oggi"
        AppLanguage.HINDI -> "आज के मौसम के बारे में Gemini से पूछें"
        AppLanguage.JAPANESE -> "今日の天気について Gemini に質問"
        AppLanguage.KOREAN -> "오늘 날씨에 대해 Gemini에게 물어보세요"
        AppLanguage.PORTUGUESE -> "Pergunte ao Gemini sobre o clima de hoje"
        AppLanguage.POLISH -> "Zapytaj Gemini o dzisiejszą pogodę"
        AppLanguage.ENGLISH -> "Ask Gemini about today's weather"
    }

    fun getAskAiPlaceholder(language: AppLanguage): String = when (language) {
        AppLanguage.SPANISH -> "Preguntar en español…"
        AppLanguage.FRENCH -> "Demander en français…"
        AppLanguage.GERMAN -> "Auf Deutsch fragen…"
        AppLanguage.ITALIAN -> "Chiedi in italiano…"
        AppLanguage.HINDI -> "हिन्दी में पूछें…"
        AppLanguage.JAPANESE -> "日本語で質問する…"
        AppLanguage.KOREAN -> "한국어로 질문하기…"
        AppLanguage.PORTUGUESE -> "Perguntar em português…"
        AppLanguage.POLISH -> "Zapytaj po polsku…"
        AppLanguage.ENGLISH -> "Ask in English…"
    }

    fun getAttireLabel(language: AppLanguage): String = when (language) {
        AppLanguage.SPANISH -> "Vestimenta"
        AppLanguage.FRENCH -> "Tenue"
        AppLanguage.GERMAN -> "Kleidung"
        AppLanguage.ITALIAN -> "Abbigliamento"
        AppLanguage.HINDI -> "पोशाक"
        AppLanguage.JAPANESE -> "服装"
        AppLanguage.KOREAN -> "복장"
        AppLanguage.PORTUGUESE -> "Vestuário"
        AppLanguage.POLISH -> "Strój"
        AppLanguage.ENGLISH -> "Attire"
    }

    fun getOutdoorsLabel(language: AppLanguage): String = when (language) {
        AppLanguage.SPANISH -> "Aire libre"
        AppLanguage.FRENCH -> "Extérieur"
        AppLanguage.GERMAN -> "Draußen"
        AppLanguage.ITALIAN -> "All'aperto"
        AppLanguage.HINDI -> "बाहरी गतिविधियाँ"
        AppLanguage.JAPANESE -> "屋外活動"
        AppLanguage.KOREAN -> "야외 활동"
        AppLanguage.PORTUGUESE -> "Ao ar livre"
        AppLanguage.POLISH -> "Na zewnątrz"
        AppLanguage.ENGLISH -> "Outdoors"
    }

    fun getGeneratingAiOverviewText(language: AppLanguage): String = when (language) {
        AppLanguage.SPANISH -> "Gemini está analizando la atmósfera en español…"
        AppLanguage.FRENCH -> "Gemini analyse l'atmosphère en français…"
        AppLanguage.GERMAN -> "Gemini analysiert die Wetterlage auf Deutsch…"
        AppLanguage.ITALIAN -> "Gemini sta analizzando l'atmosfera in italiano…"
        AppLanguage.HINDI -> "Gemini हिन्दी में वायुमंडलीय विश्लेषण कर रहा है…"
        AppLanguage.JAPANESE -> "Gemini が日本語で大気状態を分析中…"
        AppLanguage.KOREAN -> "Gemini가 한국어로 대기 상황을 분석 중입니다…"
        AppLanguage.PORTUGUESE -> "Gemini está analisando a atmosfera em português…"
        AppLanguage.POLISH -> "Gemini analizuje atmosferę po polsku…"
        AppLanguage.ENGLISH -> "Gemini is analyzing atmosphere in English…"
    }

    fun getAccuWeatherLiveText(language: AppLanguage): String = when (language) {
        AppLanguage.SPANISH -> "Mapa en Vivo"
        AppLanguage.FRENCH -> "Carte en Direct"
        AppLanguage.GERMAN -> "Live-Karte"
        AppLanguage.ITALIAN -> "Mappa in Diretta"
        AppLanguage.HINDI -> "लाइव मैप"
        AppLanguage.JAPANESE -> "ライブマップ"
        AppLanguage.KOREAN -> "실시간 지도"
        AppLanguage.PORTUGUESE -> "Mapa ao Vivo"
        AppLanguage.POLISH -> "Mapa na Żywo"
        AppLanguage.ENGLISH -> "Live Map"
    }

    fun getSearchCitiesHint(language: AppLanguage): String = when (language) {
        AppLanguage.SPANISH -> "Buscar ciudades…"
        AppLanguage.FRENCH -> "Rechercher des villes…"
        AppLanguage.GERMAN -> "Städte suchen…"
        AppLanguage.ITALIAN -> "Cerca città…"
        AppLanguage.HINDI -> "शहर खोजें…"
        AppLanguage.JAPANESE -> "都市を検索…"
        AppLanguage.KOREAN -> "도시 검색…"
        AppLanguage.PORTUGUESE -> "Pesquisar cidades…"
        AppLanguage.POLISH -> "Szukaj miast…"
        AppLanguage.ENGLISH -> "Search cities…"
    }

    fun getSearchTitle(language: AppLanguage): String = when (language) {
        AppLanguage.SPANISH -> "Buscar Ciudades y Lugares"
        AppLanguage.FRENCH -> "Rechercher Villes & Lieux"
        AppLanguage.GERMAN -> "Städte & Orte suchen"
        AppLanguage.ITALIAN -> "Cerca Città e Luoghi"
        AppLanguage.HINDI -> "शहर और स्थान खोजें"
        AppLanguage.JAPANESE -> "都市と地域を検索"
        AppLanguage.KOREAN -> "도시 및 장소 검색"
        AppLanguage.PORTUGUESE -> "Pesquisar Cidades e Lugares"
        AppLanguage.POLISH -> "Szukaj miast i miejsc"
        AppLanguage.ENGLISH -> "Search Cities & Places"
    }

    fun getSearchPlaceholder(language: AppLanguage): String = when (language) {
        AppLanguage.SPANISH -> "Ingresa ciudad, estado o país…"
        AppLanguage.FRENCH -> "Entrez ville, région ou pays…"
        AppLanguage.GERMAN -> "Stadt, Bundesland oder Land eingeben…"
        AppLanguage.ITALIAN -> "Inserisci città, regione o paese…"
        AppLanguage.HINDI -> "शहर, राज्य या देश दर्ज करें…"
        AppLanguage.JAPANESE -> "都市、地域、国を入力…"
        AppLanguage.KOREAN -> "도시, 주 또는 국가 입력…"
        AppLanguage.PORTUGUESE -> "Digite cidade, estado ou país…"
        AppLanguage.POLISH -> "Wpisz miasto, region lub kraj…"
        AppLanguage.ENGLISH -> "Enter city, state, or country…"
    }

    fun getSearchUsingCurrentLocation(language: AppLanguage): String = when (language) {
        AppLanguage.SPANISH -> "Usar ubicación actual"
        AppLanguage.FRENCH -> "Utiliser la position actuelle"
        AppLanguage.GERMAN -> "Aktuellen Standort verwenden"
        AppLanguage.ITALIAN -> "Usa posizione attuale"
        AppLanguage.HINDI -> "वर्तमान स्थान का उपयोग करें"
        AppLanguage.JAPANESE -> "現在地を使用"
        AppLanguage.KOREAN -> "현재 위치 사용"
        AppLanguage.PORTUGUESE -> "Usar localização atual"
        AppLanguage.POLISH -> "Użyj bieżącej lokalizacji"
        AppLanguage.ENGLISH -> "Use Current Location"
    }

    fun getSearchGpsDescription(language: AppLanguage): String = when (language) {
        AppLanguage.SPANISH -> "Obtén el clima instantáneo de donde te encuentras"
        AppLanguage.FRENCH -> "Météo instantanée pour votre position"
        AppLanguage.GERMAN -> "Sofortiges Wetter für deinen aktuellen Standort"
        AppLanguage.ITALIAN -> "Meteo istantaneo per la tua posizione attuale"
        AppLanguage.HINDI -> "अभी आप जहाँ हैं वहाँ का तुरंत मौसम प्राप्त करें"
        AppLanguage.JAPANESE -> "現在地の最新天気を即座に取得"
        AppLanguage.KOREAN -> "현재 위치의 실시간 날씨를 바로 확인하세요"
        AppLanguage.PORTUGUESE -> "Obtenha a previsão instantânea de onde você está"
        AppLanguage.POLISH -> "Sprawdź natychmiast pogodę dla swojej lokalizacji"
        AppLanguage.ENGLISH -> "Get instant weather for where you are now"
    }

    fun getFavoritePlaces(language: AppLanguage): String = when (language) {
        AppLanguage.SPANISH -> "Lugares Favoritos"
        AppLanguage.FRENCH -> "Lieux Favoris"
        AppLanguage.GERMAN -> "Favoriten"
        AppLanguage.ITALIAN -> "Luoghi Preferiti"
        AppLanguage.HINDI -> "पसंदीदा स्थान"
        AppLanguage.JAPANESE -> "お気に入りの場所"
        AppLanguage.KOREAN -> "즐겨찾는 장소"
        AppLanguage.PORTUGUESE -> "Locais Favoritos"
        AppLanguage.POLISH -> "Ulubione miejsca"
        AppLanguage.ENGLISH -> "Favorite Places"
    }

    fun getPopularCities(language: AppLanguage): String = when (language) {
        AppLanguage.SPANISH -> "Ciudades Populares del Mundo"
        AppLanguage.FRENCH -> "Villes Populaires du Monde"
        AppLanguage.GERMAN -> "Beliebte Weltstädte"
        AppLanguage.ITALIAN -> "Città Popolari nel Mondo"
        AppLanguage.HINDI -> "दुनिया भर के लोकप्रिय शहर"
        AppLanguage.JAPANESE -> "世界の人気都市"
        AppLanguage.KOREAN -> "전 세계 인기 도시"
        AppLanguage.PORTUGUESE -> "Cidades Populares do Mundo"
        AppLanguage.POLISH -> "Popularne miasta na świecie"
        AppLanguage.ENGLISH -> "Popular Worldwide Cities"
    }

    fun getSelectLanguageTitle(language: AppLanguage): String = when (language) {
        AppLanguage.SPANISH -> "Seleccionar idioma"
        AppLanguage.FRENCH -> "Sélectionner la langue"
        AppLanguage.GERMAN -> "Sprache auswählen"
        AppLanguage.ITALIAN -> "Seleziona lingua"
        AppLanguage.HINDI -> "भाषा चुनें"
        AppLanguage.JAPANESE -> "言語を選択"
        AppLanguage.KOREAN -> "언어 선택"
        AppLanguage.PORTUGUESE -> "Selecionar idioma"
        AppLanguage.POLISH -> "Wybierz język"
        AppLanguage.ENGLISH -> "Select Language"
    }

    fun getRetryText(language: AppLanguage): String = when (language) {
        AppLanguage.SPANISH -> "Reintentar con Gemini"
        AppLanguage.FRENCH -> "Réessayer avec Gemini"
        AppLanguage.GERMAN -> "Mit Gemini wiederholen"
        AppLanguage.ITALIAN -> "Riprova con Gemini"
        AppLanguage.HINDI -> "Gemini से पुनः प्रयास करें"
        AppLanguage.JAPANESE -> "Gemini で再試行"
        AppLanguage.KOREAN -> "Gemini로 다시 시도"
        AppLanguage.PORTUGUESE -> "Tentar novamente com Gemini"
        AppLanguage.POLISH -> "Ponów z Gemini"
        AppLanguage.ENGLISH -> "Retry with Gemini"
    }
}
