package com.example.data.model

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val flag: String
) {
    ENGLISH("en", "English", "English", "🇺🇸"),
    POLISH("pl", "Polish", "Polski", "🇵🇱"),
    ITALIAN("it", "Italian", "Italiano", "🇮🇹"),
    HINDI("hi", "Hindi", "हिन्दी", "🇮🇳"),
    SPANISH("es", "Spanish", "Español", "🇪🇸"),
    FRENCH("fr", "French", "Français", "🇫🇷"),
    GERMAN("de", "German", "Deutsch", "🇩🇪"),
    JAPANESE("ja", "Japanese", "日本語", "🇯🇵"),
    PORTUGUESE("pt", "Portuguese", "Português", "🇵🇹"),
    KOREAN("ko", "Korean", "한국어", "🇰🇷");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}
