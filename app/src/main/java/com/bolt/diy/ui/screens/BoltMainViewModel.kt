package com.bolt.diy.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bolt.diy.data.model.*
import com.bolt.diy.data.repository.ChatRepository
import com.bolt.diy.domain.service.FileSystemService
import com.bolt.diy.domain.service.LlmService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * Main ViewModel for the bolt.diy Android app.
 * Orchestrates chat, file system, and LLM services.
 */
@HiltViewModel
class BoltMainViewModel @Inject constructor(
  private val llmService: LlmService,
  private val fileSystemService: FileSystemService,
  private val chatRepository: ChatRepository
) : ViewModel() {

  // Current state
  private val _currentChatId = MutableStateFlow<String?>(null)
  val currentChatId: StateFlow<String?> = _currentChatId

  private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
  val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

  private val _isStreaming = MutableStateFlow(false)
  val isStreaming: StateFlow<Boolean> = _isStreaming

  private val _selectedProvider = MutableStateFlow("OpenAI")
  val selectedProvider: StateFlow<String> = _selectedProvider

  private val _selectedModel = MutableStateFlow("")
  val selectedModel: StateFlow<String> = _selectedModel

  private val _availableModels = MutableStateFlow<List<ModelInfo>>(emptyList())
  val availableModels: StateFlow<List<ModelInfo>> = _availableModels

  private val _chatMode = MutableStateFlow(ChatMode.BUILD)
  val chatMode: StateFlow<ChatMode> = _chatMode

  // File system state
  val files = fileSystemService.files

  // Chat list
  val chatList: Flow<List<ChatSession>> = chatRepository.getAllChats()

  /**
   * Create a new chat session.
   */
  fun createNewChat() {
    _currentChatId.value = UUID.randomUUID().toString()
    _chatMessages.value = emptyList()
    _selectedModel.value = ""
  }

  /**
   * Load an existing chat session from repository.
   */
  suspend fun loadChat(chatId: String) {
    val chat = chatRepository.getChatById(chatId) ?: return
    _currentChatId.value = chat.id
    _chatMessages.value = chat.messages
  }

  /**
   * Send a user message and get AI response.
   */
  fun sendMessage(content: String) {
    if (_isStreaming.value) return

    val userId = UUID.randomUUID().toString()
    val userMessage = ChatMessage(
      id = userId,
      role = ChatMessage.Role.USER,
      content = content
    )

    // Add user message to chat
    _chatMessages.update { messages -> messages + userMessage }

    // Start streaming AI response
    streamAIResponse(content)
  }

  /**
   * Stream AI response based on selected provider.
   */
  private fun streamAIResponse(userContent: String) {
    _isStreaming.value = true

    val providerName = _selectedProvider.value
    val modelId = _selectedModel.value

    // Get the current conversation history
    val messages = _chatMessages.value + ChatMessage(
      id = UUID.randomUUID().toString(),
      role = ChatMessage.Role.USER,
      content = userContent
    )

    viewModelScope.launch {
      try {
        var assistantResponse = ""
        val assistantId = UUID.randomUUID().toString()

        llmService.streamChatCompletion(
          providerName = providerName,
          apiKey = "", // Would come from settings repository
          baseUrl = null,
          messages = messages.map { msg ->
            ChatMessage(
              id = msg.id,
              role = msg.role,
              content = msg.content,
              timestamp = msg.timestamp,
              annotations = msg.annotations
            )
          },
          modelId = modelId,
          files = fileSystemService.getAllFiles(),
          contextOptimization = true,
          chatMode = _chatMode.value,
          designScheme = null,
          maxLLMSteps = 10,
          onChunk = { chunk ->
            assistantResponse += chunk
            // Update the last message (assistant response) in real-time
            _chatMessages.update { msgs ->
              val updatedMsgs = msgs.toMutableList()
              if (updatedMsgs.isNotEmpty()) {
                val lastMsg = updatedMsgs.last()
                if (lastMsg.role == ChatMessage.Role.ASSISTANT) {
                  updatedMsgs[updatedMsgs.size - 1] = lastMsg.copy(
                    content = assistantResponse
                  )
                } else {
                  updatedMsgs.add(
                    ChatMessage(
                      id = assistantId,
                      role = ChatMessage.Role.ASSISTANT,
                      content = assistantResponse
                    )
                  )
                }
              }
              updatedMsgs
            }
          },
          onComplete = {
            // Add final assistant message if not already added
            _chatMessages.update { msgs ->
              val updatedMsgs = msgs.toMutableList()
              if (updatedMsgs.isEmpty() || updatedMsgs.last().role != ChatMessage.Role.ASSISTANT) {
                updatedMsgs.add(
                  ChatMessage(
                    id = assistantId,
                    role = ChatMessage.Role.ASSISTANT,
                    content = assistantResponse
                  )
                )
              }
              updatedMsgs
            }

            // Save chat to repository
            _currentChatId.value?.let { chatId ->
              val finalMessages = _chatMessages.value
              val session = ChatSession(
                id = chatId,
                urlId = chatId,
                description = if (finalMessages.size <= 2) userContent else null,
                messages = finalMessages
              )
              viewModelScope.launch {
                chatRepository.saveChat(session)
              }
            }

            _isStreaming.value = false
          },
          onError = { error ->
            _chatMessages.update { msgs ->
              val updatedMsgs = msgs.toMutableList()
              updatedMsgs.add(
                ChatMessage(
                  id = assistantId,
                  role = ChatMessage.Role.ASSISTANT,
                  content = "[Error: $error]"
                )
              )
              updatedMsgs
            }
            _isStreaming.value = false
          }
        )

      } catch (e: Exception) {
        _chatMessages.update { msgs ->
          val updatedMsgs = msgs.toMutableList()
          updatedMsgs.add(
            ChatMessage(
              id = UUID.randomUUID().toString(),
              role = ChatMessage.Role.ASSISTANT,
              content = "[Error: ${e.message}]"
            )
          )
          updatedMsgs
        }
        _isStreaming.value = false
      }
    }
  }

  /**
   * Select a provider.
   */
  fun selectProvider(providerName: String) {
    _selectedProvider.value = providerName
    loadModelsForProvider(providerName)
  }

  /**
   * Load available models for a provider.
   */
  private fun loadModelsForProvider(providerName: String) {
    viewModelScope.launch {
      val models = LlmService.getModelsForProvider(providerName)
      _availableModels.value = models
      if (models.isNotEmpty()) {
        _selectedModel.value = models.first().id
      }
    }
  }

  /**
   * Select a model.
   */
  fun selectModel(modelId: String) {
    _selectedModel.value = modelId
  }

  /**
   * Set chat mode (build vs discuss).
   */
  fun setChatMode(mode: ChatMode) {
    _chatMode.value = mode
  }

  /**
   * Abort current streaming response.
   */
  fun abortStreaming() {
    _isStreaming.value = false
  }

  /**
   * Delete a chat.
   */
  suspend fun deleteChat(chatId: String) {
    chatRepository.deleteChat(chatId)
    if (_currentChatId.value == chatId) {
      createNewChat()
    }
  }

  /**
   * Duplicate a chat.
   */
  suspend fun duplicateChat(sourceChat: ChatSession): String {
    return chatRepository.duplicateChat(sourceChat)
  }

  /**
   * Create a file in the virtual filesystem.
   */
  suspend fun createFile(path: String, content: String) {
    fileSystemService.createFile(path, content)
  }

  /**
   * Save file content.
   */
  fun saveFile(path: String, content: String) {
    fileSystemService.saveFile(path, content)
  }

  /**
   * Delete a file.
   */
  suspend fun deleteFile(path: String) {
    fileSystemService.deleteFile(path)
  }
}
