package com.verbatim.studio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class TTSManager(
    context: Context,
    private val onStateChanged: ((Boolean) -> Unit)? = null
) : TextToSpeech.OnInitListener {

    private val tag = "TTSManager"
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false
    private var isCurrentlySpeaking = false

    init {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                isCurrentlySpeaking = true
                onStateChanged?.invoke(true)
            }

            override fun onDone(utteranceId: String?) {
                isCurrentlySpeaking = false
                onStateChanged?.invoke(false)
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                isCurrentlySpeaking = false
                onStateChanged?.invoke(false)
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                Log.e(tag, "TTS error: $errorCode for utteranceId: $utteranceId")
                isCurrentlySpeaking = false
                onStateChanged?.invoke(false)
            }
        })
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.getDefault())
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.US)
            }
            isInitialized = true
        } else {
            Log.e(tag, "TTS Initialization failed with status: $status")
        }
    }

    fun speak(text: String) {
        if (!isInitialized) return
        stop()
        val params = Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "verbatim_speech_${System.currentTimeMillis()}")
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "verbatim_speech")
    }

    fun stop() {
        if (tts?.isSpeaking == true) {
            tts?.stop()
        }
        isCurrentlySpeaking = false
        onStateChanged?.invoke(false)
    }

    fun isSpeaking(): Boolean {
        return isCurrentlySpeaking || (tts?.isSpeaking == true)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        isCurrentlySpeaking = false
    }
}
