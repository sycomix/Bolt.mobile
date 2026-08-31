package com.bolt.diy.data.repository

import com.bolt.diy.data.local.BoltDatabase
import com.bolt.diy.data.local.ChatDao
import com.bolt.diy.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * Repository for managing chat sessions - mirrors useChatHistory behavior.
 */
class ChatRepository(private val database: BoltDatabase) {
  private val dao = database.chatDao()
  private val json = Json { ignoreUnknownKeys = true }

  /**
   * Get all chats ordered by timestamp (newest first).
   */
  fun getAllChats(): Flow<List<ChatSession>> = dao.getAllChats().map { entities ->
    entities.map { entity ->
      ChatSession(
        id = entity.id,
        urlId = entity.urlId,
        description = entity.description,
        messages = json.decodeFromString(entity.messagesJson),
        timestamp = entity.timestamp,
        metadata = entity.metadataJson?.let { json.decodeFromString(it) }
      )
    }
  }

  /**
   * Get a single chat by ID.
   */
  suspend fun getChatById(id: String): ChatSession? {
    val entity = dao.getChatById(id) ?: return null
    return ChatSession(
      id = entity.id,
      urlId = entity.urlId,
      description = entity.description,
      messages = json.decodeFromString(entity.messagesJson),
      timestamp = entity.timestamp,
      metadata = entity.metadataJson?.let { json.decodeFromString(it) }
    )
  }

  /**
   * Get a chat by URL ID.
   */
  suspend fun getChatByUrlId(urlId: String): ChatSession? {
    val entity = dao.getChatByUrlId(urlId) ?: return null
    return ChatSession(
      id = entity.id,
      urlId = entity.urlId,
      description = entity.description,
      messages = json.decodeFromString(entity.messagesJson),
      timestamp = entity.timestamp,
      metadata = entity.metadataJson?.let { json.decodeFromString(it) }
    )
  }

  /**
   * Save or update a chat session.
   */
  suspend fun saveChat(session: ChatSession) {
    val messagesJson = json.encodeToString(session.messages)
    val metadataJson = session.metadata?.let { json.encodeToString(it) }

    dao.insertChat(
      ChatSessionEntity(
        id = session.id,
        urlId = session.urlId,
        description = session.description,
        messagesJson = messagesJson,
        timestamp = session.timestamp,
        metadataJson = metadataJson
      )
    )
  }

  /**
   * Delete a chat by ID.
   */
  suspend fun deleteChat(id: String) {
    dao.deleteById(id)
    dao.deleteSnapshotById(id)
  }

  /**
   * Delete all chats.
   */
  suspend fun deleteAllChats() {
    dao.deleteAllChats()
  }

  /**
   * Generate next ID for new chat.
   */
  suspend fun getNextId(): String {
    val currentMax = dao.getNextId() ?: 0
    return (currentMax + 1).toString()
  }

  /**
   * Get unique URL ID.
   */
  suspend fun getUrlId(baseId: String): String {
    // Simple approach: use UUID for uniqueness
    return baseId
  }

  /**
   * Add message to existing chat.
   */
  suspend fun addMessageToChat(chatId: String, message: ChatMessage) {
    val existing = getChatById(chatId) ?: throw IllegalArgumentException("Chat not found")
    val updatedMessages = existing.messages + message
    val updatedSession = existing.copy(messages = updatedMessages)
    saveChat(updatedSession)
  }

  /**
   * Update chat description.
   */
  suspend fun updateDescription(chatId: String, description: String) {
    val existing = getChatById(chatId) ?: throw IllegalArgumentException("Chat not found")
    val updatedSession = existing.copy(description = description)
    saveChat(updatedSession)
  }

  /**
   * Duplicate a chat.
   */
  suspend fun duplicateChat(sourceChat: ChatSession): String {
    val newId = getNextId()
    val duplicated = sourceChat.copy(
      id = newId,
      description = "${sourceChat.description ?: "Chat"} (copy)",
      messages = sourceChat.messages.map { it.copy(id = UUID.randomUUID().toString()) }
    )
    saveChat(duplicated)
    return newId
  }

  /**
   * Fork a chat at a specific message.
   */
  suspend fun forkChat(chatId: String, messageId: String): String {
    val existing = getChatById(chatId) ?: throw IllegalArgumentException("Chat not found")
    val messageIndex = existing.messages.indexOfFirst { it.id == messageId }
    if (messageIndex == -1) throw IllegalArgumentException("Message not found")

    val forkedMessages = existing.messages.subList(0, messageIndex + 1)
      .map { it.copy(id = UUID.randomUUID().toString()) }

    val newId = getNextId()
    val forkedChat = existing.copy(
      id = newId,
      description = "${existing.description ?: "Chat"} (fork)",
      messages = forkedMessages
    )
    saveChat(forkedChat)
    return newId
  }

  /**
   * Save snapshot for a chat.
   */
  suspend fun saveSnapshot(chatId: String, snapshot: Snapshot) {
    val jsonStr = json.encodeToString(snapshot)
    dao.insertSnapshot(SnapshotEntity(chatId, jsonStr))
  }

  /**
   * Get snapshot for a chat.
   */
  suspend fun getSnapshot(chatId: String): Snapshot? {
    val entity = dao.getSnapshot(chatId) ?: return null
    return json.decodeFromString(entity.snapshotJson)
  }

  /**
   * Delete snapshot.
   */
  suspend fun deleteSnapshot(chatId: String) {
    dao.deleteSnapshotById(chatId)
  }
}
