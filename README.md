# Verbatim Studio - 100% Pure Native Android App

**Verbatim** is a premium AI-powered text editor built **100% natively for Android** using **Jetpack Compose**, **Material 3**, and **Kotlin Coroutines**. It supports **Android 11 and above** (API level 30+).

## Tech Stack & Architecture

- **100% Pure Native Jetpack Compose**: Zero WebViews, Zero HTML, Zero CSS/JS.
- **Material 3 Design**: Fully native UI components (Cards, BottomSheet, FilterChips, OutlinedTextField, Buttons, Dialogs).
- **Kotlin Coroutines**: Asynchronous Google Gemini API network calls.
- **OkHttp 4**: High-performance HTTP client with connection pooling, retries, and multi-model fallback.
- **Android Text-to-Speech (`TextToSpeech`)**: Native system voice playback.
- **Android `ClipboardManager` & Haptics**: Native copy/paste with tactile feedback.
- **Longest Common Subsequence (LCS) Diff**: Native Compose `AnnotatedString` with word-by-word highlighted insertions and deletions.
- **Minimum SDK**: **Android 11 (API 30)**
- **Target SDK**: **Android 15 (API 35)**

## Features

- **8 Built-in AI Presets**:
  - *Correct & Polish*: Spelling, grammar, and typography improvements.
  - *Professional*: Formal, persuasive, and authoritative tone.
  - *Conversational*: Friendly, natural, and relaxed style.
  - *Summarize*: Crisp 1-2 sentence core message.
  - *Bullet Points*: Structured takeaways.
  - *Expand*: Rich descriptive detail and depth.
  - *Sarcastic*: Sharp wit, dry humor, and satire.
  - *Prompt Architect*: LLM prompt optimization.
- **Custom Presets Manager**: Create, edit, reorder (up/down), and delete personalized system prompts.
- **Dual Workspace**:
  - Responsive animated sliding tabs (Input Text ↔ Output) for phones.
  - Two-pane side-by-side view for tablets and landscape.
- **Live Text Metrics**:
  - Live character count, word count, estimated reading time, and readability complexity rating.
- **Revision History**:
  - Material 3 `ModalBottomSheet` with restore, copy original, copy enhanced, and deletion.
  - One-tap Incognito mode and configurable auto-delete retention periods.
- **Settings & Privacy**:
  - Google Gemini API key entry, model selection (Flash Lite, Flash, Pro), connection test ping, export/import settings JSON.

## How to Build & Run

### In Android Studio
1. Open Android Studio.
2. Select **File > Open...** and choose this project folder.
3. Allow Gradle to sync.
4. Click **Run 'app'** or build via **Build > Build Bundle(s) / APK(s) > Build APK(s)**.

### Via Command Line
```bash
./gradlew assembleDebug
```
Output APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`
