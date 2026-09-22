package com.verbatim.studio

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.webkit.WebViewAssetLoader
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var ttsManager: TTSManager
    private lateinit var nativeBridge: VerbatimNativeBridge

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            handleSelectedJsonFile(uri)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge layout
        WindowCompat.setDecorFitsSystemWindows(window, false)

        webView = WebView(this)
        setContentView(webView)

        // Handle window insets (status bar / nav bar padding)
        ViewCompat.setOnApplyWindowInsetsListener(webView) { view, insets ->
            val statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            val navBars = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
            view.setPadding(0, statusBars.top, 0, navBars.bottom)
            insets
        }

        // Initialize TTS Manager with JS callback
        ttsManager = TTSManager(this) { isSpeaking ->
            runOnUiThread {
                webView.evaluateJavascript(
                    "if (window.onSpeechStateChanged) { window.onSpeechStateChanged($isSpeaking); }",
                    null
                )
            }
        }

        nativeBridge = VerbatimNativeBridge(this, webView, ttsManager)

        // Setup Asset Loader for local assets
        val assetLoader = WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            cacheMode = WebSettings.LOAD_DEFAULT
            loadsImagesAutomatically = true
            useWideViewPort = true
            loadWithOverviewMode = true
            textZoom = 100
        }

        webView.isVerticalScrollBarEnabled = false
        webView.isHorizontalScrollBarEnabled = false
        webView.overScrollMode = View.OVER_SCROLL_NEVER

        webView.addJavascriptInterface(nativeBridge, "AndroidBridge")

        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                return assetLoader.shouldInterceptRequest(request.url)
            }
        }

        webView.webChromeClient = object : WebChromeClient() {}

        // Load the offline Verbatim HTML application
        webView.loadUrl("https://appassets.androidplatform.net/assets/www/index.html")

        // Handle Android Back Navigation
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                webView.evaluateJavascript("window.handleAndroidBackPress ? window.handleAndroidBackPress() : false") { result ->
                    val handled = result?.trim()?.equals("true", ignoreCase = true) == true
                    if (!handled) {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                        isEnabled = true
                    }
                }
            }
        })
    }

    fun launchFilePicker() {
        filePickerLauncher.launch("application/json")
    }

    private fun handleSelectedJsonFile(uri: Uri) {
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val reader = BufferedReader(InputStreamReader(inputStream))
                val content = reader.readText()
                val escapedJson = org.json.JSONObject.quote(content)
                webView.evaluateJavascript(
                    "if (window.handleImportedJson) { window.handleImportedJson($escapedJson); }",
                    null
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            nativeBridge.showToast("Failed to read settings file: ${e.message}", true)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
        webView.destroy()
    }
}
