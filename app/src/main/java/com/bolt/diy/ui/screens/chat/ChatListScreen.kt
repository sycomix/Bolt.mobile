package com.bolt.diy.ui.screens.chat

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
import androidx.compose.ui.unit.dp
import com.bolt.diy.data.model.ChatSession

/**
 * Chat list item for the sidebar.
 */
@Composable
fun ChatListItem(
  chat: ChatSession,
  isSelected: Boolean,
  onClick: () -> Unit,
  onDelete: () -> Unit,
  onRename: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var isEditing by remember { mutableStateOf(false) }
  var showMenu by remember { mutableStateOf(false) }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 8.dp, vertical = 2.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
      else
        Color.Transparent
    )
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      if (isEditing) {
        OutlinedTextField(
          value = chat.description ?: "Untitled Chat",
          onValueChange = { /* handle rename */ },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          maxLines = 1
        )
        isEditing = false
      } else {
        Text(
          text = chat.description ?: "Untitled Chat",
          style = MaterialTheme.typography.bodyMedium,
          modifier = Modifier.weight(1f)
        )

        // Message count and date
        Text(
          text = "${chat.messages.size} messages • ${chat.timestamp.take(10)}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
      }

      // Action menu button
      Box(modifier = Modifier.align(Alignment.End)) {
        IconButton(onClick = { showMenu = true }) {
          Icon(Icons.Default.MoreVert, contentDescription = "Options")
        }

        if (showMenu) {
          DropdownMenu(
            expanded = true,
            onDismissRequest = { showMenu = false }
          ) {
            DropdownMenuItem(
              text = { Text("Rename") },
              onClick = { isEditing = true; showMenu = false }
            )
            DropdownMenuItem(
              text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
              onClick = { onDelete(); showMenu = false }
            )
          }
        }
      }
    }
  }
}

/**
 * Chat list screen for the sidebar.
 */
@Composable
fun ChatListScreen(
  chats: List<ChatSession>,
  currentChatId: String?,
  onSelectChat: (String) -> Unit,
  onCreateNewChat: () -> Unit,
  onDeleteChat: (String) -> Unit,
  onRenameChat: (String, String) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxSize()) {
    // Header with new chat button
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
      Button(
        onClick = onCreateNewChat,
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentColor = MaterialTheme.colorScheme.primary
      ) {
        Icon(Icons.Default.Add, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("New Chat")
      }
    }

    // Chat list
    LazyColumn(modifier = Modifier.weight(1f)) {
      items(chats, key = { it.id }) { chat ->
        ChatListItem(
          chat = chat,
          isSelected = chat.id == currentChatId,
          onClick = { onSelectChat(chat.id) },
          onDelete = { onDeleteChat(chat.id) },
          onRename = { newName -> onRenameChat(chat.id, newName) }
        )
      }
    }
  }
}
