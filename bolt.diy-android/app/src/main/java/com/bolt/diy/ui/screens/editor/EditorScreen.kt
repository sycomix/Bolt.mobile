package com.bolt.diy.ui.screens.editor

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
  filePath: String,
  onBack: () -> Unit,
  initialContent: String = ""
) {
  var content by remember(filePath) { mutableStateOf(initialContent) }
  var showFileTree by remember { mutableStateOf(true) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Editor: $filePath") },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
          }
        },
        actions = {
          IconButton(onClick = { /* save */ }) {
            Icon(Icons.Default.Save, "Save")
          }
          IconButton(onClick = { showFileTree = !showFileTree }) {
            Icon(Icons.Default.Folder, "Files")
          }
        }
      )
    }
  ) { padding ->
    Row(modifier = Modifier.fillMaxSize().padding(padding)) {
      if (showFileTree) {
        FileTreePanel(
          modifier = Modifier.width(200.dp),
          onFileClick = {}
        )
      }
      CodeEditorPanel(
        modifier = Modifier.weight(1f),
        content = content,
        onContentChange = { content = it },
        filePath = filePath
      )
    }
  }
}

@Composable
fun FileTreePanel(modifier: Modifier = Modifier, onFileClick: (String) -> Unit) {
  Card(
    modifier = modifier,
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Column(modifier = Modifier.padding(8.dp)) {
      Text("Project Files", style = MaterialTheme.typography.titleSmall)
      // Simulated file tree
      listOf("src/", "app/build.gradle.kts", "README.md").forEach { name ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.primary)
          Text(name)
        }
      }
    }
  }
}

@Composable
fun CodeEditorPanel(
  modifier: Modifier = Modifier,
  content: String,
  onContentChange: (String) -> Unit,
  filePath: String
) {
  Card(modifier = modifier) {
    Column {
      // Line numbers gutter
      Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.width(40.dp).background(MaterialTheme.colorScheme.surfaceVariant)) {
          Text("1", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall)
        }
        OutlinedTextField(
          value = content,
          onValueChange = onContentChange,
          modifier = Modifier.weight(1f),
          textStyle = MaterialTheme.typography.bodyMedium.copy(
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
          ),
          minLines = 20,
          maxLines = Int.MAX_VALUE
        )
      }
    }
  }
}
