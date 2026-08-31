package com.bolt.diy.domain.service

import com.bolt.diy.domain.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * Service for interacting with LLM providers via API.
 * Mirrors the streamText/stream-text functionality from the web app.
 */
class LlmService(private val httpClient: HttpClient) {

  companion object {
    private const val MAX_TOKENS = 1024
    private const val MAX_RESPONSE_SEGMENTS = 5
    private const val API_CHAT_ENDPOINT = "/api/chat"
    private const val API_MODELS_ENDPOINT = "/api/models"
    private const val API_PROVIDERS_ENDPOINT = "/api/configured-providers"

    // Default providers matching the web app's PROVIDER_LIST
    val DEFAULT_PROVIDERS = listOf(
      Provider("OpenAI", "OpenAI models (GPT-4, GPT-3.5)", baseUrl = "https://api.openai.com"),
      Provider("Anthropic", "Anthropic Claude models", baseUrl = "https://api.anthropic.com"),
      Provider("Google", "Google Gemini models", baseUrl = "https://generativelanguage.googleapis.com"),
      Provider("Groq", "Fast inference with Llama, Mixtral", baseUrl = "https://api.groq.com/openai/v1"),
      Provider("xAI", "xAI Grok models", baseUrl = "https://api.x.ai/v1"),
      Provider("DeepSeek", "DeepSeek Coder models", baseUrl = "https://api.deepseek.com/v1"),
      Provider("Mistral", "Mistral AI models", baseUrl = "https://api.mistral.ai/v1"),
      Provider("Cohere", "Cohere Command models", baseUrl = "https://api.cohere.ai/v1"),
      Provider("Together", "Together AI models", baseUrl = "https://api.together.xyz/v1"),
      Provider("Perplexity", "Perplexity Sonar models", baseUrl = "https://api.perplexity.ai"),
      Provider("HuggingFace", "HuggingFace Hub models", baseUrl = "https://api-inference.huggingface.co/v1"),
      Provider("OpenRouter", "Unified API for multiple providers", baseUrl = "https://openrouter.ai/api/v1"),
      Provider("Ollama", "Local Ollama models", baseUrl = "http://localhost:11434", isLocalProvider = true),
      Provider("LMStudio", "Local LM Studio models", baseUrl = "http://localhost:1234", isLocalProvider = true),
      Provider("OpenAILike", "Any OpenAI-compatible API", baseUrl = null, isLocalProvider = true)
    )

    // Default models per provider
    private val DEFAULT_MODELS = mapOf(
      "OpenAI" to listOf(
        ModelInfo("gpt-4o", "GPT-4o", 128000, 4096),
        ModelInfo("gpt-4o-mini", "GPT-4o Mini", 128000, 4096),
        ModelInfo("gpt-3.5-turbo", "GPT-3.5 Turbo", 16385, 4096)
      ),
      "Anthropic" to listOf(
        ModelInfo("claude-sonnet-4-20250514", "Claude Sonnet 4", 200000, 8192),
        ModelInfo("claude-opus-4-20250514", "Claude Opus 4", 200000, 8192),
        ModelInfo("claude-3-5-sonnet-latest", "Claude 3.5 Sonnet", 200000, 8192)
      ),
      "Google" to listOf(
        ModelInfo("gemini-2.0-flash-exp", "Gemini 2.0 Flash", 1048576, 8192),
        ModelInfo("gemini-pro", "Gemini Pro", 32768, 8192)
      ),
      "Groq" to listOf(
        ModelInfo("llama-3.1-70b", "Llama 3.1 70B", 128000, 8192),
        ModelInfo("mixtral-8x7b", "Mixtral 8x7B", 32768, 8192)
      ),
      "Ollama" to listOf(
        ModelInfo("llama3", "Llama 3", 8192, 4096),
        ModelInfo("mistral", "Mistral", 8192, 4096)
      )
    )

    fun getModelsForProvider(providerName: String): List<ModelInfo> {
      return DEFAULT_MODELS[providerName] ?: emptyList()
    }
  }

