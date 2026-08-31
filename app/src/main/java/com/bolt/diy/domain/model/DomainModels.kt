package com.bolt.diy.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Represents an LLM provider configuration.
 * Mirrors IProviderConfig from the web app.
 */
@Serializable
@Parcelize
data class Provider(
  val name: String,
  val description: String,
  val iconResId: Int = 0,
  val baseUrl: String? = null,
  val apiKey: String = "",
  val enabled: Boolean = false,
  val models: List<ModelInfo> = emptyList(),
  val isLocalProvider: Boolean = false
) : Parcelable

/**
 * Information about an available model.
 */
@Serializable
@Parcelize
data class ModelInfo(
  val id: String,
  val name: String,
  val contextLength: Int = 4096,
  maxOutputTokens: Int = 8192,
  attachmentLimitBytes: Long = 25 * 1024 * 1024,
  supportedMimeTypes: List<String> = emptyList(),
  type: String = "chat"
) : Parcelable

/**
 * Provider categories matching the web app.
 */
enum class ProviderCategory {
  CLOUD, LOCAL
}

/**
 * Chat mode mirroring the web app's build/discuss modes.
 */
enum class ChatMode {
  BUILD, DISCUSS
}

/**
 * Design scheme for UI customization.
 */
@Serializable
@Parcelize
data class DesignScheme(
  val primaryColor: String = "#0284c7",
  val cornerRadius: Float = 16f,
  val fontScale: Float = 1f
) : Parcelable

/**
 * Progress indicator for streaming responses.
 */
@Serializable
@Parcelize
data class ProgressIndicator(
  val label: String,
  val status: Status,
  val message: String,
  val order: Int = 0
) {
  enum class Status { IN_PROGRESS, COMPLETE }
}

/**
 * File context for AI to understand project structure.
 */
@Serializable
@Parcelize
data class FileContext(
  val path: String,
  val content: String,
  val language: String? = null
) : Parcelable

/**
 * Token usage tracking.
 */
@Serializable
@Parcelize
data class UsageStats(
  val completionTokens: Int = 0,
  val promptTokens: Int = 0,
  val totalTokens: Int = 0
) : Parcelable {
  fun total(): Int = completionTokens + promptTokens + totalTokens
}
