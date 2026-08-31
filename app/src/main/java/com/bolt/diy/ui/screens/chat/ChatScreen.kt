package com.bolt.diy.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bolt.diy.data.model.ChatMessage
import com.bolt.diy.domain.model.ChatMode

/**
 * Main chat screen - mirrors BaseChat.tsx from the web app.
 * Handles new chat creation, message input, and conversation display.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
  viewModel: ChatViewModel = viewModel(),
  chatId: String? = null,
  onNavigateToChat: (String) -> Unit = {},
  onNavigateBack: () -> Unit = {},
  onNavigateToSettings: () -> Unit = {}
) {
  val messages by viewModel.chatMessages.collectAsState()
  val isStreaming by viewModel.isStreaming.collectAsState()

  // Load existing chat if chatId provided
  LaunchedEffect(chatId) {
    if (chatId != null) {
      viewModel.createNewChat()
    }
  }

  Column(modifier = Modifier.fillMaxSize()) {
    // Top app bar
    TopAppBar(
      onNewChat = { viewModel.createNewChat() },
      onSettingsClick = onNavigateToSettings,
      onBackClick = if (chatId != null) onNavigateBack else null
    )

    // Messages area
    ChatMessageList(
      messages = messages,
      isStreaming = isStreaming,
      modifier = Modifier.weight(1f)
    )

    // Input area
    ChatInputArea(
      onSend = { content ->
        viewModel.sendMessage(content)
      },
      isStreaming = isStreaming
    )
  }
}

@Composable
private fun TopAppBar(
  onNewChat: () -> Unit,
  onSettingsClick: () -> Unit,
  onBackClick: (() -> Unit)? = null
) {
  TopAppBar(
    title = { Text("bolt.diy") },
    navigationIcon = {
      if (onBackClick != null) {
        IconButton(onClick = onBackClick) {
          Icon(Icons.Default.ArrowBack, contentDescription = "Back")
        }
      } else {
        IconButton(onClick = onNewChat) {
          Icon(Icons.Default.Add, contentDescription = "New Chat")
        }
      }
    },
    actions = {
      // Chat mode selector
      var chatMode by remember { mutableStateOf(ChatMode.BUILD) }
      FilterChip(
        selected = chatMode == ChatMode.BUILD,
        onClick = { chatMode = if (chatMode == ChatMode.BUILD) ChatMode.DISCUSS else ChatMode.BUILD },
        label = { Text(if (chatMode == ChatMode.BUILD) "Build" else "Discuss") }
      )

      IconButton(onClick = onSettingsClick) {
        Icon(Icons.Default.Settings, contentDescription = "Settings")
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.primaryContainer
    )
  )
}

@Composable
private fun ChatMessageList(
  messages: List<ChatMessage>,
  isStreaming: Boolean,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    modifier = modifier.fillMaxSize(),
    reverseLayout = true,
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    items(messages.size) { index ->
      val message = messages[messages.size - 1 - index]
      ChatMessageBubble(
        message = message,
        isLast = index == 0
      )
    }

    // Streaming indicator
    if (isStreaming && messages.lastOrNull()?.role != ChatMessage.Role.ASSISTANT) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
          )
        ) {
          Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Text("AI is thinking...")
          }
        }
      }
    }
  }
}

@Composable
private fun ChatMessageBubble(
  message: ChatMessage,
  isLast: Boolean = false
) {
  val isUser = message.role == ChatMessage.Role.USER
  val isSystem = message.role == ChatMessage.Role.SYSTEM

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    colors = CardDefaults.cardColors(
      containerColor = when {
        isUser -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        isSystem -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
      }
    )
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Role label
      Text(
        text = when {
          isUser -> "You"
          isSystem -> "System"
          else -> "Assistant"
        },
        style = MaterialTheme.typography.labelSmall,
        color = when {
          isUser -> MaterialTheme.colorScheme.primary
          isSystem -> MaterialTheme.colorScheme.secondary
          else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        }
      )

      Spacer(Modifier.height(8.dp))

      // Content with basic markdown rendering
      if (message.content.isBlank()) {
        Text(
          text = "No content",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
      } else {
        // Handle code blocks
        if (message.content.contains("```")) {
          renderCodeBlocks(message.content)
        } else {
          Text(
            text = message.content,
            style = MaterialTheme.typography.bodyMedium
          )
        }
      }

      // Timestamp
      Text(
        text = formatTimestamp(message.timestamp),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        modifier = Modifier.padding(top = 8.dp)
      )
    }
  }
}

@Composable
private fun renderCodeBlocks(content: String) {
  val lines = content.split("\n")
  var inCodeBlock = false
  var codeContent = ""
  var codeLang = ""

  for (line in lines) {
    if (line.startsWith("```")) {
      if (inCodeBlock) {
        // End of code block - render it
        CodeBlockDisplay(code = codeContent, language = codeLang)
        codeContent = ""
        codeLang = ""
        inCodeBlock = false
      } else {
        inCodeBlock = true
        codeLang = line.substring(3).trim()
      }
    } else if (inCodeBlock) {
      codeContent += line + "\n"
    } else {
      Text(
        text = line,
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }

  // Handle unclosed code block
  if (inCodeBlock && codeContent.isNotBlank()) {
    CodeBlockDisplay(code = codeContent.trim(), language = codeLang)
  }
}

@Composable
private fun CodeBlockDisplay(code: String, language: String) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 8.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    )
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      if (language.isNotBlank()) {
        Text(
          text = language,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary
        )
      }
      Spacer(Modifier.height(4.dp))
      Text(
        text = code,
        style = MaterialTheme.typography.bodyMedium.copy(
          fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
        ),
        modifier = Modifier.fillMaxWidth()
      )
    }
  }
}

private fun formatTimestamp(timestamp: Long): String {
  val date = java.util.Date(timestamp)
  return java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(date)
}

@Composable
private fun ChatInputArea(
  onSend: (String) -> Unit,
  isStreaming: Boolean
) {
  var text by remember { mutableStateOf("") }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Text input
      OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        modifier = Modifier.weight(1f),
        placeholder = { Text("Send a message...") },
        maxLines = 4,
        shape = MaterialTheme.shapes.large,
        enabled = !isStreaming
      )

      // Send button
      FloatingActionButton(
        onClick = {
          if (text.isNotBlank()) {
            onSend(text)
            text = ""
          }
        },
        modifier = Modifier.padding(start = 8.dp),
        enabled = text.isNotBlank() && !isStreaming,
        containerColor = MaterialTheme.colorScheme.primary
      ) {
        if (isStreaming) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimary
          )
        } else {
          Icon(Icons.Default.Send, contentDescription = "Send")
        }
      }

      // Stop button when streaming
      if (isStreaming) {
        IconButton(onClick = {}) {
          Icon(Icons.Default.Stop, contentDescription = "Stop", tint = MaterialTheme.colorScheme.error)
        }
      }
    }
  }
}
