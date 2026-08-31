package com.bolt.diy.ui.screens.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bolt.diy.data.model.FileEntry
import com.bolt.diy.domain.service.FileSystemService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the editor panel.
 */
@HiltViewModel
class EditorViewModel @Inject constructor(
  private val fileSystemService: FileSystemService
) : ViewModel() {

  private val _selectedFilePath = MutableStateFlow<String?>(null)
  val selectedFilePath: StateFlow<String?> = _selectedFilePath

  private val _fileContent = MutableStateFlow("")
  val fileContent: StateFlow<String> = _fileContent

  val files = fileSystemService.files

  /**
   * Select a file to edit.
   */
  fun selectFile(filePath: String) {
    _selectedFilePath.value = filePath
    val file = fileSystemService.getFile(filePath)
    if (file != null && file.type == "file") {
      _fileContent.value = file.content
    } else {
      _fileContent.value = ""
    }
  }

  /**
   * Save the current file content.
   */
  fun saveFile() {
    val path = _selectedFilePath.value ?: return
    val content = _fileContent.value
    fileSystemService.saveFile(path, content)
  }

  /**
   * Get all files grouped by folder structure.
   */
  fun getFolderStructure(): Map<String, List<FileEntry>> {
    val allFiles = fileSystemService.getAllFiles()
    val grouped = mutableMapOf<String, MutableList<FileEntry>>()

    for (file in allFiles.values) {
      if (file !is FileEntry) continue
      val parentPath = file.path.substringBeforeLast('/')
      val folderKey = if (parentPath.isEmpty()) "root" else parentPath
      grouped.getOrPut(folderKey) { mutableListOf() }.add(file)
    }

    return grouped
  }
}
