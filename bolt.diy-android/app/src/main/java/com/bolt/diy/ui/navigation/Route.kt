package com.bolt.diy.ui.navigation

sealed class Route(val route: String) {
  object ChatList : Route("chat_list")
  object ChatDetail : Route("chat_detail/{sessionId}") {
    val sessionId: String get() = "chat_detail/{sessionId}"
  }

  object Editor : Route("editor/{filePath}") {
    val filePath: String get() = "editor/{filePath}"
  }

  object Settings : Route("settings")
  object Deploy : Route("deploy")
  object Terminal : Route("terminal")
}
