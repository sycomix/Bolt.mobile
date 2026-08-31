package com.bolt.diy.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
  val id: String = java.util.UUID.randomUUID().toString(),
  val role: Role,
  val content: String,
  val timestamp: Long = System.currentTimeMillis(),
  val model: String? = null
)

enum class Role {
  USER, ASSISTANT, SYSTEM
}

@Serializable
data class ChatSession(
  val id: String = java.util.UUID.randomUUID().toString(),
  val title: String,
  val messages: List<ChatMessage> = emptyList(),
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
  val model: String? = null,
  val contextOptimization: Boolean = true,
  val buildMode: Boolean = false
)

@Serializable
data class FileNode(
  val path: String,
  val name: String,
  val type: FileType,
  val content: String = "",
  val isModified: Boolean = false,
  val lastModified: Long = System.currentTimeMillis(),
  val children: List<FileNode> = emptyList()
)

enum class FileType {
  FILE, DIRECTORY
}

@Serializable
data class LlmProvider(
  val id: String,
  val name: String,
  val apiKey: String,
  val baseUrl: String? = null,
  val enabled: Boolean = true
)

@Serializable
data class Settings(
  val defaultModel: String = "openai/gpt-4o",
  val temperature: Double = 0.7,
  val maxTokens: Int = 4096,
  val theme: ThemeMode = ThemeMode.SYSTEM,
  val providers: List<LlmProvider> = emptyList(),
  val contextOptimization: Boolean = true,
  val autoSaveInterval: Long = 5000
)

enum class ThemeMode {
  LIGHT, DARK, SYSTEM
}
