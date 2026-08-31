package com.bolt.diy.domain.service

import com.bolt.diy.data.model.FileEntry
import com.bolt.diy.data.model.FolderEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * Service for managing files in the virtual filesystem.
 * Mirrors the FilesStore from the web app's FilesStore class.
 */
class FileSystemService {

  private val _files = MutableStateFlow<Map<String, Any>>(emptyMap())
  val files: StateFlow<Map<String, Any>> = _files

  private val _modifiedFiles = MutableStateFlow<Map<String, String>>(emptyMap())
  private val _deletedPaths = MutableStateFlow<Set<String>>(emptySet())
  private var fileCount = 0

  /**
   * Get a file entry by path.
   */
  fun getFile(path: String): FileEntry? {
    val currentFiles = _files.value
    val dirent = currentFiles[path] ?: return null
    return when (dirent) {
      is FileEntry -> dirent
      else -> null
    }
  }

  /**
   * Get any file or folder by path.
   */
  fun getFileOrFolder(path: String): Any? {
    return _files.value[path]
  }

  /**
   * Check if a file is locked.
   */
  fun isFileLocked(filePath: String): Boolean {
    val file = getFile(filePath) ?: return false
    return file.isLocked
  }

  /**
   * Lock a file.
   */
  fun lockFile(filePath: String): Boolean {
    val file = getFile(filePath) ?: return false
    _files.update { current ->
      current.toMutableMap().apply {
        this[filePath] = file.copy(isLocked = true)
      }
    }
    return true
  }

  /**
   * Unlock a file.
   */
  fun unlockFile(filePath: String): Boolean {
    val file = getFile(filePath) ?: return false
    _files.update { current ->
      current.toMutableMap().apply {
        this[filePath] = file.copy(isLocked = false, lockedByFolder = null)
      }
    }
    return true
  }

  /**
   * Save file content.
   */
  fun saveFile(filePath: String, content: String) {
    val currentFiles = _files.value
    val existingFile = getFile(filePath)

    // Track original content for modifications
    if (existingFile != null && existingFile.type == "file") {
      _modifiedFiles.update { modified ->
        modified.toMutableMap().apply {
          this[filePath] = existingFile.content
        }
      }
    }

    _files.update { current ->
      current.toMutableMap().apply {
        this[filePath] = FileEntry(
          path = filePath,
          content = content,
          isBinary = false,
          isLocked = existingFile?.isLocked ?: false,
          lockedByFolder = existingFile?.lockedByFolder
        )
      }
    }
  }

  /**
   * Create a new file.
   */
  suspend fun createFile(filePath: String, content: String = "") {
    val dirPath = filePath.substringBeforeLast('/')
    if (dirPath.isNotEmpty() && dirPath != ".") {
      createFolder(dirPath)
    }

    _files.update { current ->
      current.toMutableMap().apply {
        this[filePath] = FileEntry(
          path = filePath,
          content = content.ifEmpty { " " },
          isBinary = false,
          isLocked = false
        )
      }
    }

    _modifiedFiles.update { modified ->
      modified.toMutableMap().apply {
        this[filePath] = content
      }
    }

    fileCount++
  }

  /**
   * Create a folder.
   */
  suspend fun createFolder(folderPath: String) {
    _files.update { current ->
      current.toMutableMap().apply {
        this[folderPath] = FolderEntry(
          path = folderPath,
          isLocked = false
        )
      }
    }
  }

  /**
   * Delete a file.
   */
  suspend fun deleteFile(filePath: String) {
    _files.update { current ->
      current.toMutableMap().apply {
        this[filePath] = null
      }
    }

    _deletedPaths.update { deleted ->
      deleted + filePath
    }

    fileCount--
    _modifiedFiles.update { modified ->
      modified.toMutableMap().apply {
        remove(filePath)
      }
    }
  }

  /**
   * Delete a folder and all its contents.
   */
  suspend fun deleteFolder(folderPath: String) {
    val currentFiles = _files.value.toMutableMap()
    val prefix = "$folderPath/"

    currentFiles.keys.filter { it.startsWith(prefix) }.forEach { path ->
      currentFiles[path] = null
      if (getFile(path)?.let { it as? FileEntry } != null) {
        fileCount--
      }
    }

    currentFiles[folderPath] = null
    _files.update { currentFiles }

    _deletedPaths.update { deleted ->
      deleted + folderPath
    }
  }

  /**
   * Get file modifications since last save.
   */
  fun getModifiedFiles(): Map<String, FileEntry> {
    val result = mutableMapOf<String, FileEntry>()
    val currentFiles = _files.value

    for ((filePath, originalContent) in _modifiedFiles.value) {
      val currentFile = getFile(filePath) ?: continue
      if (currentFile.content == originalContent) continue
      result[filePath] = currentFile as? FileEntry ?: continue
    }

    return result
  }

  /**
   * Reset file modifications tracking.
   */
  fun resetFileModifications() {
    _modifiedFiles.update { emptyMap() }
  }

  /**
   * Get all files for AI context.
   */
  fun getAllFiles(): Map<String, FileEntry> {
    return _files.value.entries
      .filterValues { it is FileEntry }
      .mapValues { it.value as FileEntry }
      .toMap()
  }

  /**
   * Get all modified files for AI context.
   */
  fun getModifiedFilesForContext(): Map<String, String> {
    val result = mutableMapOf<String, String>()
    val currentFiles = _files.value

    for ((filePath, originalContent) in _modifiedFiles.value) {
      val currentFile = getFile(filePath) ?: continue
      if (currentFile.content != originalContent) {
        result[filePath] = currentFile.content
      }
    }

    return result
  }
}
