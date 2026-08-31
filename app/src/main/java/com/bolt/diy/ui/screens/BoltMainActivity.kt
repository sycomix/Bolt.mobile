package com.bolt.diy.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bolt.diy.data.model.ChatMessage
import com.bolt.diy.data.model.FileEntry
import com.bolt.diy.domain.model.ChatMode

/**
 * Main screen for bolt.diy Android app.
 * Mirrors the web app's root layout with sidebar, chat, editor, and terminal panels.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoltMainActivity() {
  val viewModel: ChatViewModel = viewModel()
  val editorViewModel: EditorViewModel = viewModel()

  var selectedScreen by remember { mutableStateOf("chat") }
  var showSettings by remember { mutableStateOf(false) }
  var showTerminal by remember { mutableStateOf(false) }
  var showSidebar by remember { mutableStateOf(true) }
  var showEditorPanel by remember { mutableStateOf(false) }

  androidx.compose.material3.Scaffold(
    modifier = Modifier.fillMaxSize(),
    containerColor = MaterialTheme.colorScheme.background
  ) { paddingValues ->
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Sidebar - mirrors the web app's sidebar
      if (showSidebar) {
        Card(
          modifier = Modifier
            .width(280.dp)
            .fillMaxHeight(),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f)
          )
        ) {
          SidebarContent(
            onNewChat = { viewModel.createNewChat() },
            selectedScreen = selectedScreen,
            onSelectScreen = { screen ->
              selectedScreen = screen
              showSidebar = false
            },
            onCloseSidebar = { showSidebar = false }
          )
        }

        VerticalDivider(
          modifier = Modifier.width(1.dp),
          color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
      }

      // Main content area
      Column(modifier = Modifier.weight(1f)) {
        when (selectedScreen) {
          "chat" -> ChatMainContent(viewModel = viewModel, onToggleEditor = { showEditorPanel = !showEditorPanel })
          "editor" -> EditorMainContent(editorViewModel = editorViewModel)
          "deploy" -> DeployMainContent()
          else -> ChatMainContent(viewModel = viewModel, onToggleEditor = { showEditorPanel = !showEditorPanel })
        }

        // Terminal panel at bottom
        if (showTerminal) {
          TerminalPanel(onClose = { showTerminal = false })
        }
      }

      // Editor panel - mirrors the web app's editor panel
      if (showEditorPanel) {
        Card(
          modifier = Modifier
            .width(400.dp)
            .fillMaxHeight(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          EditorPanelContent(viewModel = editorViewModel)
        }

        VerticalDivider(
          modifier = Modifier.width(1.dp),
          color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
      }
    }
  }

  // Settings modal
  if (showSettings) {
    SettingsModal(onDismiss = { showSettings = false })
  }
}

@Composable
private fun SidebarContent(
  onNewChat: () -> Unit,
  selectedScreen: String,
  onSelectScreen: (String) -> Unit,
  onCloseSidebar: () -> Unit
) {
  Column(modifier = Modifier.fillMaxSize()) {
    // Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = "bolt.diy",
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.primary
      )

      Spacer(Modifier.weight(1f))

      IconButton(onClick = onCloseSidebar) {
        Icon(Icons.Default.Close, contentDescription = "Close sidebar")
      }
    }

    // New chat button
    Button(
      onClick = onNewChat,
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
      Icon(Icons.Default.Add, contentDescription = null)
      Spacer(Modifier.width(8.dp))
      Text("New Chat")
    }

    // Navigation items
    NavigationItem(
      icon = Icons.Default.Chat,
      label = "Chat",
      isSelected = selectedScreen == "chat",
      onClick = { onSelectScreen("chat") }
    )

    NavigationItem(
      icon = Icons.Default.Code,
      label = "Editor",
      isSelected = selectedScreen == "editor",
      onClick = { onSelectScreen("editor") }
    )

    NavigationItem(
      icon = Icons.Default.Cloud,
      label = "Deploy",
      isSelected = selectedScreen == "deploy",
      onClick = { onSelectScreen("deploy") }
    )

    Spacer(Modifier.weight(1f))

    // Bottom actions
    NavigationItem(
      icon = Icons.Default.Terminal,
      label = "Terminal",
      isSelected = false,
      onClick = {}
    )

    NavigationItem(
      icon = Icons.Default.Settings,
      label = "Settings",
      isSelected = false,
      onClick = { /* open settings */ }
    )
  }
}

@Composable
private fun NavigationItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 4.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
      else
        Color.Transparent
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        icon,
        contentDescription = null,
        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
      )
      Spacer(Modifier.width(12.dp))
      Text(label, style = MaterialTheme.typography.bodyMedium)
    }
  }
}

