package com.bolt.diy.data.local

import android.content.Context
import androidx.room.*
import com.bolt.diy.data.model.ChatMessage
import com.bolt.diy.data.model.ChatSession
import com.bolt.diy.data.model.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "chat_sessions")
data class ChatSessionEntity(
  @PrimaryKey val id: String,
  val title: String,
  val messagesJson: String,
  val createdAt: Long,
  val updatedAt: Long,
  val model: String? = null,
  val contextOptimization: Boolean = true,
  val buildMode: Boolean = false
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
  @PrimaryKey val id: String,
  val sessionId: String,
  val role: String,
  val content: String,
  val timestamp: Long,
  val model: String? = null
)

@Dao
interface ChatSessionDao {
  @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC")
  fun getAllSessions(): Flow<List<ChatSessionEntity>>

  @Query("SELECT * FROM chat_sessions WHERE id = :sessionId")
  suspend fun getSessionById(sessionId: String): ChatSessionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSession(session: ChatSessionEntity)

  @Delete
  suspend fun deleteSession(session: ChatSessionEntity)

  @Query("UPDATE chat_sessions SET title = :title, updatedAt = :updatedAt WHERE id = :sessionId")
  suspend fun updateSessionTitle(sessionId: String, title: String, updatedAt: Long)
}

@Dao
interface ChatMessageDao {
  @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
  fun getMessagesForSession(sessionId: String): Flow<List<ChatMessageEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMessage(message: ChatMessageEntity)

  @Delete
  suspend fun deleteMessage(message: ChatMessageEntity)

  @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
  suspend fun deleteMessagesForSession(sessionId: String)
}

@Database(entities = [ChatSessionEntity::class, ChatMessageEntity::class], version = 1, exportSchema = false)
abstract class BoltDatabase : RoomDatabase() {
  abstract fun chatSessionDao(): ChatSessionDao
  abstract fun chatMessageDao(): ChatMessageDao

  companion object {
    @Volatile
    private var INSTANCE: BoltDatabase? = null

    fun getDatabase(context: Context): BoltDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          BoltDatabase::class.java,
          "bolt_database"
        ).build()
        INSTANCE = instance
        instance
      }
    }
  }
}