  /**
   * Get available models for a provider.
   */
  suspend fun getModels(providerName: String, apiKey: String? = null): Result<List<ModelInfo>> {
    return try {
      val baseUrl = DEFAULT_PROVIDERS.find { it.name == providerName }?.baseUrl
        ?: throw IllegalArgumentException("Unknown provider: $providerName")

      val url = when (providerName) {
        "OpenAI" -> "$baseUrl/v1/models"
        "Anthropic" -> "$baseUrl/v1/messages/models"
        "Google" -> "$baseUrl/v1beta/models"
        "Groq" -> "$baseUrl/v1/models"
        "xAI" -> "$baseUrl/v1/models"
        "DeepSeek" -> "$baseUrl/v1/models"
        "Mistral" -> "$baseUrl/v1/models"
        "Cohere" -> "$baseUrl/v1/chat/models"
        "Together" -> "$baseUrl/v1/models"
        "Perplexity" -> "$baseUrl/v1/models"
        "HuggingFace" -> "$baseUrl/v1/models"
        "OpenRouter" -> "$baseUrl/api/v1/models"
        else -> emptyList<ModelInfo>()
      }

      val response = httpClient.get(url) {
        apiKey?.let { header("Authorization", "Bearer $it") }
        providerName == "Anthropic" && header("x-api-key", apiKey ?: "")
        providerName == "Anthropic" && header("anthropic-version", "2023-06-01")
      }

      if (response.status != HttpStatusCode.OK) {
        return Result.failure(Exception("Failed to fetch models: ${response.status}"))
      }

      // Parse response based on provider format
      val models = parseModelsResponse(providerName, response.bodyAsText())
      Result.success(models)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Stream a chat completion from the API.
   * This mirrors the web app's streaming endpoint behavior.
   */
  fun streamChatCompletion(
    providerName: String,
    apiKey: String,
    baseUrl: String?,
    messages: List<com.bolt.diy.data.model.ChatMessage>,
    modelId: String,
    files: Map<String, com.bolt.diy.data.model.FileEntry> = emptyMap(),
    contextOptimization: Boolean = true,
    chatMode: com.bolt.diy.domain.model.ChatMode = com.bolt.diy.domain.model.ChatMode.BUILD,
    designScheme: DesignScheme? = null,
    maxLLMSteps: Int = 10,
    onChunk: (String) -> Unit,
    onComplete: () -> Unit,
    onError: (String) -> Unit
  ): Job {
    return CoroutineScope(Dispatchers.IO).launch {
      try {
        val providerBaseUrl = baseUrl ?: DEFAULT_PROVIDERS.find { it.name == providerName }?.baseUrl
        ?: throw IllegalArgumentException("Provider $providerName not configured")

        // Build the request payload matching the web app's API format
        val requestBody = buildJsonObject {
          put("messages", Json.encodeToJsonElement(messages))
          if (files.isNotEmpty()) {
            put("files", Json.encodeToJsonElement(files.mapValues { (_, file) ->
              buildJsonObject {
                put("content", file.content)
                put("isBinary", file.isBinary)
                put("isLocked", file.isLocked)
              }
            }))
          }
          put("providerName", providerName)
          put("modelId", modelId)
          put("contextOptimization", contextOptimization)
          put("chatMode", chatMode.name.lowercase())
          designScheme?.let { scheme ->
            putJsonObject("designScheme") {
              put("primaryColor", scheme.primaryColor)
              put("cornerRadius", scheme.cornerRadius)
              put("fontScale", scheme.fontScale)
            }
          }
          put("maxLLMSteps", maxLLMSteps)
        }

        // Make streaming request based on provider type
        when (providerName.lowercase()) {
          "openai" -> streamOpenAI(providerBaseUrl, apiKey, messages, modelId, onChunk, onComplete, onError)
          "anthropic" -> streamAnthropic(providerBaseUrl, apiKey, messages, modelId, onChunk, onComplete, onError)
          "google" -> streamGoogle(providerBaseUrl, apiKey, messages, modelId, onChunk, onComplete, onError)
          else -> streamGeneric(providerBaseUrl, providerName, apiKey, messages, modelId, onChunk, onComplete, onError)
        }

      } catch (e: Exception) {
        onError("Error: ${e.message ?: "Unknown error"}")
      }
    }
  }

  /**
   * Stream completion using OpenAI-compatible API format.
   */
  private suspend fun streamOpenAI(
    baseUrl: String,
    apiKey: String,
    messages: List<com.bolt.diy.data.model.ChatMessage>,
    modelId: String,
    onChunk: (String) -> Unit,
    onComplete: () -> Unit,
    onError: (String) -> Unit
  ) {
    try {
      val requestBody = buildJsonObject {
        put("model", modelId)
        put("messages", Json.encodeToJsonElement(messages.map { msg ->
          buildJsonObject {
            put("role", msg.role.name.lowercase())
            put("content", msg.content)
          }
        }))
        put("stream", true)
        put("temperature", 0.7)
        put("max_tokens", MAX_TOKENS)
      }

      val response = httpClient.post("$baseUrl/v1/chat/completions") {
        header("Authorization", "Bearer $apiKey")
        header("Content-Type", "application/json")
        setBody(requestBody.toString())
      }

      if (response.status != HttpStatusCode.OK) {
        onError("API Error: ${response.status}")
        return
      }

      // Parse SSE stream
      val responseBody = response.bodyAsText()
      parseSSEStream(responseBody, onChunk, onComplete, onError)

    } catch (e: Exception) {
      onError("OpenAI Stream Error: ${e.message}")
    }
  }

  /**
   * Stream completion using Anthropic API format.
   */
  private suspend fun streamAnthropic(
    baseUrl: String,
    apiKey: String,
    messages: List<com.bolt.diy.data.model.ChatMessage>,
    modelId: String,
    onChunk: (String) -> Unit,
    onComplete: () -> Unit,
    onError: (String) -> Unit
  ) {
    try {
      // Convert chat messages to Anthropic message format
      val anthropicMessages = messages.filter { it.role != com.bolt.diy.data.model.ChatMessage.Role.SYSTEM }
        .map { msg ->
          buildJsonObject {
            put("role", if (msg.role == com.bolt.diy.data.model.ChatMessage.Role.USER) "user" else "assistant")
            put("content", msg.content)
          }
        }

      val systemMessage = messages.firstOrNull { it.role == com.bolt.diy.data.model.ChatMessage.Role.SYSTEM }?.content

      val requestBody = buildJsonObject {
        put("model", modelId)
        put("max_tokens", MAX_TOKENS.toLong())
        if (systemMessage != null) put("system", systemMessage)
        put("messages", Json.encodeToJsonElement(anthropicMessages))
        put("stream", true)
      }

      val response = httpClient.post("$baseUrl/v1/messages") {
        header("x-api-key", apiKey)
        header("anthropic-version", "2023-06-01")
        header("Content-Type", "application/json")
        setBody(requestBody.toString())
      }

      if (response.status != HttpStatusCode.OK) {
        onError("Anthropic API Error: ${response.status}")
        return
      }

      val responseBody = response.bodyAsText()
      parseSSEStream(responseBody, onChunk, onComplete, onError)

    } catch (e: Exception) {
      onError("Anthropic Stream Error: ${e.message}")
    }
  }

  /**
   * Stream completion using Google Gemini API format.
   */
  private suspend fun streamGoogle(
    baseUrl: String,
    apiKey: String,
    messages: List<com.bolt.diy.data.model.ChatMessage>,
    modelId: String,
    onChunk: (String) -> Unit,
    onComplete: () -> Unit,
    onError: (String) -> Unit
  ) {
    try {
      // Convert to Gemini format - use last user message as content
      val lastUserMessage = messages.lastOrNull { it.role == com.bolt.diy.data.model.ChatMessage.Role.USER }

      if (lastUserMessage == null) {
        onError("No user message found")
        return
      }

      val requestBody = buildJsonObject {
        put(
          "contents", Json.encodeToJsonElement(
          listOf(
          buildJsonObject {
            put("role", "user")
            put(
              "parts", Json.encodeToJsonElement(
              listOf(
              buildJsonObject { put("text", lastUserMessage.content) }
            )))
          }
        )))
        put("generationConfig", buildJsonObject {
          put("temperature", 0.7f)
          put("maxOutputTokens", MAX_TOKENS)
        })
      }

      val response = httpClient.post("$baseUrl/v1beta/models/$modelId:streamGenerateContent?key=$apiKey") {
        header("Content-Type", "application/json")
        setBody(requestBody.toString())
      }

      if (response.status != HttpStatusCode.OK) {
        onError("Google API Error: ${response.status}")
        return
      }

      val responseBody = response.bodyAsText()
      parseSSEStream(responseBody, onChunk, onComplete, onError)

    } catch (e: Exception) {
      onError("Google Stream Error: ${e.message}")
    }
  }

  /**
   * Generic streaming for other providers.
   */
  private suspend fun streamGeneric(
    baseUrl: String,
    providerName: String,
    apiKey: String,
    messages: List<com.bolt.diy.data.model.ChatMessage>,
    modelId: String,
    onChunk: (String) -> Unit,
    onComplete: () -> Unit,
    onError: (String) -> Unit
  ) {
    try {
      val requestBody = buildJsonObject {
        put("model", modelId)
        put("messages", Json.encodeToJsonElement(messages.map { msg ->
          buildJsonObject {
            put("role", msg.role.name.lowercase())
            put("content", msg.content)
          }
        }))
        put("stream", true)
      }

      val url = if (baseUrl.endsWith("/")) "$baseUrl/v1/chat/completions" else "$baseUrl/v1/chat/completions"

      val response = httpClient.post(url) {
        header("Authorization", "Bearer $apiKey")
        header("Content-Type", "application/json")
        setBody(requestBody.toString())
      }

      if (response.status != HttpStatusCode.OK) {
        onError("$providerName API Error: ${response.status}")
        return
      }

      val responseBody = response.bodyAsText()
      parseSSEStream(responseBody, onChunk, onComplete, onError)

    } catch (e: Exception) {
      onError("$providerName Stream Error: ${e.message}")
    }
  }

  /**
   * Parse Server-Sent Events stream into chunks.
   */
  private fun parseSSEStream(
    sseData: String,
    onChunk: (String) -> Unit,
    onComplete: () -> Unit,
    onError: (String) -> Unit
  ) {
    try {
      val lines = sseData.split("\n")

      for (line in lines) {
        if (line.startsWith("data: ")) {
          val data = line.substringAfter("data: ").trim()

          // Skip [DONE] end marker
          if (data == "[DONE]") {
            onComplete()
            return
          }

          // Parse JSON data event
          if (data.startsWith("{")) {
            try {
              val jsonElement = Json.parseToJsonElement(data)
              val choice = jsonElement.jsonObject["choices"]?.jsonArray?.get(0)
              val delta = choice?.jsonObject?.get("delta")?.jsonObject
              val content = delta?.get("content")?.let { it as? String }

              if (!content.isNullOrBlank()) {
                onChunk(content)
              }
            } catch (e: Exception) {
              // Skip malformed JSON chunks
            }
          }
        }
      }
      onComplete()
    } catch (e: Exception) {
      onError("Stream parse error: ${e.message}")
    }
  }

  /**
   * Parse models response based on provider format.
   */
  private fun parseModelsResponse(providerName: String, responseBody: String): List<ModelInfo> {
    return try {
      val json = Json.parseToJsonElement(responseBody)

      when (providerName.lowercase()) {
        "openai" -> {
          // OpenAI returns { data: [{ id: "...", ... }] }
          val data = json.jsonObject["data"]?.jsonArray ?: return emptyList()
          data.mapNotNull { item ->
            val obj = item.jsonObject
            val id = obj["id"]?.let { it.asString } ?: return@mapNotNull null
            ModelInfo(id, id)
          }
        }

        "anthropic" -> {
          // Anthropic returns { models: [{ model_id: "...", ... }] }
          val models = json.jsonObject["models"]?.jsonArray ?: return emptyList()
          models.mapNotNull { item ->
            val obj = item.jsonObject
            val id = obj["model_id"]?.let { it.asString } ?: return@mapNotNull null
            ModelInfo(id, id)
          }
        }

        "google" -> {
          // Google returns { models: [{ name: "models/...", ... }] }
          val models = json.jsonObject["models"]?.jsonArray ?: return emptyList()
          models.mapNotNull { item ->
            val obj = item.jsonObject
            val name = obj["name"]?.let { it.asString } ?: return@mapNotNull null
            val id = name.removePrefix("models/")
            ModelInfo(id, id)
          }
        }

        else -> DEFAULT_MODELS[providerName] ?: emptyList()
      }
    } catch (e: Exception) {
      // Fall back to default models
      DEFAULT_MODELS[providerName]?.toList() ?: emptyList()
    }
  }

  /**
   * Get configured providers list.
   */
  suspend fun getConfiguredProviders(): List<Provider> {
    return try {
      val response = httpClient.get(API_PROVIDERS_ENDPOINT)
      if (response.status != HttpStatusCode.OK) return DEFAULT_PROVIDERS

      val json = Json.parseToJsonElement(response.bodyAsText())
      val providers = json.jsonObject["providers"]?.jsonArray ?: return DEFAULT_PROVIDERS

      providers.mapNotNull { item ->
        val obj = item.jsonObject
        val name = obj["name"]?.let { it.asString } ?: return@mapNotNull null
        val isConfigured = obj["isConfigured"]?.booleanOrNull ?: false
        val configMethod = obj["configMethod"]?.let { it.asString }

        DEFAULT_PROVIDERS.find { it.name == name }?.copy(
          enabled = isConfigured && configMethod == "environment",
          baseUrl = baseUrl ?: DEFAULT_PROVIDERS.find { it.name == name }?.baseUrl
        )
      }
    } catch (e: Exception) {
      DEFAULT_PROVIDERS
    }
  }
}
