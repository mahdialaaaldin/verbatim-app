package com.verbatim.studio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.verbatim.studio.model.AppSettings
import com.verbatim.studio.model.GeminiModels
import com.verbatim.studio.model.Preset
import com.verbatim.studio.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    initialSettings: AppSettings,
    customPresets: List<Preset>,
    isDarkTheme: Boolean,
    onDismiss: () -> Unit,
    onSaveSettings: (AppSettings) -> Unit,
    onTestConnection: (apiKey: String, model: String, onResult: (Boolean, String) -> Unit) -> Unit,
    onAddOrUpdatePreset: (id: String?, name: String, prompt: String) -> Unit,
    onDeletePreset: (String) -> Unit,
    onMovePresetUp: (String) -> Unit,
    onMovePresetDown: (String) -> Unit,
    onExportSettings: () -> Unit,
    onImportSettings: () -> Unit,
    onResetDefaults: (clearHistory: Boolean) -> Unit
) {
    var apiKey by remember { mutableStateOf(initialSettings.geminiApiKey) }
    var showApiKey by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf(initialSettings.geminiModel) }
    var modelDropdownExpanded by remember { mutableStateOf(false) }

    var historyMode by remember { mutableStateOf(initialSettings.historyMode) }
    var historyRetention by remember { mutableStateOf(initialSettings.historyRetention) }
    var customDaysText by remember { mutableStateOf(if (initialSettings.customRetentionDays > 0) initialSettings.customRetentionDays.toString() else "") }

    // Test status
    var testStatusText by remember { mutableStateOf<String?>(null) }
    var isTestingConnection by remember { mutableStateOf(false) }

    // Custom preset form
    var editingPresetId by remember { mutableStateOf<String?>(null) }
    var newPresetName by remember { mutableStateOf("") }
    var newPresetPrompt by remember { mutableStateOf("") }

    // Reset confirm dialog
    var showResetConfirm by remember { mutableStateOf(false) }
    var resetClearHistory by remember { mutableStateOf(false) }

    val dialogBg = if (isDarkTheme) Color(0xFF0B0F1A) else SurfaceLight
    val cardBg = if (isDarkTheme) Color(0xFF0F172A).copy(alpha = 0.5f) else SurfaceVariantLight.copy(alpha = 0.6f)
    val cardBorder = if (isDarkTheme) BorderDark else BorderLight

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.88f)
                .clip(RoundedCornerShape(22.dp))
                .border(1.dp, cardBorder, RoundedCornerShape(22.dp)),
            color = dialogBg,
            shadowElevation = 8.dp
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDarkTheme) SurfaceDark.copy(alpha = 0.5f) else SurfaceVariantLight.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = IndigoPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Settings & Privacy",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight
                        )
                    }
                }

                HorizontalDivider(color = cardBorder)

                // Body Scroll
                val scrollState = rememberScrollState()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // API Key Section
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "GEMINI API KEY",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDarkTheme) TextMutedDark else TextMutedLight
                        )
                        OutlinedTextField(
                            value = apiKey,
                            onValueChange = { apiKey = it },
                            placeholder = { Text("Paste gemini api key...", fontSize = 13.sp) },
                            visualTransformation = if (showApiKey) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showApiKey = !showApiKey }) {
                                    Icon(
                                        imageVector = if (showApiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = if (isDarkTheme) TextMutedDark else TextMutedLight
                                    )
                                }
                            },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IndigoPrimary,
                                unfocusedBorderColor = cardBorder
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "Key stays saved locally on your device. Sent via API header.",
                            fontSize = 10.sp,
                            color = if (isDarkTheme) TextMutedDark else TextMutedLight
                        )
                    }

                    // Model Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "GEMINI MODEL",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isDarkTheme) TextMutedDark else TextMutedLight
                        )
                        ExposedDropdownMenuBox(
                            expanded = modelDropdownExpanded,
                            onExpandedChange = { modelDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedModel,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = modelDropdownExpanded) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = IndigoPrimary,
                                    unfocusedBorderColor = cardBorder
                                ),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = modelDropdownExpanded,
                                onDismissRequest = { modelDropdownExpanded = false }
                            ) {
                                GeminiModels.allModels.forEach { model ->
                                    DropdownMenuItem(
                                        text = { Text(model) },
                                        onClick = {
                                            selectedModel = model
                                            modelDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Test Connection Button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                isTestingConnection = true
                                testStatusText = "Testing connection..."
                                onTestConnection(apiKey.trim(), selectedModel) { success, msg ->
                                    isTestingConnection = false
                                    testStatusText = if (success) "✅ Connection successful!" else "❌ $msg"
                                }
                            },
                            enabled = !isTestingConnection,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDarkTheme) SurfaceVariantDark else SurfaceVariantLight,
                                contentColor = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(text = "Test Connection", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }

                        testStatusText?.let { status ->
                            Text(
                                text = status,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (status.startsWith("✅")) EmeraldGreen else RoseRed
                            )
                        }
                    }

                    HorizontalDivider(color = cardBorder)

                    // History & Privacy Section
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = IndigoPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "HISTORY & PRIVACY CONTROL",
                                style = MaterialTheme.typography.labelSmall,
                                color = IndigoPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // History Mode
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "History Mode",
                                fontSize = 12.sp,
                                color = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(
                                    "enabled" to "Always Save",
                                    "incognito" to "Incognito",
                                    "disabled" to "Disabled"
                                ).forEach { (mode, label) ->
                                    FilterChip(
                                        selected = historyMode == mode,
                                        onClick = { historyMode = mode },
                                        label = { Text(label, fontSize = 11.sp) }
                                    )
                                }
                            }
                        }

                        // Retention
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Auto-Delete History After",
                                fontSize = 12.sp,
                                color = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(
                                    "never" to "Never",
                                    "1month" to "1 Mo",
                                    "1week" to "1 Wk",
                                    "3days" to "3 Days",
                                    "1day" to "1 Day",
                                    "custom" to "Custom"
                                ).forEach { (retention, label) ->
                                    FilterChip(
                                        selected = historyRetention == retention,
                                        onClick = { historyRetention = retention },
                                        label = { Text(label, fontSize = 10.sp) }
                                    )
                                }
                            }

                            if (historyRetention == "custom") {
                                OutlinedTextField(
                                    value = customDaysText,
                                    onValueChange = { customDaysText = it },
                                    label = { Text("Custom Duration (Days)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = cardBorder)

                    // Preset Prompts Section
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = null,
                                tint = IndigoPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "PRESET PROMPTS",
                                style = MaterialTheme.typography.labelSmall,
                                color = IndigoPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Add / Edit form
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(cardBg)
                                .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (editingPresetId == null) "Create New Preset" else "Edit Preset",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
                            )

                            OutlinedTextField(
                                value = newPresetName,
                                onValueChange = { newPresetName = it },
                                placeholder = { Text("Menu Item Name (e.g. Translate to French)", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = newPresetPrompt,
                                onValueChange = { newPresetPrompt = it },
                                placeholder = { Text("System Prompt Instructions...", fontSize = 12.sp) },
                                minLines = 2,
                                maxLines = 4,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        if (newPresetName.isNotBlank() && newPresetPrompt.isNotBlank()) {
                                            onAddOrUpdatePreset(editingPresetId, newPresetName.trim(), newPresetPrompt.trim())
                                            editingPresetId = null
                                            newPresetName = ""
                                            newPresetPrompt = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = if (editingPresetId == null) "Add Preset" else "Save Changes",
                                        fontSize = 11.sp
                                    )
                                }

                                if (editingPresetId != null) {
                                    TextButton(
                                        onClick = {
                                            editingPresetId = null
                                            newPresetName = ""
                                            newPresetPrompt = ""
                                        }
                                    ) {
                                        Text("Cancel", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // Custom Presets List
                        customPresets.forEachIndexed { index, preset ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(cardBg)
                                    .border(1.dp, cardBorder, RoundedCornerShape(10.dp))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Column {
                                        IconButton(
                                            onClick = { onMovePresetUp(preset.id) },
                                            enabled = index > 0,
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(Icons.Default.KeyboardArrowUp, null, modifier = Modifier.size(16.dp))
                                        }
                                        IconButton(
                                            onClick = { onMovePresetDown(preset.id) },
                                            enabled = index < customPresets.size - 1,
                                            modifier = Modifier.size(20.dp)
                                        ) {
                                            Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = preset.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isDarkTheme) TextPrimaryDark else TextPrimaryLight
                                        )
                                        Text(
                                            text = preset.prompt,
                                            fontSize = 10.sp,
                                            maxLines = 1,
                                            color = if (isDarkTheme) TextMutedDark else TextMutedLight
                                        )
                                    }
                                }

                                Row {
                                    IconButton(
                                        onClick = {
                                            editingPresetId = preset.id
                                            newPresetName = preset.name
                                            newPresetPrompt = preset.prompt
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, null, tint = IndigoPrimary, modifier = Modifier.size(15.dp))
                                    }
                                    IconButton(
                                        onClick = { onDeletePreset(preset.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, null, tint = RoseRed, modifier = Modifier.size(15.dp))
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = cardBorder)

                    // Backup & Restore Section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "BACKUP & RESTORE",
                            style = MaterialTheme.typography.labelSmall,
                            color = IndigoPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onExportSettings,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.FileDownload, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Export", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = onImportSettings,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.FileUpload, null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Import", fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = { showResetConfirm = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseRed),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Reset Defaults", fontSize = 11.sp)
                            }
                        }

                        if (showResetConfirm) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(RoseRed.copy(alpha = 0.08f))
                                    .border(1.dp, RoseRed.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Reset all preferences to factory defaults?",
                                    fontWeight = FontWeight.Bold,
                                    color = RoseRed,
                                    fontSize = 12.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Checkbox(
                                        checked = resetClearHistory,
                                        onCheckedChange = { resetClearHistory = it }
                                    )
                                    Text("Also clear revision history logs", fontSize = 11.sp)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            onResetDefaults(resetClearHistory)
                                            showResetConfirm = false
                                            onDismiss()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = RoseRed)
                                    ) {
                                        Text("Yes, Reset", fontSize = 11.sp)
                                    }
                                    TextButton(onClick = { showResetConfirm = false }) {
                                        Text("Cancel", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = cardBorder)

                // Footer Apply Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isDarkTheme) SurfaceDark.copy(alpha = 0.5f) else SurfaceVariantLight.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            val parsedDays = customDaysText.toFloatOrNull() ?: 0f
                            onSaveSettings(
                                initialSettings.copy(
                                    geminiApiKey = apiKey.trim(),
                                    geminiModel = selectedModel,
                                    historyMode = historyMode,
                                    historyRetention = historyRetention,
                                    customRetentionDays = parsedDays
                                )
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Apply & Close", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
