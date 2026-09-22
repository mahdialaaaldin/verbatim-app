package com.verbatim.studio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.verbatim.studio.repository.GeminiRepository
import com.verbatim.studio.repository.SettingsRepository
import com.verbatim.studio.service.TTSManager
import com.verbatim.studio.theme.VerbatimTheme
import com.verbatim.studio.ui.VerbatimScreen

class MainActivity : ComponentActivity() {

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var geminiRepository: GeminiRepository
    private lateinit var ttsManager: TTSManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        settingsRepository = SettingsRepository(this)
        geminiRepository = GeminiRepository()
        ttsManager = TTSManager(this)

        setContent {
            val settings by settingsRepository.settings.collectAsState()

            VerbatimTheme(darkTheme = settings.isDarkTheme) {
                VerbatimScreen(
                    settingsRepository = settingsRepository,
                    geminiRepository = geminiRepository,
                    ttsManager = ttsManager
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}