@Composable
private fun ChatMainContent(viewModel: ChatViewModel, onToggleEditor: () -> Unit) {
  val messages by viewModel.chatMessages.collectAsState()
  val isStreaming by viewModel.isStreaming.collectAsState()

  Column(modifier = Modifier.fillMaxSize()) {
    // Top bar with provider selection
    TopAppBar(
      onToggleTerminal = {},
      onToggleEditor = onToggleEditor
    )

    // Chat messages area
    if (messages.isEmpty()) {
      WelcomeScreen()
    } else {
      ChatMessageList(messages, isStreaming)
    }

    // Input area
    ChatInputArea(
      onSend = { content -> viewModel.sendMessage(content) },
      isStreaming = isStreaming
    )
  }
}

@Composable
private fun WelcomeScreen() {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(32.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(
      text = "bolt.diy",
      style = MaterialTheme.typography.headlineLarge,
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.padding(bottom = 16.dp)
    )

    Text(
      text = "Your AI-powered coding assistant",
      style = MaterialTheme.typography.titleMedium,
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
      modifier = Modifier.padding(bottom = 32.dp)
    )

    // Example prompts
    val examplePrompts = listOf(
      "Build a React todo app",
      "Create a Python web scraper",
      "Design a REST API with Node.js",
      "Make a simple game in JavaScript"
    )

    LazyColumn(modifier = Modifier.fillMaxWidth()) {
      items(examplePrompts.size) { index ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
          Text(
            text = examplePrompts[index],
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun TopAppBar(
  onToggleTerminal: () -> Unit,
  onToggleEditor: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Provider selector
      Text(
        text = "OpenAI",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
      )

      // Action buttons
      Row {
        IconButton(onClick = onToggleTerminal) {
          Icon(Icons.Default.Terminal, contentDescription = "Toggle terminal")
        }
        IconButton(onClick = onToggleEditor) {
          Icon(Icons.Default.Code, contentDescription = "Toggle editor")
        }
      }
    }
  }
}

@Composable
private fun ChatMessageList(messages: List<ChatMessage>, isStreaming: Boolean) {
  LazyColumn(
    modifier = Modifier.weight(1f).fillMaxWidth(),
    reverseLayout = true,
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
  ) {
    items(messages.size) { index ->
      val message = messages[messages.size - 1 - index]
      ChatMessageBubble(message = message)
    }

    if (isStreaming && messages.lastOrNull()?.role != ChatMessage.Role.ASSISTANT) {
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
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
private fun ChatMessageBubble(message: ChatMessage) {
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

      if (message.content.isBlank()) {
        Text(
          "No content",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
      } else {
        if (message.content.contains("```")) {
          renderCodeBlocks(message.content)
        } else {
          Text(message.content, style = MaterialTheme.typography.bodyMedium)
        }
      }

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
      Text(line, style = MaterialTheme.typography.bodyMedium)
    }
  }

  if (inCodeBlock && codeContent.isNotBlank()) {
    CodeBlockDisplay(code = codeContent.trim(), language = codeLang)
  }
}

@Composable
private fun CodeBlockDisplay(code: String, language: String) {
  Card(
    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      if (language.isNotBlank()) {
        Text(language, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
      }
      Spacer(Modifier.height(4.dp))
      Text(
        code,
        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
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
private fun ChatInputArea(onSend: (String) -> Unit, isStreaming: Boolean) {
  var text by remember { mutableStateOf("") }

  Card(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        modifier = Modifier.weight(1f),
        placeholder = { Text("Send a message...") },
        maxLines = 4,
        shape = MaterialTheme.shapes.large,
        enabled = !isStreaming
      )

      FloatingActionButton(
        onClick = {
          if (text.isNotBlank()) {
            onSend(text); text = ""
          }
        },
        modifier = Modifier.padding(start = 8.dp),
        enabled = text.isNotBlank() && !isStreaming,
        containerColor = MaterialTheme.colorScheme.primary
      ) {
        if (isStreaming) {
          CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
        } else {
          Icon(Icons.Default.Send, contentDescription = "Send")
        }
      }

      if (isStreaming) {
        IconButton(onClick = {}) {
          Icon(Icons.Default.Stop, contentDescription = "Stop", tint = MaterialTheme.colorScheme.error)
        }
      }
    }
  }
}

@Composable
private fun EditorMainContent(editorViewModel: EditorViewModel) {
  val files by editorViewModel.files.collectAsState()
  val selectedFile by editorViewModel.selectedFilePath.collectAsState()
  val fileContent by editorViewModel.fileContent.collectAsState()

  Column(modifier = Modifier.fillMaxSize()) {
    // File tree sidebar
    Card(
      modifier = Modifier.width(200.dp).fillMaxHeight(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
    ) {
      Column(modifier = Modifier.padding(8.dp)) {
        Text("Files", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(8.dp))

        LazyColumn {
          files.entries.filterValues { it is FileEntry }.forEach { (path, entry) ->
            if (entry is FileEntry) {
              Card(
                modifier = Modifier.fillMaxWidth().clickable { editorViewModel.selectFile(path) }
                  .padding(vertical = 2.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (selectedFile == path) MaterialTheme.colorScheme.primaryContainer.copy(
                    alpha = 0.3f
                  ) else Color.Transparent
                )
              ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                  Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(20.dp))
                  Spacer(Modifier.width(8.dp))
                  Text(path.substringAfterLast('/'), style = MaterialTheme.typography.bodySmall)
                }
              }
            }
          }
        }
      }
    }

    // Code editor area
    if (selectedFile != null) {
      Card(
        modifier = Modifier.weight(1f).fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Column(modifier = Modifier.fillMaxSize()) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(selectedFile!!.substringAfterLast('/'), style = MaterialTheme.typography.bodyMedium)
          }

          Row(modifier = Modifier.fillMaxSize()) {
            Column(
              modifier = Modifier.width(48.dp).fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)).padding(vertical = 8.dp),
              horizontalAlignment = Alignment.End
            ) {
              val lineCount = fileContent.lines().size.coerceAtMost(1000)
              for (i in 1..lineCount) {
                Text(
                  text = i.toString(),
                  style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                  modifier = Modifier.padding(end = 8.dp)
                )
              }
            }

            OutlinedTextField(
              value = fileContent,
              onValueChange = { editorViewModel.saveFile() },
              modifier = Modifier.weight(1f).fillMaxSize(),
              textStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                lineHeight = 24.sp
              ),
              minLines = 20,
              maxLines = Int.MAX_VALUE,
              backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
          }
        }
      }
    } else {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Select a file to edit")
      }
    }
  }
}

@Composable
private fun EditorPanelContent(viewModel: EditorViewModel) {
  Column(modifier = Modifier.fillMaxSize()) {
    Text("Editor Panel", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))
  }
}

