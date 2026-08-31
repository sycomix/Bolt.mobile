package com.bolt.diy.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Represents a message in the conversation, mirroring the AI SDK Message type.
 */
@Serializable
@Parcelize
data class ChatMessage(
  val id: String,
  val role: Role,
  val content: String,
  val timestamp: Long = System.currentTimeMillis(),
  val annotations: List<Annotation> = emptyList()
) : Parcelable {

  @Serializable
  enum class Role {
    USER, ASSISTANT, SYSTEM
  }

  @Serializable
  data class Annotation(
    val type: String,
    val value: Any? = null
  ) : Parcelable
}

/**
 * Represents a full chat session with messages.
 */
@Serializable
@Parcelize
data class ChatSession(
  val id: String,
  val urlId: String?,
  val description: String?,
  val messages: List<ChatMessage> = emptyList(),
  val timestamp: String = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
    .format(System.currentTimeMillis()),
  val metadata: ChatMetadata? = null
) : Parcelable

/**
 * Metadata for a chat session (git URL, branch, etc.)
 */
@Serializable
@Parcelize
data class ChatMetadata(
  val gitUrl: String? = null,
  val gitBranch: String? = null,
  val netlifySiteId: String? = null
) : Parcelable

/**
 * Represents a file in the WebContainer filesystem.
 */
@Serializable
@Parcelize
data class FileEntry(
  val path: String,
  val content: String,
  val isBinary: Boolean = false,
  val isLocked: Boolean = false,
  val lockedByFolder: String? = null
) : Parcelable

/**
 * Represents a folder in the WebContainer filesystem.
 */
@Serializable
@Parcelize
data class FolderEntry(
  val path: String,
  val isLocked: Boolean = false,
  val lockedByFolder: String? = null
) : Parcelable

/**
 * Snapshot of files for restore functionality.
 */
@Serializable
@Parcelize
data class Snapshot(
  val chatIndex: String,
  val files: Map<String, FileSnapshotData>,
  val summary: String? = null
) : Parcelable {
  @Serializable
  @Parcelize
  data class FileSnapshotData(
    val type: String, // "file" or "folder"
    val content: String?,
    val isBinary: Boolean = false
  ) : Parcelable
}

/**
 * Provider configuration for LLM services.
 */
@Serializable
@Parcelize
data class ProviderConfig(
  val name: String,
  val apiKey: String = "",
  val baseUrl: String? = null,
  val enabled: Boolean = false,
  val models: List<String> = emptyList()
) : Parcelable

/**
 * Streaming progress indicator.
 */
@Serializable
@Parcelize
data class ProgressAnnotation(
  val type: String,
  val label: String,
  val status: String, // "in-progress" or "complete"
  val order: Int = 0,
  val message: String = ""
) : Parcelable

/**
 * Token usage statistics.
 */
@Serializable
@Parcelize
data class TokenUsage(
  val completionTokens: Int = 0,
  val promptTokens: Int = 0,
  val totalTokens: Int = 0
) : Parcelable
