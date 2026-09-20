package com.example.voice

import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceController(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    init {
        try {
            tts = TextToSpeech(context, this)
        } catch (e: Exception) {
            Log.e("VoiceController", "Failed to init TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            tts?.language = Locale.US
        }
    }

    fun speak(text: String, languageCode: String = "en", rate: Float = 1.0f, pitch: Float = 1.0f) {
        if (!isTtsReady || tts == null) return

        try {
            val locale = if (languageCode == "bn") {
                Locale("bn", "BD")
            } else {
                Locale.US
            }

            val langResult = tts?.setLanguage(locale)
            if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to default
                tts?.language = Locale.US
            }

            tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
            tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))

            _isSpeaking.value = true
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "AI_SPEECH_ID")
        } catch (e: Exception) {
            Log.e("VoiceController", "Error speaking text", e)
        }
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun getSpeechInputIntent(languageCode: String = "en"): Intent {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            val langTag = if (languageCode == "bn") "bn-BD" else "en-US"
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
            putExtra(RecognizerIntent.EXTRA_PROMPT, if (languageCode == "bn") "কথা বলুন..." else "Listening to your voice...")
        }
        return intent
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
