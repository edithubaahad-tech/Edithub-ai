package com.example.data.ai

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceoverEngine(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _supportedLanguages = MutableStateFlow<List<String>>(
        listOf("English", "Hindi", "Marathi", "Tamil", "Telugu", "Bengali")
    )
    val supportedLanguages: StateFlow<List<String>> = _supportedLanguages.asStateFlow()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.language = Locale.ENGLISH
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }
            })
        }
    }

    fun speak(
        text: String,
        language: String,
        speed: Float = 1.0f,
        pitch: Float = 1.0f
    ) {
        if (!isInitialized || tts == null) return

        val locale = when (language.lowercase()) {
            "hindi" -> Locale("hi", "IN")
            "marathi" -> Locale("mr", "IN")
            "tamil" -> Locale("ta", "IN")
            "telugu" -> Locale("te", "IN")
            "bengali" -> Locale("bn", "IN")
            else -> Locale.ENGLISH
        }

        try {
            val result = tts?.setLanguage(locale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.ENGLISH
            }
        } catch (e: Exception) {
            tts?.language = Locale.ENGLISH
        }

        tts?.setSpeechRate(speed.coerceIn(0.5f, 2.0f))
        tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "edithub_utterance_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
