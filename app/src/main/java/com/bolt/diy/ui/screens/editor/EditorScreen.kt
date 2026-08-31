package com.bolt.diy.ui.screens.editor

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
import com.bolt.diy.data.model.FileEntry

/**
 * File tree component for the editor panel.
 */
@Composable
fun FileTree(
  files: Map<String, Any>,
  onFileClick: (String) -> Unit,
  onFolderClick: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  LazyColumn(modifier = modifier) {
    val folders = files.entries.filterValues { it is com.bolt.diy.data.model.FolderEntry }
    val fileEntries = files.entries.filterValues { it is FileEntry }

    // Group files by folder
    val groupedFiles = mutableMapOf<String, List<FileEntry>>()
    val rootFiles = mutableListOf<FileEntry>()

    for ((path, entry) in fileEntries) {
      if (entry !is FileEntry) continue
      val parentPath = path.substringBeforeLast('/')
      if (parentPath == "") {
        rootFiles.add(entry)
      } else {
        groupedFiles.getOrPut(parentPath) { mutableListOf() }.add(entry)
      }
    }

    // Render folders
    for ((folderPath, folderEntry) in folders) {
      FileTreeItem(
        name = folderPath.substringAfterLast('/'),
        isFolder = true,
        path = folderPath,
        onClick = { onFolderClick(folderPath) },
        depth = 0
      )

      // Render children of this folder
      for ((childPath, childEntry) in files) {
        if (childPath.startsWith("$folderPath/") && childEntry is FileEntry) {
          val depth = childPath.substringAfter("$folderPath/").split("/").size - 1
          FileTreeItem(
            name = childPath.substringAfterLast('/'),
            isFolder = false,
            path = childPath,
            onClick = { onFileClick(childPath) },
            depth = depth + 1
          )
        }
      }
    }

    // Render root files
    for (file in rootFiles.sortedBy { it.path }) {
      FileTreeItem(
        name = file.path.substringAfterLast('/'),
        isFolder = false,
        path = file.path,
        onClick = { onFileClick(file.path) },
        depth = 0
      )
    }
  }
}

@Composable
private fun FileTreeItem(
  name: String,
  isFolder: Boolean,
  path: String,
  onClick: () -> Unit,
  depth: Int
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 8.dp, vertical = 4.dp)
      .padding(start = (depth * 16).dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = if (isFolder) Icons.Default.Folder else Icons.Default.Description,
      contentDescription = null,
      tint = if (isFolder) Color(0xFF6366F1) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
      modifier = Modifier.size(20.dp)
    )
    Spacer(Modifier.width(8.dp))
    Text(
      text = name,
      style = MaterialTheme.typography.bodySmall,
      color = if (isFolder) Color(0xFF6366F1) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )
  }
}

/**
 * Code editor component with syntax highlighting basics.
 */
@Composable
fun CodeEditor(
  filePath: String,
  content: String,
  onContentChange: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxSize()) {
    // File path bar
    Card(
      modifier = Modifier.fillMaxWidth(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(
          text = filePath.substringAfterLast('/'),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }

    // Code content with line numbers
    Row(modifier = Modifier.fillMaxSize()) {
      // Line numbers gutter
      Column(
        modifier = Modifier
          .width(48.dp)
          .fillMaxHeight()
          .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
          .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.End
      ) {
        val lineCount = content.lines().size.coerceAtMost(1000)
        for (i in 1..lineCount) {
          Text(
            text = i.toString(),
            style = MaterialTheme.typography.bodySmall.copy(
              fontFamily = FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
            modifier = Modifier.padding(end = 8.dp)
          )
        }
      }

      // Code text area
      OutlinedTextField(
        value = content,
        onValueChange = onContentChange,
        modifier = Modifier.weight(1f),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
          fontFamily = FontFamily.Monospace,
          lineHeight = 24.sp
        ),
        minLines = 20,
        maxLines = Int.MAX_VALUE,
        modifier = Modifier.fillMaxSize(),
        backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
      )
    }
  }
}

/**
 * Editor panel with file tree and code editor.
 */
@Composable
fun EditorPanel(
  files: Map<String, Any>,
  selectedFile: String?,
  fileContent: String,
  onFileClick: (String) -> Unit,
  onFolderClick: (String) -> Unit,
  onContentChange: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Row(modifier = modifier.fillMaxSize()) {
    // File tree sidebar
    Card(
      modifier = Modifier
        .width(240.dp)
        .fillMaxHeight(),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
    ) {
      Column(modifier = Modifier.padding(8.dp)) {
        Text(
          text = "Files",
          style = MaterialTheme.typography.titleSmall,
          modifier = Modifier.padding(8.dp)
        )
        FileTree(
          files = files,
          onFileClick = onFileClick,
          onFolderClick = onFolderClick
        )
      }
    }

    // Code editor
    if (selectedFile != null) {
      Card(
        modifier = Modifier.weight(1f).fillMaxHeight(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        CodeEditor(
          filePath = selectedFile,
          content = fileContent,
          onContentChange = onContentChange
        )
      }
    } else {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Select a file to edit")
      }
    }
  }
}
