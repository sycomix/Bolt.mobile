package com.bolt.diy.data.remote

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object BoltHttpClient {
  private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
  }

  val client = HttpClient {
    install(ContentNegotiation) {
      json(json)
    }
    install(HttpTimeout) {
      connectTimeoutMillis = 10000
      socketTimeoutMillis = 60000
    }
    install(HttpRequestRetry) {
      maxRetries = 3
      retryOnExceptionOrServerError()
    }
  }

  suspend fun streamChatCompletion(
    providerUrl: String,
    apiKey: String,
    model: String,
    messages: List<Map<String, String>>,
    temperature: Double,
    maxTokens: Int,
    onChunk: (String) -> Unit,
    onComplete: () -> Unit,
    onError: (Exception) -> Unit
  ) {
    try {
      val response = client.post(providerUrl) {
        url {
          appendPathSegments("/chat/completions")
        }
        header(HttpHeaders.Authorization, "Bearer $apiKey")
        header(HttpHeaders.ContentType, ContentType.Application.Json)
        setBody(
          mapOf(
            "model" to model,
            "messages" to messages,
            "temperature" to temperature,
            "max_tokens" to maxTokens,
            "stream" to true
          )
        )
      }

      response.bodyAsTextStream { chunk ->
        val content = parseSseChunk(chunk)
        if (content != null) {
          onChunk(content)
        }
      }
      onComplete()
    } catch (e: Exception) {
      onError(e)
    }
  }

  private suspend fun HttpResponse.bodyAsTextStream(onChunk: (String) -> Unit) {
    val content = bodyAsText()
    content.lines().forEach { line ->
      if (line.isNotBlank()) {
        onChunk(line)
      }
    }
  }

  private fun parseSseChunk(chunk: String): String? {
    if (chunk.startsWith("data: ")) {
      val data = chunk.substringAfter("data: ").trim()
      if (data == "[DONE]") return null
      try {
        val json = Json.parseToJsonElement(data)
        val choices = json.jsonObject["choices"]?.jsonArray
        return choices?.get(0)?.jsonObject?.get("delta")?.jsonObject?.get("content")?.let {
          it.jsonPrimitive.content
        }
      } catch (e: Exception) {
        return null
      }
    }
    return null
  }

  private fun URLBuilder.appendPathSegments(vararg segments: String) {
    segments.forEach { segment ->
      pathSegment(segment)
    }
  }
}
