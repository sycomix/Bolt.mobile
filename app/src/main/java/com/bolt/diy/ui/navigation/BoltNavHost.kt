package com.bolt.diy.ui.navigation

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.bolt.diy.ui.screens.chat.ChatScreen
import com.bolt.diy.ui.screens.editor.EditorScreen
import com.bolt.diy.ui.screens.settings.SettingsScreen
import com.bolt.diy.ui.screens.deploy.DeployScreen
import com.bolt.diy.ui.screens.terminal.TerminalScreen

/**
 * Navigation routes for the bolt.diy app.
 */
object Routes {
  const val HOME = "home"
  const val CHAT = "chat/{chatId}"
  const val EDITOR = "editor"
  const val SETTINGS = "settings"
  const val DEPLOY = "deploy"
}

/**
 * Main navigation host for the bolt.diy app.
 */
@Composable
fun BoltNavHost(
  navController: NavHostController,
  modifier: Modifier = Modifier,
  startDestination: String = Routes.HOME,
  viewModel: ChatViewModel = viewModel()
) {
  NavHost(
    navController = navController,
    startDestination = startDestination,
    modifier = modifier
  ) {
    // Home / New Chat screen
    composable(Routes.HOME) {
      ChatScreen(
        viewModel = viewModel,
        onNavigateToChat = { chatId ->
          navController.navigate("${Routes.CHAT}/$chatId")
        },
        onNavigateToSettings = {
          navController.navigate(Routes.SETTINGS)
        }
      )
    }

    // Chat screen with conversation
    composable("${Routes.CHAT}/{chatId}") { backStackEntry ->
      val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
      ChatScreen(
        viewModel = viewModel,
        chatId = chatId,
        onNavigateBack = { navController.popBackStack() },
        onNavigateToSettings = {
          navController.navigate(Routes.SETTINGS)
        }
      )
    }

    // Editor screen for code files
    composable(Routes.EDITOR) {
      EditorScreen(
        viewModel = viewModel,
        onNavigateBack = { navController.popBackStack() },
        onNavigateToChat = { chatId ->
          navController.navigate("${Routes.CHAT}/$chatId")
        }
      )
    }

    // Settings screen
    composable(Routes.SETTINGS) {
      SettingsScreen(
        viewModel = viewModel,
        onNavigateBack = { navController.popBackStack() }
      )
    }

    // Deploy screen
    composable(Routes.DEPLOY) {
      DeployScreen(
        viewModel = viewModel,
        onNavigateBack = { navController.popBackStack() }
      )
    }
  }
}

/**
 * Extension function for NavGraphBuilder to pass chatId parameter.
 */
fun NavGraphBuilder.chatRoute(
  viewModel: ChatViewModel,
  onNavigateToChat: (String) -> Unit,
  onNavigateToSettings: () -> Unit
) {
  composable("${Routes.CHAT}/{chatId}") { backStackEntry ->
    val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
    ChatScreen(
      viewModel = viewModel,
      chatId = chatId,
      onNavigateBack = { navController.popBackStack() },
      onNavigateToSettings = onNavigateToSettings
    )
  }
}
