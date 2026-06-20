package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GitaViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GitaRepository(application)
    private var speaker: GitaSpeaker? = null

    // --- State Observables ---
    val chatHistory = repository.chatHistory
    val favoriteShlokas = repository.favoriteShlokas
    val journalEntries = repository.journalEntries

    // --- Daily Shloka ---
    var dailyShloka by mutableStateOf(ShlokaData.getShlokaForToday())
        private set

    // --- Live UI Settings States ---
    var personaMode by mutableStateOf(repository.getPersonaMode())
        private set
    var preferredLanguage by mutableStateOf(repository.getPreferredLanguage())
        private set
    var voiceEnabled by mutableStateOf(repository.isVoiceEnabled())
        private set
    var textSizeMultiplier by mutableStateOf(repository.getTextSizeMultiplier())
        private set

    // --- Live Interactive States ---
    var chatInputText by mutableStateOf("")
    var isChatLoading by mutableStateOf(false)
        private set

    // Camera Vision state
    var capturedBitmap by mutableStateOf<Bitmap?>(null)
    var cameraResultText by mutableStateOf("")
    var isCameraLoading by mutableStateOf(false)
        private set
    var cameraPrompt by mutableStateOf("What wisdom does the Bhagavad Gita have for my situation/document?")

    // Mood Solver state
    var selectedMood by mutableStateOf<String?>(null)
    var gitaMoodResponse by mutableStateOf("")
    var isMoodLoading by mutableStateOf(false)
        private set

    // Reflection Journal state
    var journalText by mutableStateOf("")
    var journalMood by mutableStateOf("Neutral")
    var isJournalLoading by mutableStateOf(false)
        private set

    init {
        speaker = GitaSpeaker(application)
    }

    // --- Theme / Settings Management ---
    fun updatePersonaMode(mode: String) {
        personaMode = mode
        repository.setPersonaMode(mode)
    }

    fun updateLanguage(lang: String) {
        preferredLanguage = lang
        repository.setPreferredLanguage(lang)
        // Update daily shloka just in case context changes or we want to force re-render
        dailyShloka = ShlokaData.getShlokaForToday()
    }

    fun updateVoiceEnabled(enabled: Boolean) {
        voiceEnabled = enabled
        repository.setVoiceEnabled(enabled)
        if (!enabled) {
            speaker?.stop()
        }
    }

    fun updateTextSize(multiplier: Float) {
        textSizeMultiplier = multiplier
        repository.setTextSizeMultiplier(multiplier)
    }

    // --- Chat Functions ---
    fun sendMessage() {
        val messageText = chatInputText.trim()
        if (messageText.isEmpty()) return

        chatInputText = ""
        isChatLoading = true

        viewModelScope.launch {
            // 1. Save user's message to local database immediately
            repository.saveMessage(messageText, isUser = true)

            // 2. Query Gemini API with the full persona and language requirements
            val response = repository.askGita(messageText)

            // 3. Save AI response to DB
            repository.saveMessage(response, isUser = false)
            isChatLoading = false

            // 4. Read aloud if voice is enabled
            if (voiceEnabled) {
                speaker?.speak(response, preferredLanguage)
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearHistory()
            speaker?.stop()
        }
    }

    // --- Voice Capabilities (Speak Response manually) ---
    fun speakText(text: String) {
        if (voiceEnabled) {
            speaker?.speak(text, preferredLanguage)
        }
    }

    fun stopSpeaking() {
        speaker?.stop()
    }

    // --- Shloka Operations ---
    fun toggleShlokaFavorite(chapter: Int, verse: Int) {
        viewModelScope.launch {
            repository.toggleFavorite(chapter, verse)
        }
    }

    fun isShlokaFavorite(chapter: Int, verse: Int): Flow<Boolean> {
        return repository.isFavorite(chapter, verse)
    }

    // --- Mood Solver Query ---
    fun solveMood(mood: String) {
        selectedMood = mood
        isMoodLoading = true
        gitaMoodResponse = ""

        viewModelScope.launch {
            val moodQuery = when (mood) {
                "Sad" -> "I am feeling deep sadness and sorrow. Everything feels heavy and dark right now."
                "Confused" -> "I am highly confused and indecisive. I do not know which path to take."
                "Angry" -> "I am filled with anger, frustration, and resentment at someone or something."
                "Stressed" -> "I am extremely stressed, anxious, and overwhelmed by duties, pressure, and future results."
                else -> "I feel neutral or emotionless, disconnected from excitement or sadness."
            }

            val prompt = "My current emotional state is: $mood. Here is how I feel: \"$moodQuery\". What Gita guidance and 1 simple Karma Yoga action step do you suggest? Please be encouraging but brief!"
            val response = repository.askGita(prompt)
            gitaMoodResponse = response
            isMoodLoading = false

            if (voiceEnabled) {
                speaker?.speak(response, preferredLanguage)
            }
        }
    }

    // --- Journal Functions ---
    fun submitJournalEntry() {
        val text = journalText.trim()
        if (text.isEmpty()) return

        journalText = ""
        isJournalLoading = true

        viewModelScope.launch {
            val prompt = "Review this mind reflection journal entry: \"$text\". Generate a brief, wise, 2-bullet summary highlighting the core spiritual/psychological mindset, and a compassionate coaching response. Use the language preferred: $preferredLanguage."
            val response = repository.askGita(prompt)

            repository.saveJournalEntry(text, response, journalMood)
            isJournalLoading = false
        }
    }

    fun deleteJournal(id: Int) {
        viewModelScope.launch {
            repository.deleteJournalEntry(id)
        }
    }

    // --- Camera Vision Functions ---
    fun runCameraAnalysis() {
        val bitmap = capturedBitmap ?: return
        isCameraLoading = true
        cameraResultText = ""

        viewModelScope.launch {
            val promptSuffix = " Looking at this image and situation, provide Gita-based guidance + practical advice. Prompt: $cameraPrompt"
            val response = repository.askGita(promptSuffix, bitmap)
            cameraResultText = response
            isCameraLoading = false

            if (voiceEnabled) {
                speaker?.speak(response, preferredLanguage)
            }
        }
    }

    fun resetCamera() {
        capturedBitmap = null
        cameraResultText = ""
    }

    override fun onCleared() {
        super.onCleared()
        speaker?.shutdown()
        speaker = null
    }
}
