package com.verbatim.studio.model

data class AppSettings(
    val geminiApiKey: String = "",
    val geminiModel: String = "gemini-3.5-flash-lite",
    val isDarkTheme: Boolean = true,
    val historyMode: String = "enabled", // "enabled", "incognito", "disabled"
    val historyRetention: String = "never", // "never", "1month", "1week", "3days", "1day", "custom"
    val customRetentionDays: Float = 0f
)

object GeminiModels {
    val flashLite = listOf(
        "gemini-3.5-flash-lite" to "Gemini 3.5 Flash Lite",
        "gemini-3.1-flash-lite" to "Gemini 3.1 Flash Lite",
        "gemini-2.5-flash-lite" to "Gemini 2.5 Flash Lite"
    )

    val flash = listOf(
        "gemini-3.6-flash" to "Gemini 3.6 Flash",
        "gemini-3.5-flash" to "Gemini 3.5 Flash",
        "gemini-3-flash-preview" to "Gemini 3.0 Flash Preview",
        "gemini-2.5-flash" to "Gemini 2.5 Flash"
    )

    val pro = listOf(
        "gemini-3.1-pro-preview" to "Gemini 3.1 Pro Preview",
        "gemini-2.5-pro" to "Gemini 2.5 Pro"
    )

    val allModels = (flashLite + flash + pro).map { it.first }
}
