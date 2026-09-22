package com.verbatim.studio

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VerbatimNativeBridge(
    private val activity: MainActivity,
    private val webView: WebView,
    private val ttsManager: TTSManager
) {

    private val clipboard: ClipboardManager =
        activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = activity.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        activity.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    @JavascriptInterface
    fun isAndroidApp(): Boolean = true

    @JavascriptInterface
    fun copyToClipboard(text: String, label: String?): Boolean {
        return try {
            val clip = ClipData.newPlainText(label ?: "Verbatim", text)
            clipboard.setPrimaryClip(clip)
            vibrateClick()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @JavascriptInterface
    fun pasteFromClipboard(): String {
        return try {
            val clip = clipboard.primaryClip
            if (clip != null && clip.itemCount > 0) {
                clip.getItemAt(0).coerceToText(activity).toString()
            } else {
                ""
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    @JavascriptInterface
    fun speak(text: String) {
        activity.runOnUiThread {
            ttsManager.speak(text)
        }
    }

    @JavascriptInterface
    fun stopSpeaking() {
        activity.runOnUiThread {
            ttsManager.stop()
        }
    }

    @JavascriptInterface
    fun isSpeaking(): Boolean {
        return ttsManager.isSpeaking()
    }

    @JavascriptInterface
    fun vibrateClick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @JavascriptInterface
    fun exportSettings(jsonContent: String) {
        activity.runOnUiThread {
            try {
                val exportDir = File(activity.cacheDir, "exports")
                if (!exportDir.exists()) exportDir.mkdirs()

                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val fileName = "verbatim-settings-backup-${dateFormat.format(Date())}.json"
                val file = File(exportDir, fileName)
                file.writeText(jsonContent)

                val uri = FileProvider.getUriForFile(
                    activity,
                    "${activity.packageName}.fileprovider",
                    file
                )

                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                activity.startActivity(
                    Intent.createChooser(sendIntent, "Export Verbatim Settings")
                )
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Failed to export settings: ${e.message}", true)
            }
        }
    }

    @JavascriptInterface
    fun triggerImportSettings() {
        activity.runOnUiThread {
            activity.launchFilePicker()
        }
    }

    @JavascriptInterface
    fun showToast(message: String, isError: Boolean) {
        activity.runOnUiThread {
            Toast.makeText(activity, message, if (isError) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
        }
    }
}
