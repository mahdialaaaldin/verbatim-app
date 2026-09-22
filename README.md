# Verbatim Studio - Android App

**Verbatim** is a premium AI-powered text editor Android application supporting **Android 11 and above** (API level 30+).

## Features

- **Built-in & Custom AI Presets**:
  - *Correct & Polish*: Fixes typos, spelling, and grammar.
  - *Professional*: Elevates tone to formal, persuasive, and authoritative.
  - *Conversational*: Transforms text into friendly, natural speech.
  - *Summarize*: Condenses content into 1-2 core sentences.
  - *Bullet Points*: Extracts structured key takeaways.
  - *Expand*: Elaborates with rich descriptive clarity.
  - *Sarcastic*: Adds sharp wit, dry humor, and satire.
  - *Prompt Architect*: Engineers optimized LLM prompts.
  - *Custom Presets*: Create, edit, reorder, and persist personalized system prompt workflows.
- **Dual Workspace**:
  - Responsive mobile tab switcher (Input Text ↔ Output).
  - Side-by-side view on tablets and larger screens.
- **Visual Diff Inspector**:
  - Longest Common Subsequence (LCS) word-by-word diff engine highlighting additions (`ins`) and deletions (`del`).
- **Native Android Integration**:
  - Android `TextToSpeech` engine with live playback status.
  - Native `ClipboardManager` with haptic feedback.
  - Edge-to-edge system bars (status & navigation bar theme synchronization).
  - Storage Access Framework & `FileProvider` for JSON settings backup and restore.
  - Hardware back button handling for closing dialogs and drawers gracefully.
- **Privacy & History Control**:
  - Revision history logging (up to 25 entries).
  - One-tap Incognito Mode.
  - Configurable auto-delete retention periods (1 Day, 3 Days, 1 Week, 1 Month, Custom, Never).
- **Google Gemini Engine**:
  - Supports Gemini Flash Lite, Flash, and Pro models (`gemini-3.5-flash-lite`, `gemini-3.6-flash`, etc.).
  - Automatic multi-model fallback chain and retry logic on rate limits.
  - In-app API connection tester.

## Requirements

- Android Studio Jellyfish (2023.3.1) or newer / Ladybug / Koala
- JDK 17
- Minimum SDK: **Android 11 (API 30)**
- Target SDK: **Android 15 (API 35)**

## Opening & Building in Android Studio

1. Open Android Studio.
2. Select **File > Open...** and navigate to this folder:
   ```
   c:\Users\mahdi.alaaaldin\OneDrive - Montyholding\Documents\App
   ```
3. Let Gradle sync project dependencies.
4. Click **Run > Run 'app'** or use an Android 11+ emulator or physical device.

To build an APK via command line:
```bash
./gradlew assembleDebug
```
The output APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`
