package com.bolt.diy.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bolt.diy.data.model.ChatMessage
import com.bolt.diy.data.model.ChatSession
import com.bolt.diy.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
  private val repository: ChatRepository
) : ViewModel() {

  private val _sessions = MutableStateFlow<List<ChatSession>>(emptyList())
  val sessions: StateFlow<List<ChatSession>> = _sessions.asStateFlow()

  private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
  val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

  init {
    viewModelScope.launch {
      repository.getAllSessions().collect { sessionEntities ->
        _sessions.value = sessionEntities
      }
    }
  }

  fun loadMessages(sessionId: String) {
    viewModelScope.launch {
      repository.getSessionMessages(sessionId).collect { msgList ->
        _messages.value = msgList
      }
    }
  }

  fun sendMessage(sessionId: String, message: ChatMessage) {
    viewModelScope.launch {
      repository.addMessage(sessionId, message)
    }
  }

  fun addAssistantMessage(sessionId: String, content: String) {
    val assistantMsg = ChatMessage(
      id = java.util.UUID.randomUUID().toString(),
      role = com.bolt.diy.data.model.Role.ASSISTANT,
      content = content,
      timestamp = System.currentTimeMillis()
    )
    viewModelScope.launch {
      repository.addMessage(sessionId, assistantMsg)
    }
  }

  fun createNewChat(title: String, model: String?) {
    viewModelScope.launch {
      repository.createSession(title, model)
    }
  }

  fun deleteChat(sessionId: String) {
    viewModelScope.launch {
      repository.deleteSession(sessionId)
    }
  }

  fun forkChat(originalSession: ChatSession) {
    viewModelScope.launch {
      repository.forkSession(originalSession)
    }
  }
}
