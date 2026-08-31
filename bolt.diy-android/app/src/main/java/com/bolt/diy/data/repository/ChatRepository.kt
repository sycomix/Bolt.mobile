package com.bolt.diy.data.repository

import com.bolt.diy.data.local.BoltDatabase
import com.bolt.diy.data.model.ChatMessage
import com.bolt.diy.data.model.ChatSession
import com.bolt.diy.data.model.Role
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class ChatRepository(private val database: BoltDatabase) {

  private val json = Json { ignoreUnknownKeys = true }

  fun getAllSessions(): Flow<List<ChatSession>> = database.chatSessionDao().getAllSessions()
    .map { entities ->
      entities.map { entity ->
        ChatSession(
          id = entity.id,
          title = entity.title,
          messages = try {
            json.decodeFromString<List<ChatMessage>>(entity.messagesJson)
          } catch (e: Exception) {
            emptyList()
          },
          createdAt = entity.createdAt,
          updatedAt = entity.updatedAt,
          model = entity.model,
          contextOptimization = entity.contextOptimization,
          buildMode = entity.buildMode
        )
      }
    }

  suspend fun getSession(id: String): ChatSession? {
    val entity = database.chatSessionDao().getSessionById(id) ?: return null
    return ChatSession(
      id = entity.id,
      title = entity.title,
      messages = try {
        json.decodeFromString<List<ChatMessage>>(entity.messagesJson)
      } catch (e: Exception) {
        emptyList()
      },
      createdAt = entity.createdAt,
      updatedAt = entity.updatedAt,
      model = entity.model,
      contextOptimization = entity.contextOptimization,
      buildMode = entity.buildMode
    )
  }

  suspend fun createSession(title: String, model: String?): ChatSession {
    val now = System.currentTimeMillis()
    val session = ChatSession(
      id = java.util.UUID.randomUUID().toString(),
      title = title,
      createdAt = now,
      updatedAt = now,
      model = model
    )
    database.chatSessionDao().insertSession(
      com.bolt.diy.data.local.ChatSessionEntity(
        id = session.id,
        title = session.title,
        messagesJson = json.encodeToString(session.messages),
        createdAt = now,
        updatedAt = now,
        model = model,
        contextOptimization = session.contextOptimization,
        buildMode = session.buildMode
      )
    )
    return session
  }

  suspend fun updateSessionTitle(sessionId: String, title: String) {
    database.chatSessionDao().updateSessionTitle(
      sessionId = sessionId,
      title = title,
      updatedAt = System.currentTimeMillis()
    )
  }

  suspend fun deleteSession(id: String) {
    val entity = database.chatSessionDao().getSessionById(id) ?: return
    database.chatSessionDao().deleteSession(entity)
    database.chatMessageDao().deleteMessagesForSession(id)
  }

  suspend fun addMessage(sessionId: String, message: ChatMessage) {
    database.chatMessageDao().insertMessage(
      com.bolt.diy.data.local.ChatMessageEntity(
        id = message.id,
        sessionId = sessionId,
        role = message.role.name,
        content = message.content,
        timestamp = message.timestamp,
        model = message.model
      )
    )
  }

  fun getSessionMessages(sessionId: String): Flow<List<ChatMessage>> {
    return database.chatMessageDao().getMessagesForSession(sessionId)
      .map { entities ->
        entities.map { entity ->
          ChatMessage(
            id = entity.id,
            role = try {
              Role.valueOf(entity.role)
            } catch (e: Exception) {
              Role.USER
            },
            content = entity.content,
            timestamp = entity.timestamp,
            model = entity.model
          )
        }
      }
  }

  suspend fun forkSession(originalSession: ChatSession): ChatSession {
    val newId = java.util.UUID.randomUUID().toString()
    val now = System.currentTimeMillis()
    val newSession = originalSession.copy(
      id = newId,
      title = "${originalSession.title} (copy)",
      createdAt = now,
      updatedAt = now
    )
    database.chatSessionDao().insertSession(
      com.bolt.diy.data.local.ChatSessionEntity(
        id = newSession.id,
        title = newSession.title,
        messagesJson = json.encodeToString(newSession.messages),
        createdAt = now,
        updatedAt = now,
        model = newSession.model,
        contextOptimization = newSession.contextOptimization,
        buildMode = newSession.buildMode
      )
    )
    return newSession
  }
}
