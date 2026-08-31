package com.bolt.diy.ui.screens.terminal

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminalScreen(
  onBack: () -> Unit,
  initialCommand: String = ""
) {
  var commandHistory by remember { mutableStateOf(listOf<String>()) }
  var currentInput by remember { mutableStateOf("") }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Terminal") },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, "Back")
          }
        },
        actions = {
          IconButton(onClick = { /* clear */ }) {
            Icon(Icons.Default.Terminal, "Clear")
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
    ) {
      // Terminal output area
      LazyColumn(
        modifier = Modifier.weight(1f),
        contentPadding = PaddingValues(8.dp)
      ) {
        items(commandHistory.size) { index ->
          Text(
            commandHistory[index],
            style = MaterialTheme.typography.bodySmall.copy(
              fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      // Command input
      Row(modifier = Modifier.padding(8.dp)) {
        Text("❯ ", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.primary))
        OutlinedTextField(
          value = currentInput,
          onValueChange = { currentInput = it },
          modifier = Modifier.weight(1f),
          textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
          ),
          onKeyboardAction = {
            if (currentInput.isNotBlank()) {
              commandHistory += "❯ $currentInput"
              // Simulate command execution
              commandHistory += "Command executed: $currentInput"
              currentInput = ""
            }
          }
        )
      }
    }
  }
}
