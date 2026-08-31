package com.bolt.diy.domain.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FileSystemService {
  private val fileStore = mutableMapOf<String, String>()
  private val lockStore = mutableMapOf<String, Boolean>()

  data class FileInfo(
    val path: String,
    val content: String,
    val isModified: Boolean,
    val lastModified: Long
  )

  suspend fun readFile(path: String): String? {
    return withContext(Dispatchers.IO) {
      fileStore[path]
    }
  }

  suspend fun writeFile(path: String, content: String) {
    withContext(Dispatchers.IO) {
      fileStore[path] = content
    }
  }

  suspend fun deleteFile(path: String): Boolean {
    return withContext(Dispatchers.IO) {
      fileStore.remove(path) != null
    }
  }

  suspend fun isLocked(path: String): Boolean {
    return lockStore[path] == true
  }

  suspend fun lockFile(path: String) {
    withContext(Dispatchers.IO) {
      lockStore[path] = true
    }
  }

  suspend fun unlockFile(path: String) {
    withContext(Dispatchers.IO) {
      lockStore[path] = false
    }
  }

  suspend fun listFiles(): List<String> {
    return withContext(Dispatchers.IO) {
      fileStore.keys.toList()
    }
  }

  suspend fun getFileTree(rootPath: String = ""): List<Map<String, Any?>> {
    return withContext(Dispatchers.IO) {
      val files = fileStore.filterKeys { it.startsWith(rootPath) }
      files.map { (path, _) ->
        mapOf(
          "path" to path,
          "name" to path.substringAfterLast("/"),
          "type" to "file",
          "isModified" to false
        )
      }
    }
  }

  suspend fun saveFileTree(tree: List<Map<String, Any?>>) {
    withContext(Dispatchers.IO) {
      tree.forEach { item ->
        val path = item["path"] as? String ?: return@forEach
        val content = item["content"] as? String ?: ""
        fileStore[path] = content
      }
    }
  }
}
