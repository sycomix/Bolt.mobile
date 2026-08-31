package com.bolt.diy.data.local

import android.content.Context
import androidx.room.*
import com.bolt.diy.data.model.ChatMessage
import com.bolt.diy.data.model.ChatSession
import com.bolt.diy.data.model.Snapshot

/**
 * Room database for persisting chat sessions and snapshots.
 */
@Database(
  entities = [ChatSessionEntity::class, SnapshotEntity::class],
  version = 1,
  exportSchema = false
)
abstract class BoltDatabase : RoomDatabase() {
  abstract fun chatDao(): ChatDao

  companion object {
    private const val DB_NAME = "bolt_history"

    fun buildDatabase(context: Context): BoltDatabase {
      return Room.databaseBuilder(
        context.applicationContext,
        BoltDatabase::class.java,
        DB_NAME
      ).build()
    }
  }
}

/**
 * Room entity for chat sessions.
 */
@Entity(tableName = "chats")
data class ChatSessionEntity(
  @PrimaryKey val id: String,
  val urlId: String?,
  val description: String?,
  val messagesJson: String, // JSON serialized list of ChatMessage
  val timestamp: String,
  val metadataJson: String? // JSON serialized ChatMetadata
)

/**
 * Room entity for snapshots.
 */
@Entity(tableName = "snapshots")
data class SnapshotEntity(
  @PrimaryKey val chatId: String,
  val snapshotJson: String // JSON serialized Snapshot
)

/**
 * DAO for chat operations.
 */
@Dao
interface ChatDao {
  @Query("SELECT * FROM chats ORDER BY timestamp DESC")
  fun getAllChats(): kotlinx.coroutines.flow.Flow<List<ChatSessionEntity>>

  @Query("SELECT * FROM chats WHERE id = :id")
  suspend fun getChatById(id: String): ChatSessionEntity?

  @Query("SELECT * FROM chats WHERE urlId = :urlId")
  suspend fun getChatByUrlId(urlId: String): ChatSessionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertChat(chat: ChatSessionEntity)

  @Update
  suspend fun updateChat(chat: ChatSessionEntity)

  @Delete
  suspend fun deleteChat(chat: ChatSessionEntity)

  @Query("DELETE FROM chats WHERE id = :id")
  suspend fun deleteById(id: String)

  @Query("DELETE FROM chats")
  suspend fun deleteAllChats()

  @Query("SELECT MAX(CAST(id AS INTEGER)) FROM chats")
  suspend fun getNextId(): Int?

  // Snapshot operations
  @Query("SELECT * FROM snapshots WHERE chatId = :chatId")
  suspend fun getSnapshot(chatId: String): SnapshotEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSnapshot(snapshot: SnapshotEntity)

  @Delete
  suspend fun deleteSnapshot(snapshot: SnapshotEntity)

  @Query("DELETE FROM snapshots WHERE chatId = :chatId")
  suspend fun deleteSnapshotById(chatId: String)
}
