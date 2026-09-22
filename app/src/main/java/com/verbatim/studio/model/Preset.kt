package com.verbatim.studio.model

import androidx.compose.ui.graphics.Color
import com.verbatim.studio.theme.*

data class Preset(
    val id: String,
    val name: String,
    val prompt: String,
    val isCustom: Boolean = false,
    val lightBg: Color = IndigoPrimary.copy(alpha = 0.1f),
    val darkBg: Color = IndigoPrimary.copy(alpha = 0.15f),
    val lightBorder: Color = IndigoPrimary.copy(alpha = 0.2f),
    val darkBorder: Color = IndigoPrimary.copy(alpha = 0.3f),
    val textColor: Color = IndigoPrimary
)

object PresetDefaults {
    const val STRICT_FORMATTING_SUFFIX =
        "\n\nCRITICAL INSTRUCTION: Return ONLY the processed result as plain text. Do NOT include any introductory greetings, commentary, labels, quotes, explanations, or markdown code block formatting (unless raw markdown is explicitly requested)."

    val builtInPresets = listOf(
        Preset(
            id = "improve",
            name = "Correct & Polish",
            prompt = "Correct spelling, typos, and improve the general grammar of the following text.",
            lightBg = IndigoPrimary.copy(alpha = 0.08f),
            darkBg = IndigoPrimary.copy(alpha = 0.15f),
            lightBorder = IndigoPrimary.copy(alpha = 0.25f),
            darkBorder = IndigoPrimary.copy(alpha = 0.35f),
            textColor = IndigoPrimary
        ),
        Preset(
            id = "professional",
            name = "Professional",
            prompt = "Rewrite the following text to sound highly professional, formal, persuasive, and authoritative.",
            lightBg = EmeraldGreen.copy(alpha = 0.08f),
            darkBg = EmeraldGreen.copy(alpha = 0.15f),
            lightBorder = EmeraldGreen.copy(alpha = 0.25f),
            darkBorder = EmeraldGreen.copy(alpha = 0.35f),
            textColor = EmeraldGreen
        ),
        Preset(
            id = "casual",
            name = "Conversational",
            prompt = "Rewrite the following text to sound friendly, relaxed, conversational, and natural. Keep it casual and engaging.",
            lightBg = SkyBlue.copy(alpha = 0.08f),
            darkBg = SkyBlue.copy(alpha = 0.15f),
            lightBorder = SkyBlue.copy(alpha = 0.25f),
            darkBorder = SkyBlue.copy(alpha = 0.35f),
            textColor = SkyBlue
        ),
        Preset(
            id = "summarize",
            name = "Summarize",
            prompt = "Create a concise summary of the key message in the text. Return only 1 or 2 sentences of simple plain text.",
            lightBg = AmberYellow.copy(alpha = 0.08f),
            darkBg = AmberYellow.copy(alpha = 0.15f),
            lightBorder = AmberYellow.copy(alpha = 0.25f),
            darkBorder = AmberYellow.copy(alpha = 0.35f),
            textColor = AmberYellow
        ),
        Preset(
            id = "bullet",
            name = "Bullet Points",
            prompt = "Extract the key bullet points from the text. Format them with simple dashes (-).",
            lightBg = PurpleAccent.copy(alpha = 0.08f),
            darkBg = PurpleAccent.copy(alpha = 0.15f),
            lightBorder = PurpleAccent.copy(alpha = 0.25f),
            darkBorder = PurpleAccent.copy(alpha = 0.35f),
            textColor = PurpleAccent
        ),
        Preset(
            id = "expand",
            name = "Expand",
            prompt = "Elaborate on the following text by adding rich descriptive details, clarity, and depth while maintaining the exact core message.",
            lightBg = CyanAccent.copy(alpha = 0.08f),
            darkBg = CyanAccent.copy(alpha = 0.15f),
            lightBorder = CyanAccent.copy(alpha = 0.25f),
            darkBorder = CyanAccent.copy(alpha = 0.35f),
            textColor = CyanAccent
        ),
        Preset(
            id = "sarcastic",
            name = "Sarcastic",
            prompt = "Rewrite the following text with sharp, clever sarcasm and dry humor while preserving the original meaning.",
            lightBg = RoseRed.copy(alpha = 0.08f),
            darkBg = RoseRed.copy(alpha = 0.15f),
            lightBorder = RoseRed.copy(alpha = 0.25f),
            darkBorder = RoseRed.copy(alpha = 0.35f),
            textColor = RoseRed
        ),
        Preset(
            id = "prompt",
            name = "Prompt Architect",
            prompt = "Reconstruct the input text into a highly optimized, structured prompt tailored for advanced large language models (ChatGPT, Gemini).",
            lightBg = OrangeAccent.copy(alpha = 0.08f),
            darkBg = OrangeAccent.copy(alpha = 0.15f),
            lightBorder = OrangeAccent.copy(alpha = 0.25f),
            darkBorder = OrangeAccent.copy(alpha = 0.35f),
            textColor = OrangeAccent
        )
    )

    fun getPromptFor(preset: Preset, inputText: String): String {
        return "${preset.prompt}$STRICT_FORMATTING_SUFFIX\n\nText: \"$inputText\""
    }
}
