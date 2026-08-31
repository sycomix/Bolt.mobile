package com.bolt.diy.ui.screens.terminal

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Terminal emulator screen mirroring the web app's xterm terminal.
 */
@Composable
fun TerminalScreen(
  isVisible: Boolean,
  onClose: () -> Unit,
  onCommand: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  if (!isVisible) return

  var inputText by remember { mutableStateOf("") }
  val terminalLines = remember { mutableListOf<String>() }

  // Simulated terminal output
  LaunchedEffect(Unit) {
    terminalLines.add("$ bolt.diy Terminal v1.0.0")
    terminalLines.add("Type 'help' for available commands.")
  }

  Card(
    modifier = modifier.fillMaxSize(),
    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Terminal header bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFF2D2D2D))
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Terminal",
          style = MaterialTheme.typography.titleSmall,
          color = Color.White
        )

        IconButton(onClick = onClose) {
          Icon(Icons.Default.Close, contentDescription = "Close terminal", tint = Color.White)
        }
      }

      // Terminal output area
      Column(modifier = Modifier.weight(1f)) {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          reverseLayout = false
        ) {
          items(terminalLines.size) { index ->
            Text(
              text = terminalLines[index],
              color = if (index == terminalLines.lastIndex) Color(0xFF00FF00) else Color.Gray,
              fontFamily = FontFamily.Monospace,
              fontSize = 12.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }
        }

        // Terminal input line
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "bolt@android:~$ ",
            color = Color.Green,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp
          )

          OutlinedTextField(
            value = inputText,
            onValueChange = { newText ->
              inputText = newText
              // Process command when Enter is pressed
              if (newText.contains("\n")) {
                val command = newText.trim()
                if (command.isNotBlank()) {
                  handleTerminalCommand(command, terminalLines) { result ->
                    // Update output
                  }
                  onCommand(command)
                }
                inputText = ""
              }
            },
            modifier = Modifier.weight(1f),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
              fontFamily = FontFamily.Monospace,
              fontSize = 12.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = Color.Transparent,
              unfocusedBorderColor = Color.Transparent,
              cursorColor = Color.Green
            ),
            singleLine = true
          )
        }
      }
    }
  }
}

private fun handleTerminalCommand(command: String, terminalLines: MutableList<String>) {
  when (command.lowercase()) {
    "help" -> {
      terminalLines.add("Available commands:")
      terminalLines.add("  help     - Show this help message")
      terminalLines.add("  ls       - List files")
      terminalLines.add("  pwd      - Print working directory")
      terminalLines.add("  clear    - Clear terminal")
      terminalLines.add("  exit     - Close terminal")
    }

    "ls" -> {
      terminalLines.add("src/")
      terminalLines.add("public/")
      terminalLines.add("package.json")
      terminalLines.add("README.md")
    }

    "pwd" -> {
      terminalLines.add("/home/bolt/project")
    }

    "clear" -> {
      terminalLines.clear()
    }

    "exit" -> {
      terminalLines.add("Terminal closed.")
    }

    else -> {
      terminalLines.add("Command not found: $command. Type 'help' for available commands.")
    }
  }
}
