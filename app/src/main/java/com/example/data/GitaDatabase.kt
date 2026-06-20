package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- 1. Entities ---

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_shlokas", primaryKeys = ["chapter", "verse"])
data class FavoriteShloka(
    val chapter: Int,
    val verse: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val aiSummary: String,
    val mood: String, // e.g., "Sad", "Confused", "Angry", "Neutral", "Stressed"
    val timestamp: Long = System.currentTimeMillis()
)

// --- 2. DAOs ---

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getChatHistory(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatHistory()
}

@Dao
interface FavoriteShlokaDao {
    @Query("SELECT * FROM favorite_shlokas ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<FavoriteShloka>>

    @Query("SELECT EXISTS(SELECT * FROM favorite_shlokas WHERE chapter = :chapter AND verse = :verse)")
    fun isFavoriteFlg(chapter: Int, verse: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(shloka: FavoriteShloka)

    @Delete
    suspend fun removeFavorite(shloka: FavoriteShloka)
}

@Dao
interface JournalEntryDao {
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry)

    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Int)
}

// --- 3. Database ---

@Database(
    entities = [ChatMessage::class, FavoriteShloka::class, JournalEntry::class],
    version = 1,
    exportSchema = false
)
abstract class GitaDatabase : RoomDatabase() {
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun favoriteShlokaDao(): FavoriteShlokaDao
    abstract fun journalEntryDao(): JournalEntryDao

    companion object {
        @Volatile
        private var INSTANCE: GitaDatabase? = null

        fun getDatabase(context: Context): GitaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GitaDatabase::class.java,
                    "gita_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
