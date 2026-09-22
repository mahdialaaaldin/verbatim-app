package com.verbatim.studio.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.verbatim.studio.model.AppSettings
import com.verbatim.studio.model.HistoryEntry
import com.verbatim.studio.model.Preset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("verbatim_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private val _customPresets = MutableStateFlow(loadCustomPresets())
    val customPresets: StateFlow<List<Preset>> = _customPresets.asStateFlow()

    private val _history = MutableStateFlow(loadHistory())
    val history: StateFlow<List<HistoryEntry>> = _history.asStateFlow()

    fun loadSettings(): AppSettings {
        return AppSettings(
            geminiApiKey = prefs.getString("cs_gemini_key", "") ?: "",
            geminiModel = prefs.getString("cs_gemini_model", "gemini-3.5-flash-lite") ?: "gemini-3.5-flash-lite",
            isDarkTheme = prefs.getBoolean("cs_theme_dark", true),
            historyMode = prefs.getString("cs_history_mode", "enabled") ?: "enabled",
            historyRetention = prefs.getString("cs_history_retention", "never") ?: "never",
            customRetentionDays = prefs.getFloat("cs_custom_retention_days", 0f)
        )
    }

    fun saveSettings(newSettings: AppSettings) {
        prefs.edit()
            .putString("cs_gemini_key", newSettings.geminiApiKey)
            .putString("cs_gemini_model", newSettings.geminiModel)
            .putBoolean("cs_theme_dark", newSettings.isDarkTheme)
            .putString("cs_history_mode", newSettings.historyMode)
            .putString("cs_history_retention", newSettings.historyRetention)
            .putFloat("cs_custom_retention_days", newSettings.customRetentionDays)
            .apply()
        _settings.value = newSettings
        pruneHistory()
    }

    fun toggleTheme() {
        val current = _settings.value
        saveSettings(current.copy(isDarkTheme = !current.isDarkTheme))
    }

    fun toggleIncognito() {
        val current = _settings.value
        val newMode = if (current.historyMode == "incognito") "enabled" else "incognito"
        saveSettings(current.copy(historyMode = newMode))
    }

    // Custom Presets
    fun loadCustomPresets(): List<Preset> {
        val json = prefs.getString("cs_custom_presets", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Preset>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun saveCustomPresets(list: List<Preset>) {
        val json = gson.toJson(list)
        prefs.edit().putString("cs_custom_presets", json).apply()
        _customPresets.value = list
    }

    fun addCustomPreset(name: String, prompt: String) {
        val newPreset = Preset(
            id = "custom_${System.currentTimeMillis()}",
            name = name,
            prompt = prompt,
            isCustom = true
        )
        val current = _customPresets.value.toMutableList()
        current.add(newPreset)
        saveCustomPresets(current)
    }

    fun updateCustomPreset(id: String, name: String, prompt: String) {
        val current = _customPresets.value.map {
            if (it.id == id) it.copy(name = name, prompt = prompt) else it
        }
        saveCustomPresets(current)
    }

    fun deleteCustomPreset(id: String) {
        val current = _customPresets.value.filter { it.id != id }
        saveCustomPresets(current)
    }

    fun movePresetUp(id: String) {
        val list = _customPresets.value.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index > 0) {
            val item = list.removeAt(index)
            list.add(index - 1, item)
            saveCustomPresets(list)
        }
    }

    fun movePresetDown(id: String) {
        val list = _customPresets.value.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index != -1 && index < list.size - 1) {
            val item = list.removeAt(index)
            list.add(index + 1, item)
            saveCustomPresets(list)
        }
    }

    // History
    fun loadHistory(): List<HistoryEntry> {
        val json = prefs.getString("cs_history", null) ?: return emptyList()
        val list: List<HistoryEntry> = try {
            val type = object : TypeToken<List<HistoryEntry>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        return pruneList(list)
    }

    fun saveHistoryEntry(tone: String, input: String, output: String) {
        val mode = _settings.value.historyMode
        if (mode == "incognito" || mode == "disabled") return

        val now = java.util.Date()
        val timeFormat = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US)
        val dateFormat = java.text.SimpleDateFormat("MMM d", java.util.Locale.US)

        val entry = HistoryEntry(
            id = System.currentTimeMillis(),
            timestamp = timeFormat.format(now),
            date = dateFormat.format(now),
            tone = tone,
            input = input,
            output = output
        )

        val current = _history.value.toMutableList()
        current.add(0, entry)
        if (current.size > 25) current.removeAt(current.size - 1)

        val pruned = pruneList(current)
        val json = gson.toJson(pruned)
        prefs.edit().putString("cs_history", json).apply()
        _history.value = pruned
    }

    fun deleteHistoryEntry(id: Long) {
        val current = _history.value.filter { it.id != id }
        val json = gson.toJson(current)
        prefs.edit().putString("cs_history", json).apply()
        _history.value = current
    }

    fun clearAllHistory() {
        prefs.edit().remove("cs_history").apply()
        _history.value = emptyList()
    }

    private fun pruneHistory() {
        val current = _history.value
        val pruned = pruneList(current)
        if (pruned.size != current.size) {
            val json = gson.toJson(pruned)
            prefs.edit().putString("cs_history", json).apply()
            _history.value = pruned
        }
    }

    private fun pruneList(list: List<HistoryEntry>): List<HistoryEntry> {
        val retention = _settings.value.historyRetention
        val customDays = _settings.value.customRetentionDays
        if (retention == "never") return list

        val days = when (retention) {
            "1day" -> 1f
            "3days" -> 3f
            "1week" -> 7f
            "1month" -> 30f
            "custom" -> customDays
            else -> 0f
        }

        if (days <= 0f) return list
        val cutoff = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L).toLong()
        return list.filter { it.id >= cutoff }
    }

    // Export & Import
    fun exportSettingsJson(): String {
        val data = mapOf(
            "exportDate" to java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US).format(java.util.Date()),
            "theme" to if (_settings.value.isDarkTheme) "dark" else "light",
            "geminiModel" to _settings.value.geminiModel,
            "historyMode" to _settings.value.historyMode,
            "historyRetention" to _settings.value.historyRetention,
            "customRetentionDays" to _settings.value.customRetentionDays,
            "customPresets" to _customPresets.value
        )
        return gson.toJson(data)
    }

    fun importSettingsJson(json: String): Boolean {
        return try {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val map: Map<String, Any> = gson.fromJson(json, type)
            val isDark = map["theme"]?.toString() != "light"
            val model = map["geminiModel"]?.toString() ?: _settings.value.geminiModel
            val historyMode = map["historyMode"]?.toString() ?: "enabled"
            val retention = map["historyRetention"]?.toString() ?: "never"
            val customDays = (map["customRetentionDays"] as? Number)?.toFloat() ?: 0f

            saveSettings(
                _settings.value.copy(
                    isDarkTheme = isDark,
                    geminiModel = model,
                    historyMode = historyMode,
                    historyRetention = retention,
                    customRetentionDays = customDays
                )
            )

            val presetsJson = gson.toJson(map["customPresets"])
            if (!presetsJson.isNullOrEmpty() && presetsJson != "null") {
                val presetType = object : TypeToken<List<Map<String, Any>>>() {}.type
                val rawPresets: List<Map<String, Any>> = gson.fromJson(presetsJson, presetType) ?: emptyList()
                val parsed = rawPresets.mapNotNull { p ->
                    val name = p["name"]?.toString()
                    val prompt = p["prompt"]?.toString()
                    if (!name.isNullOrBlank() && !prompt.isNullOrBlank()) {
                        Preset(
                            id = p["id"]?.toString() ?: "custom_${System.currentTimeMillis()}",
                            name = name,
                            prompt = prompt,
                            isCustom = true
                        )
                    } else null
                }
                saveCustomPresets(parsed)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun resetToDefaults(clearHistory: Boolean) {
        prefs.edit()
            .remove("cs_gemini_key")
            .putString("cs_gemini_model", "gemini-3.5-flash-lite")
            .putBoolean("cs_theme_dark", true)
            .putString("cs_history_mode", "enabled")
            .putString("cs_history_retention", "never")
            .putFloat("cs_custom_retention_days", 0f)
            .remove("cs_custom_presets")
            .apply()

        if (clearHistory) {
            clearAllHistory()
        }

        _settings.value = loadSettings()
        _customPresets.value = emptyList()
    }
}
