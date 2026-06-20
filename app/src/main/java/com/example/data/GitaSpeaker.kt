package com.example.data

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class GitaSpeaker(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingText: String? = null
    private var pendingLanguage: String? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            // If there's queued speech waiting for init, speak it now
            pendingText?.let { text ->
                speak(text, pendingLanguage ?: "English")
                pendingText = null
                pendingLanguage = null
            }
        } else {
            Log.e("GitaSpeaker", "TTS Initialization failed!")
        }
    }

    fun speak(text: String, language: String) {
        if (!isInitialized) {
            pendingText = text
            pendingLanguage = language
            return
        }

        val speechLocale = when (language) {
            "Hindi" -> Locale("hi", "IN")
            "Marathi" -> Locale("mr", "IN")
            else -> Locale.US
        }

        tts?.let {
            val result = it.setLanguage(speechLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to default
                it.language = Locale.US
            }
            // Clean markdown syntax from the text so it sounds more natural
            val cleanSpeech = text
                .replace("*", "")
                .replace("#", "")
                .replace("_", "")
                .replace("`", "")

            it.speak(cleanSpeech, TextToSpeech.QUEUE_FLUSH, null, "GitaAI_TTS")
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }
}
