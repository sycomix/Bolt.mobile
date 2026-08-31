package com.bolt.diy.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bolt.diy.data.model.ChatSession

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
  onChatClick: (String) -> Unit,
  onNewChat: () -> Unit,
  onDeleteChat: (String) -> Unit,
  viewModel: ChatViewModel = hiltViewModel()
) {
  val sessions by viewModel.sessions.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("bolt.diy") },
        actions = {
          IconButton(onClick = onNewChat) {
            Icon(Icons.Default.Add, "New Chat")
          }
        }
      )
    }
  ) { padding ->
    if (sessions.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(Icons.Default.ChatBubbleOutline, "Start a new conversation", modifier = Modifier.size(64.dp))
          Spacer(modifier = Modifier.height(16.dp))
          Text("No conversations yet")
          Button(onClick = onNewChat, modifier = Modifier.padding(top = 16.dp)) {
            Text("New Chat")
          }
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(sessions, key = { it.id }) { session ->
          ChatListItem(
            session = session,
            onClick = { onChatClick(session.id) },
            onDelete = { onDeleteChat(session.id) }
          )
        }
      }
    }
  }
}

@Composable
fun ChatListItem(
  session: ChatSession,
  onClick: () -> Unit,
  onDelete: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 8.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(session.title, style = MaterialTheme.typography.titleMedium)
      if (session.messages.isNotEmpty()) {
        Text(
          session.messages.last().content.take(100),
          style = MaterialTheme.typography.bodySmall,
          maxLines = 2
        )
      } else {
        Text("No messages", style = MaterialTheme.typography.bodySmall)
      }
    }
    IconButton(onClick = onDelete) {
      Icon(Icons.Default.Delete, "Delete")
    }
  }
}
