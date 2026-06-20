package com.example.data

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class GitaRepository(private val context: Context) {
    private val db = GitaDatabase.getDatabase(context)
    private val chatMessageDao = db.chatMessageDao()
    private val favoriteShlokaDao = db.favoriteShlokaDao()
    private val journalEntryDao = db.journalEntryDao()

    private val sharedPrefs = context.getSharedPreferences("gita_ai_settings", Context.MODE_PRIVATE)

    // --- Chat History ---
    val chatHistory: Flow<List<ChatMessage>> = chatMessageDao.getChatHistory()

    suspend fun saveMessage(text: String, isUser: Boolean) {
        chatMessageDao.insertMessage(ChatMessage(text = text, isUser = isUser))
    }

    suspend fun clearHistory() {
        chatMessageDao.clearChatHistory()
    }

    // --- Favorite Shlokas ---
    val favoriteShlokas: Flow<List<FavoriteShloka>> = favoriteShlokaDao.getFavorites()

    fun isFavorite(chapter: Int, verse: Int): Flow<Boolean> {
        return favoriteShlokaDao.isFavoriteFlg(chapter, verse)
    }

    suspend fun toggleFavorite(chapter: Int, verse: Int) {
        val favorite = FavoriteShloka(chapter, verse)
        val exists = favoriteShlokaDao.isFavoriteFlg(chapter, verse).firstOrNull() ?: false
        if (exists) {
            favoriteShlokaDao.removeFavorite(favorite)
        } else {
            favoriteShlokaDao.insertFavorite(favorite)
        }
    }

    // --- Journal Entries ---
    val journalEntries: Flow<List<JournalEntry>> = journalEntryDao.getAllEntries()

    suspend fun saveJournalEntry(text: String, aiSummary: String, mood: String) {
        journalEntryDao.insertEntry(JournalEntry(text = text, aiSummary = aiSummary, mood = mood))
    }

    suspend fun deleteJournalEntry(id: Int) {
        journalEntryDao.deleteEntryById(id)
    }

    // --- App Settings (SharedPreferences) ---
    fun getPersonaMode(): String = sharedPrefs.getString("persona_mode", "Krishna Mode") ?: "Krishna Mode"
    fun setPersonaMode(mode: String) = sharedPrefs.edit().putString("persona_mode", mode).apply()

    fun getPreferredLanguage(): String = sharedPrefs.getString("preferred_lang", "English") ?: "English"
    fun setPreferredLanguage(lang: String) = sharedPrefs.edit().putString("preferred_lang", lang).apply()

    fun isVoiceEnabled(): Boolean = sharedPrefs.getBoolean("voice_enabled", true)
    fun setVoiceEnabled(enabled: Boolean) = sharedPrefs.edit().putBoolean("voice_enabled", enabled).apply()

    fun getTextSizeMultiplier(): Float = sharedPrefs.getFloat("text_size_multiplier", 1.0f)
    fun setTextSizeMultiplier(multiplier: Float) = sharedPrefs.edit().putFloat("text_size_multiplier", multiplier).apply()

    // --- Gemini Call Helper ---
    suspend fun askGita(prompt: String, bitmap: Bitmap? = null): String {
        val persona = getPersonaMode()
        val lang = getPreferredLanguage()
        return GeminiService.generateGitaResponse(
            prompt = prompt,
            bitmapImage = bitmap,
            personaMode = persona,
            language = lang
        )
    }
}
