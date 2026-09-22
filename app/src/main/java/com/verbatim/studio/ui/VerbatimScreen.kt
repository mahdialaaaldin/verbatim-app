package com.verbatim.studio.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.verbatim.studio.model.AppSettings
import com.verbatim.studio.model.Preset
import com.verbatim.studio.model.PresetDefaults
import com.verbatim.studio.repository.GeminiRepository
import com.verbatim.studio.repository.SettingsRepository
import com.verbatim.studio.service.TTSManager
import com.verbatim.studio.theme.BackgroundDark
import com.verbatim.studio.theme.BackgroundLight
import com.verbatim.studio.ui.components.*
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun VerbatimScreen(
    settingsRepository: SettingsRepository,
    geminiRepository: GeminiRepository,
    ttsManager: TTSManager
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = remember { context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }

    val settings by settingsRepository.settings.collectAsState()
    val customPresets by settingsRepository.customPresets.collectAsState()
    val history by settingsRepository.history.collectAsState()
    val isSpeaking by ttsManager.isSpeaking.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var outputText by remember { mutableStateOf("") }
    var activePresetName by remember { mutableStateOf<String?>(null) }
    var showDiff by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    var showHistorySheet by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isTabletOrLandscape = configuration.screenWidthDp >= 768

    // File Picker for JSON Restore
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val json = stream.bufferedReader().readText()
                    val success = settingsRepository.importSettingsJson(json)
                    Toast.makeText(
                        context,
                        if (success) "Settings imported successfully!" else "Failed to parse settings JSON.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error importing file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Helper: Trigger AI
    fun executePreset(preset: Preset) {
        val trimmed = inputText.trim()
        if (trimmed.isEmpty()) {
            Toast.makeText(context, "Please write or paste some text first!", Toast.LENGTH_SHORT).show()
            return
        }

        if (settings.geminiApiKey.isBlank()) {
            Toast.makeText(context, "Please set your Gemini API Key in Settings first!", Toast.LENGTH_LONG).show()
            showSettingsDialog = true
            return
        }

        isLoading = true
        activePresetName = preset.name
        if (!isTabletOrLandscape) {
            // If user is on Input tab (1), switch to Both (0) so they can see both input and output
            if (selectedTab == 1) {
                selectedTab = 0
            }
        }

        scope.launch {
            val prompt = PresetDefaults.getPromptFor(preset, trimmed)
            val result = geminiRepository.callGemini(
                apiKey = settings.geminiApiKey,
                preferredModel = settings.geminiModel,
                prompt = prompt
            )

            isLoading = false
            result.onSuccess { responseText ->
                outputText = responseText
                settingsRepository.saveHistoryEntry(
                    tone = preset.name,
                    input = trimmed,
                    output = responseText
                )
            }.onFailure { error ->
                outputText = "Error: ${error.message}"
                Toast.makeText(context, "AI Error: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Helper: Native Share Sheet
    fun shareText(text: String) {
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share text via")
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Share failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Sample Text for 1-Tap Demo
    val sampleText = "Verbatim Studio is an intelligent writing companion. It automatically corrects grammatical mistakes, refines formal professional communications, summarizes dense documentation, and creates structured prompts for large language models."

    val screenBg = if (settings.isDarkTheme) BackgroundDark else BackgroundLight
    val hasValidOutput = outputText.isNotBlank() && outputText != "Result will appear here..."

    Scaffold(
        containerColor = screenBg,
        contentColor = if (settings.isDarkTheme) Color(0xFFF1F5F9) else Color(0xFF1E293B)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Header Bar
            HeaderBar(
                hasApiKey = settings.geminiApiKey.isNotBlank(),
                isDarkTheme = settings.isDarkTheme,
                isIncognito = settings.historyMode == "incognito",
                onToggleTheme = { settingsRepository.toggleTheme() },
                onToggleIncognito = { settingsRepository.toggleIncognito() },
                onOpenHistory = { showHistorySheet = true },
                onOpenSettings = { showSettingsDialog = true }
            )

            // 2. Action Presets Carousel
            PresetCarousel(
                builtInPresets = PresetDefaults.builtInPresets,
                customPresets = customPresets,
                isDarkTheme = settings.isDarkTheme,
                activePresetName = activePresetName,
                isLoading = isLoading,
                onSelectPreset = { executePreset(it) }
            )

            // 3. Mobile Sliding Tab Bar (Hidden on Tablet / Landscape)
            if (!isTabletOrLandscape) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    SlidingTabBar(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        isDarkTheme = settings.isDarkTheme,
                        hasOutput = hasValidOutput,
                        isLoading = isLoading
                    )
                }
            }

            // 4. Main Workspace
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (isTabletOrLandscape) {
                    // Side-by-side view on Tablets / Landscape
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InputPanel(
                            text = inputText,
                            onTextChanged = { inputText = it },
                            onPaste = {
                                val clip = clipboard.primaryClip
                                if (clip != null && clip.itemCount > 0) {
                                    val pasteText = clip.getItemAt(0).coerceToText(context).toString()
                                    inputText = pasteText
                                    Toast.makeText(context, "Pasted from clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onClear = {
                                inputText = ""
                            },
                            onSampleText = {
                                inputText = sampleText
                            },
                            isDarkTheme = settings.isDarkTheme,
                            modifier = Modifier.weight(1f)
                        )

                        OutputPanel(
                            rawInputText = inputText,
                            rawOutputText = outputText,
                            activePresetName = activePresetName,
                            isLoading = isLoading,
                            isSpeaking = isSpeaking,
                            showDiff = showDiff,
                            onToggleDiff = { showDiff = !showDiff },
                            onToggleSpeak = {
                                if (outputText.isNotBlank() && outputText != "Result will appear here...") {
                                    ttsManager.toggleSpeak(outputText)
                                }
                            },
                            onCopy = {
                                if (outputText.isNotBlank()) {
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Verbatim Enhanced", outputText))
                                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onUseAsInput = {
                                inputText = outputText
                                Toast.makeText(context, "Output transferred to input!", Toast.LENGTH_SHORT).show()
                            },
                            onShare = {
                                shareText(outputText)
                            },
                            isDarkTheme = settings.isDarkTheme,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    // Mobile Portrait View: Support BOTH (Split View), INPUT only, and OUTPUT only
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = {
                            if (targetState > initialState) {
                                (slideInHorizontally { width -> width / 4 } + fadeIn(animationSpec = tween(200)))
                                    .togetherWith(slideOutHorizontally { width -> -width / 4 } + fadeOut(animationSpec = tween(160)))
                            } else {
                                (slideInHorizontally { width -> -width / 4 } + fadeIn(animationSpec = tween(200)))
                                    .togetherWith(slideOutHorizontally { width -> width / 4 } + fadeOut(animationSpec = tween(160)))
                            }
                        },
                        label = "tab_content_anim",
                        modifier = Modifier.fillMaxSize()
                    ) { tabIndex ->
                        when (tabIndex) {
                            0 -> {
                                // BOTH (Split View in portrait): Simultaneous Input & Output!
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    InputPanel(
                                        text = inputText,
                                        onTextChanged = { inputText = it },
                                        onPaste = {
                                            val clip = clipboard.primaryClip
                                            if (clip != null && clip.itemCount > 0) {
                                                val pasteText = clip.getItemAt(0).coerceToText(context).toString()
                                                inputText = pasteText
                                                Toast.makeText(context, "Pasted from clipboard!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onClear = {
                                            inputText = ""
                                        },
                                        onSampleText = {
                                            inputText = sampleText
                                        },
                                        isDarkTheme = settings.isDarkTheme,
                                        modifier = Modifier
                                            .weight(0.95f)
                                            .fillMaxWidth()
                                    )

                                    OutputPanel(
                                        rawInputText = inputText,
                                        rawOutputText = outputText,
                                        activePresetName = activePresetName,
                                        isLoading = isLoading,
                                        isSpeaking = isSpeaking,
                                        showDiff = showDiff,
                                        onToggleDiff = { showDiff = !showDiff },
                                        onToggleSpeak = {
                                            if (outputText.isNotBlank() && outputText != "Result will appear here...") {
                                                ttsManager.toggleSpeak(outputText)
                                            }
                                        },
                                        onCopy = {
                                            if (outputText.isNotBlank()) {
                                                clipboard.setPrimaryClip(ClipData.newPlainText("Verbatim Enhanced", outputText))
                                                Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        onUseAsInput = {
                                            inputText = outputText
                                            Toast.makeText(context, "Output transferred to input!", Toast.LENGTH_SHORT).show()
                                        },
                                        onShare = {
                                            shareText(outputText)
                                        },
                                        isDarkTheme = settings.isDarkTheme,
                                        modifier = Modifier
                                            .weight(1.05f)
                                            .fillMaxWidth()
                                    )
                                }
                            }
                            1 -> {
                                // INPUT ONLY: Full height focus
                                InputPanel(
                                    text = inputText,
                                    onTextChanged = { inputText = it },
                                    onPaste = {
                                        val clip = clipboard.primaryClip
                                        if (clip != null && clip.itemCount > 0) {
                                            val pasteText = clip.getItemAt(0).coerceToText(context).toString()
                                            inputText = pasteText
                                            Toast.makeText(context, "Pasted from clipboard!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onClear = {
                                        inputText = ""
                                    },
                                    onSampleText = {
                                        inputText = sampleText
                                    },
                                    isDarkTheme = settings.isDarkTheme,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> {
                                // OUTPUT ONLY: Full height focus
                                OutputPanel(
                                    rawInputText = inputText,
                                    rawOutputText = outputText,
                                    activePresetName = activePresetName,
                                    isLoading = isLoading,
                                    isSpeaking = isSpeaking,
                                    showDiff = showDiff,
                                    onToggleDiff = { showDiff = !showDiff },
                                    onToggleSpeak = {
                                        if (outputText.isNotBlank() && outputText != "Result will appear here...") {
                                            ttsManager.toggleSpeak(outputText)
                                        }
                                    },
                                    onCopy = {
                                        if (outputText.isNotBlank()) {
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Verbatim Enhanced", outputText))
                                            Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onUseAsInput = {
                                        inputText = outputText
                                        selectedTab = 0
                                        Toast.makeText(context, "Output transferred to input!", Toast.LENGTH_SHORT).show()
                                    },
                                    onShare = {
                                        shareText(outputText)
                                    },
                                    isDarkTheme = settings.isDarkTheme,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // History BottomSheet
    if (showHistorySheet) {
        HistoryBottomSheet(
            history = history,
            historyMode = settings.historyMode,
            isDarkTheme = settings.isDarkTheme,
            onDismiss = { showHistorySheet = false },
            onRestore = { entry ->
                inputText = entry.input
                outputText = entry.output
                activePresetName = entry.tone
                showHistorySheet = false
                selectedTab = 0
                Toast.makeText(context, "Restored from logs!", Toast.LENGTH_SHORT).show()
            },
            onCopyOriginal = { text ->
                clipboard.setPrimaryClip(ClipData.newPlainText("Original", text))
                Toast.makeText(context, "Copied original text!", Toast.LENGTH_SHORT).show()
            },
            onCopyEnhanced = { text ->
                clipboard.setPrimaryClip(ClipData.newPlainText("Enhanced", text))
                Toast.makeText(context, "Copied enhanced text!", Toast.LENGTH_SHORT).show()
            },
            onDelete = { id ->
                settingsRepository.deleteHistoryEntry(id)
                Toast.makeText(context, "Log removed", Toast.LENGTH_SHORT).show()
            },
            onClearAll = {
                settingsRepository.clearAllHistory()
                Toast.makeText(context, "All logs cleared.", Toast.LENGTH_SHORT).show()
            },
            onOpenSettings = {
                showHistorySheet = false
                showSettingsDialog = true
            }
        )
    }

    // Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            initialSettings = settings,
            customPresets = customPresets,
            isDarkTheme = settings.isDarkTheme,
            onDismiss = { showSettingsDialog = false },
            onSaveSettings = { updated ->
                settingsRepository.saveSettings(updated)
                Toast.makeText(context, "Settings saved!", Toast.LENGTH_SHORT).show()
            },
            onTestConnection = { key, model, callback ->
                scope.launch {
                    val res = geminiRepository.testConnection(key, model)
                    res.onSuccess { callback(true, "Connection successful!") }
                        .onFailure { callback(false, it.message ?: "Failed") }
                }
            },
            onAddOrUpdatePreset = { id, name, prompt ->
                if (id == null) {
                    settingsRepository.addCustomPreset(name, prompt)
                    Toast.makeText(context, "Added preset \"$name\"!", Toast.LENGTH_SHORT).show()
                } else {
                    settingsRepository.updateCustomPreset(id, name, prompt)
                    Toast.makeText(context, "Updated preset \"$name\"!", Toast.LENGTH_SHORT).show()
                }
            },
            onDeletePreset = { id ->
                settingsRepository.deleteCustomPreset(id)
                Toast.makeText(context, "Preset deleted", Toast.LENGTH_SHORT).show()
            },
            onMovePresetUp = { id -> settingsRepository.movePresetUp(id) },
            onMovePresetDown = { id -> settingsRepository.movePresetDown(id) },
            onExportSettings = {
                try {
                    val json = settingsRepository.exportSettingsJson()
                    val file = File(context.cacheDir, "verbatim-settings-backup.json")
                    file.writeText(json)
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Export Verbatim Settings"))
                } catch (e: Exception) {
                    Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            onImportSettings = {
                filePickerLauncher.launch("application/json")
            },
            onResetDefaults = { clearHistory ->
                settingsRepository.resetToDefaults(clearHistory)
                Toast.makeText(context, "Reset all settings to factory defaults!", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
