package com.bolt.diy.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bolt.diy.data.model.*
import com.bolt.diy.domain.service.FileSystemService
import com.bolt.diy.domain.service.LlmService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * ViewModel for chat functionality.
 * Mirrors the web app's useChatHistory hook behavior.
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
  private val llmService: LlmService,
  private val fileSystemService: FileSystemService
) : ViewModel() {

  // Current chat state
  private val _currentChatId = MutableStateFlow<String?>(null)
  val currentChatId: StateFlow<String?> = _currentChatId

  private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
  val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages

  private val _isStreaming = MutableStateFlow(false)
  val isStreaming: StateFlow<Boolean> = _isStreaming

  private val _progressIndicators = MutableStateFlow<List<ProgressAnnotation>>(emptyList())
  val progressIndicators: StateFlow<List<ProgressAnnotation>> = _progressIndicators

  private val _selectedProvider = MutableStateFlow("OpenAI")
  val selectedProvider: StateFlow<String> = _selectedProvider

  private val _selectedModel = MutableStateFlow("")
  val selectedModel: StateFlow<String> = _selectedModel

  private val _availableModels = MutableStateFlow<List<ModelInfo>>(emptyList())
  val availableModels: StateFlow<List<ModelInfo>> = _availableModels

  private val _chatMode = MutableStateFlow(ChatMode.BUILD)
  val chatMode: StateFlow<ChatMode> = _chatMode

  private val _designScheme = MutableStateFlow(DesignScheme())
  val designScheme: StateFlow<DesignScheme> = _designScheme

  // File system state
  private val _files = fileSystemService.files
  val files: StateFlow<Map<String, Any>> = _files

  /**
   * Create a new chat session.
   */
  fun createNewChat() {
    _currentChatId.value = UUID.randomUUID().toString()
    _chatMessages.value = emptyList()
    _selectedModel.value = ""
  }

  /**
   * Load an existing chat session.
   */
  fun loadChat(chatId: String, messages: List<ChatMessage>) {
    _currentChatId.value = chatId
    _chatMessages.value = messages
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
        // Get provider config
        val providerConfig = getProviderConfig(providerName)

        // Stream the response
        var assistantResponse = ""
        val assistantId = UUID.randomUUID().toString()

        llmService.streamChatCompletion(
          providerName = providerName,
          apiKey = providerConfig?.apiKey ?: "",
          baseUrl = providerConfig?.baseUrl,
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
          designScheme = _designScheme.value,
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
   * Get provider configuration.
   */
  private fun getProviderConfig(providerName: String): ProviderConfig? {
    // In production, this would come from a repository or DI
    return when (providerName) {
      "OpenAI" -> ProviderConfig("OpenAI", "gsk_...", "https://api.openai.com")
      "Anthropic" -> ProviderConfig("Anthropic", "sk-ant-...", "https://api.anthropic.com")
      "Google" -> ProviderConfig("Google", "", "https://generativelanguage.googleapis.com")
      else -> null
    }
  }

  /**
   * Select a provider.
   */
  fun selectProvider(providerName: String) {
    _selectedProvider.value = providerName
    // Load models for this provider
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
   * Set design scheme.
   */
  fun setDesignScheme(scheme: DesignScheme) {
    _designScheme.value = scheme
  }

  /**
   * Abort current streaming response.
   */
  fun abortStreaming() {
    _isStreaming.value = false
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

  /**
   * Get file modifications for AI context.
   */
  fun getModifiedFiles(): Map<String, FileEntry> {
    return fileSystemService.getModifiedFiles()
  }
}
