package com.bolt.diy.ui.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.bolt.diy.ui.navigation.Route
import com.bolt.diy.ui.screens.chat.ChatListScreen
import com.bolt.diy.ui.screens.chat.ChatDetailScreen
import com.bolt.diy.ui.screens.editor.EditorScreen
import com.bolt.diy.ui.screens.settings.SettingsScreen
import com.bolt.diy.ui.screens.deploy.DeployScreen
import com.bolt.diy.ui.screens.terminal.TerminalScreen

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      BoltApp()
    }
  }
}

@Composable
fun BoltApp() {
  var currentRoute by remember { mutableStateOf(Route.ChatList.route) }
  val onNavigateTo: (String) -> Unit = { route -> currentRoute = route }

  NavigationGraph(
    startDestination = Route.ChatList.route,
    onNavigateTo = onNavigateTo,
    currentRoute = currentRoute
  )
}

@Composable
fun NavigationGraph(
  startDestination: String,
  onNavigateTo: (String) -> Unit,
  currentRoute: String
) {
  when {
    currentRoute.startsWith(Route.ChatDetail.route) -> {
      val sessionId = currentRoute.substringAfter("/")
      ChatDetailScreen(
        sessionId = sessionId,
        onBack = { onNavigateTo(Route.ChatList.route) }
      )
    }

    currentRoute.startsWith(Route.Editor.route) -> {
      val filePath = currentRoute.substringAfter("/")
      EditorScreen(
        filePath = filePath,
        onBack = { onNavigateTo(Route.ChatList.route) }
      )
    }

    currentRoute == Route.Settings.route -> SettingsScreen(
      onBack = { onNavigateTo(Route.ChatList.route) }
    )

    currentRoute == Route.Deploy.route -> DeployScreen(
      onBack = { onNavigateTo(Route.ChatList.route) }
    )

    currentRoute == Route.Terminal.route -> TerminalScreen(
      onBack = { onNavigateTo(Route.ChatList.route) }
    )

    else -> ChatListScreen(
      onChatClick = { sessionId ->
        onNavigateTo("${Route.ChatDetail.route}/$sessionId")
      },
      onNewChat = {},
      onDeleteChat = {}
    )
  }

  BottomNavigation(
    currentRoute = currentRoute,
    onNavigateTo = onNavigateTo
  )
}

@Composable
fun BottomNavigation(
  currentRoute: String,
  onNavigateTo: (String) -> Unit
) {
  val items = listOf(
    Triple(Route.ChatList.route, Icons.Default.Chat, "Chats"),
    Triple(Route.Editor.route, Icons.Default.Code, "Editor"),
    Triple(Route.Terminal.route, Icons.Default.Terminal, "Terminal"),
    Triple(Route.Deploy.route, Icons.Default.CloudUpload, "Deploy"),
    Triple(Route.Settings.route, Icons.Default.Settings, "Settings")
  )

  NavigationBar {
    items.forEach { (route, icon, label) ->
      NavigationBarItem(
        selected = currentRoute == route || currentRoute.startsWith(route),
        onClick = { onNavigateTo(route) },
        icon = { Icon(icon, label) },
        label = { Text(label) }
      )
    }
  }
}
