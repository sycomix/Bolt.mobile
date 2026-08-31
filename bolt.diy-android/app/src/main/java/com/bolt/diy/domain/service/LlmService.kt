package com.bolt.diy.domain.service

import com.bolt.diy.data.model.ChatMessage
import com.bolt.diy.data.model.Role
import com.bolt.diy.data.remote.BoltHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LlmService {
  private val providers = mapOf(
    "openai" to ProviderConfig("https://api.openai.com", "GPT-4o", listOf("gpt-4o", "gpt-4o-mini", "gpt-3.5-turbo")),
    "anthropic" to ProviderConfig(
      "https://api.anthropic.com",
      "Claude",
      listOf("claude-sonnet-4-20250514", "claude-opus-4-20250514", "claude-haiku-3-5-20241022")
    ),
    "google" to ProviderConfig(
      "https://generativelanguage.googleapis.com/v1beta/models",
      "Gemini",
      listOf("gemini-pro", "gemini-pro-vision", "gemini-2.0-flash-exp")
    ),
    "groq" to ProviderConfig(
      "https://api.groq.com/openai/v1",
      "Groq",
      listOf("llama3-8b-8192", "llama3-70b-8192", "mixtral-8x7b-32768")
    ),
    "xai" to ProviderConfig("https://api.x.ai/v1", "Grok", listOf("grok-beta")),
    "deepseek" to ProviderConfig("https://api.deepseek.com/v1", "DeepSeek", listOf("deepseek-chat", "deepseek-coder")),
    "ollama" to ProviderConfig("http://localhost:11434", "Ollama", listOf("llama2", "mistral", "mixtral")),
    "lmstudio" to ProviderConfig("http://localhost:1234/v1", "LM Studio", listOf("default"))
  )

  data class ProviderConfig(val baseUrl: String, val displayName: String, val models: List<String>)

  fun getProviders(): Map<String, ProviderConfig> = providers

  suspend fun streamCompletion(
    providerId: String,
    apiKey: String,
    model: String,
    messages: List<ChatMessage>,
    temperature: Double = 0.7,
    maxTokens: Int = 4096,
    onChunk: (String) -> Unit,
    onComplete: () -> Unit,
    onError: (Exception) -> Unit
  ) {
    val provider = providers[providerId] ?: run {
      onError(IllegalArgumentException("Unknown provider: $providerId"))
      return
    }

    val formattedMessages = messages.map { msg ->
      mapOf(
        "role" to msg.role.name.lowercase(),
        "content" to msg.content
      )
    }

    BoltHttpClient.client.post(provider.baseUrl) {
      url { appendPathSegments(model, "chat", "completions") }
      header("Authorization", "Bearer $apiKey")
      header("Content-Type", "application/json")
      setBody(
        mapOf(
          "model" to model,
          "messages" to formattedMessages,
          "temperature" to temperature,
          "max_tokens" to maxTokens,
          "stream" to true
        )
      )
    }.bodyAsTextStream { chunk ->
      val content = parseSseChunk(chunk)
      if (content != null) onChunk(content)
    }
    onComplete()
  }

  private suspend fun io.ktor.client.statement.HttpResponse.bodyAsTextStream(onChunk: (String) -> Unit) {
    withContext(Dispatchers.IO) {
      val text = bodyAsText()
      text.lines().filter { it.isNotBlank() }.forEach { line ->
        onChunk(line)
      }
    }
  }

  private fun parseSseChunk(chunk: String): String? {
    if (!chunk.startsWith("data: ")) return null
    val data = chunk.substringAfter("data: ").trim()
    if (data == "[DONE]") return null
    return try {
      kotlinx.serialization.json.Json.parseToJsonElement(data)
        .jsonObject["choices"]?.jsonArray?.get(0)?.jsonObject
        ?.get("delta")?.jsonObject?.get("content")
        ?.jsonPrimitive?.content
    } catch (e: Exception) {
      null
    }
  }
}