@Composable
private fun DeployMainContent() {
  Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
    Text("Deployments", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(bottom = 8.dp))

    DeployOptionCard(title = "Netlify", description = "Deploy to Netlify for fast hosting.", icon = Icons.Default.Cloud)
    DeployOptionCard(title = "Vercel", description = "Deploy to Vercel for edge hosting.", icon = Icons.Default.Speed)
    DeployOptionCard(
      title = "GitHub",
      description = "Connect to GitHub for repository deployment.",
      icon = Icons.Default.Code
    )
    DeployOptionCard(
      title = "GitLab",
      description = "Connect to GitLab for repository deployment.",
      icon = Icons.Default.Code
    )
    DeployOptionCard(
      title = "Supabase",
      description = "Connect to Supabase for database and auth.",
      icon = Icons.Default.Database
    )
  }
}

@Composable
private fun DeployOptionCard(
  title: String,
  description: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(title, style = MaterialTheme.typography.titleMedium)
          Text(description, style = MaterialTheme.typography.bodySmall)
        }
      }
      Button(onClick = {}, modifier = Modifier.align(Alignment.End).padding(top = 8.dp)) { Text("Deploy") }
    }
  }
}

@Composable
private fun TerminalPanel(onClose: () -> Unit) {
  Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))) {
    Column(modifier = Modifier.fillMaxSize()) {
      Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF2D2D2D)).padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Terminal", style = MaterialTheme.typography.titleSmall, color = Color.White)
        IconButton(onClick = onClose) {
          Icon(
            Icons.Default.Close,
            contentDescription = "Close terminal",
            tint = Color.White
          )
        }
      }

      Column(modifier = Modifier.fillMaxSize()) {
        Text(
          "bolt@android:~$ ",
          color = Color.Green,
          fontFamily = FontFamily.Monospace,
          style = MaterialTheme.typography.bodyMedium
        )
      }
    }
  }
}

@Composable
private fun SettingsModal(onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Settings") },
    text = { Column { Text("Provider settings would go here") } },
    confirmButton = {},
    dismissButton = { TextButton(onClick = onDismiss) { Text("Close") } }
  )
}
