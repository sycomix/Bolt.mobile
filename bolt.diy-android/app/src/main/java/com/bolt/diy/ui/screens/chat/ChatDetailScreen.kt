package com.bolt.diy.ui.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.bolt.diy.data.model.ChatMessage
import com.bolt.diy.data.model.Role

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
  sessionId: String,
  onBack: () -> Unit,
  viewModel: ChatViewModel = hiltViewModel()
) {
  val messages by viewModel.messages.collectAsState(initial = emptyList())
  var inputText by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  LaunchedEffect(sessionId) {
    viewModel.loadMessages(sessionId)
  }

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(listState.layoutInfo.totalItemsCount.plus(1))
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Chat") },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
          }
        },
        actions = {
          IconButton(onClick = { /* copy */ }) {
            Icon(Icons.Default.ContentCopy, "Copy")
          }
        }
      )
    },
    bottomBar = {
      Surface(shadowElevation = 8.dp) {
        Row(modifier = Modifier.padding(8.dp)) {
          OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Type a message...") },
            maxLines = 4
          )
          IconButton(onClick = {
            if (inputText.isNotBlank()) {
              viewModel.sendMessage(sessionId, ChatMessage(content = inputText, role = Role.USER))
              inputText = ""
            }
          }) {
            Icon(Icons.AutoMirrored.Filled.Send, "Send")
          }
        }
      }
    }
  ) { padding ->
    LazyColumn(
      state = listState,
      modifier = Modifier.fillMaxSize().padding(padding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(messages.size) { index ->
        MessageBubble(message = messages[index])
      }
    }
  }
}

@Composable
fun MessageBubble(message: ChatMessage) {
  val isUser = message.role == Role.USER
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    )
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        message.content,
        color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
      )
      Text(
        message.timestamp.toString(),
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier.align(Alignment.End)
      )
    }
  }
}
