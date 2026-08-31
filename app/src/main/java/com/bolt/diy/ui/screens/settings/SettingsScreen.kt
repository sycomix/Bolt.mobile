package com.bolt.diy.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bolt.diy.domain.model.Provider

/**
 * Settings screen mirroring the web app's settings panels.
 */
@Composable
fun SettingsScreen(
  providers: List<Provider>,
  onUpdateProvider: (String, String) -> Unit,
  onToggleProvider: (String, Boolean) -> Unit,
  onSetDefaultModel: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxSize()) {
    // Settings tabs
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Providers", "Models", "General", "Advanced")

    TabRow(selectedTabIndex = selectedTab) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTab == index,
          onClick = { selectedTab = index },
          text = { Text(title) }
        )
      }
    }

    when (selectedTab) {
      0 -> ProvidersSettings(providers, onUpdateProvider, onToggleProvider)
      1 -> ModelsSettings(onSetDefaultModel)
      2 -> GeneralSettings()
      3 -> AdvancedSettings()
    }
  }
}

@Composable
private fun ProvidersSettings(
  providers: List<Provider>,
  onUpdateProvider: (String, String) -> Unit,
  onToggleProvider: (String, Boolean) -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }

  Column(modifier = Modifier.padding(16.dp)) {
    // Search bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      placeholder = { Text("Search providers...") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
    )

    Spacer(Modifier.height(16.dp))

    // Cloud providers section
    Text(
      text = "Cloud Providers",
      style = MaterialTheme.typography.titleMedium,
      modifier = Modifier.padding(vertical = 8.dp)
    )

    LazyColumn {
      items(providers.filter { !it.isLocalProvider }) { provider ->
        ProviderCard(
          provider = provider,
          onToggle = { enabled -> onToggleProvider(provider.name, enabled) },
          onUpdateKey = { key -> onUpdateProvider(provider.name, key) }
        )
      }
    }

    // Local providers section
    Text(
      text = "Local Providers",
      style = MaterialTheme.typography.titleMedium,
      modifier = Modifier.padding(vertical = 16.dp)
    )

    LazyColumn {
      items(providers.filter { it.isLocalProvider }) { provider ->
        ProviderCard(
          provider = provider,
          onToggle = { enabled -> onToggleProvider(provider.name, enabled) },
          onUpdateKey = { key -> onUpdateProvider(provider.name, key) }
        )
      }
    }
  }
}

@Composable
private fun ModelsSettings(onSetDefaultModel: (String) -> Unit) {
  Column(modifier = Modifier.padding(16.dp)) {
    Text(
      text = "Default Model",
      style = MaterialTheme.typography.titleMedium,
      modifier = Modifier.padding(vertical = 8.dp)
    )

    OutlinedTextField(
      value = "",
      onValueChange = { model -> /* handle model selection */ },
      placeholder = { Text("Enter model ID...") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true
    )

    Spacer(Modifier.height(16.dp))

    Button(
      onClick = { /* set default model */ },
      modifier = Modifier.fillMaxWidth()
    ) {
      Text("Set Default Model")
    }
  }
}

@Composable
private fun GeneralSettings() {
  Column(modifier = Modifier.padding(16.dp)) {
    Text(
      text = "General Settings",
      style = MaterialTheme.typography.titleMedium,
      modifier = Modifier.padding(vertical = 8.dp)
    )

    // Theme setting
    var darkMode by remember { mutableStateOf(true) }
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Dark Mode", modifier = Modifier.weight(1f))
      Switch(checked = darkMode, onCheckedChange = { darkMode = it })
    }

    Spacer(Modifier.height(16.dp))

    // Context optimization
    var contextOptimization by remember { mutableStateOf(true) }
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Context Optimization", modifier = Modifier.weight(1f))
      Switch(checked = contextOptimization, onCheckedChange = { contextOptimization = it })
    }

    Spacer(Modifier.height(16.dp))

    // Developer mode
    var developerMode by remember { mutableStateOf(false) }
    Row(
      modifier = Modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text("Developer Mode", modifier = Modifier.weight(1f))
      Switch(checked = developerMode, onCheckedChange = { developerMode = it })
    }
  }
}

@Composable
private fun AdvancedSettings() {
  Column(modifier = Modifier.padding(16.dp)) {
    Text(
      text = "Advanced Settings",
      style = MaterialTheme.typography.titleMedium,
      modifier = Modifier.padding(vertical = 8.dp)
    )

    // Custom API endpoints
    OutlinedTextField(
      value = "",
      onValueChange = { /* custom endpoint */ },
      placeholder = { Text("Custom API Endpoint") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true
    )

    Spacer(Modifier.height(16.dp))

    // Max tokens
    var maxTokens by remember { mutableIntStateOf(4096) }
    OutlinedTextField(
      value = maxTokens.toString(),
      onValueChange = { newValue ->
        maxTokens = newValue.toIntOrNull() ?: 4096
      },
      placeholder = { Text("Max Tokens") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      keyboardOptions = androidx.compose.foundation.text.input.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
    )

    Spacer(Modifier.height(16.dp))

    // Temperature
    var temperature by remember { mutableFloatStateOf(0.7f) }
    OutlinedTextField(
      value = temperature.toString(),
      onValueChange = { newValue ->
        temperature = newValue.toFloatOrNull() ?: 0.7f
      },
      placeholder = { Text("Temperature (0-1)") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
      keyboardOptions = androidx.compose.foundation.text.input.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
    )
  }
}

@Composable
private fun ProviderCard(
  provider: Provider,
  onToggle: (Boolean) -> Unit,
  onUpdateKey: (String) -> Unit
) {
  var isExpanded by remember { mutableStateOf(false) }
  var apiKey by remember { mutableStateOf(provider.apiKey) }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (provider.enabled)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
      else
        MaterialTheme.colorScheme.surface
    )
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Provider header with toggle
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = provider.name,
          style = MaterialTheme.typography.titleSmall,
          modifier = Modifier.weight(1f)
        )

        Switch(
          checked = provider.enabled,
          onCheckedChange = { enabled ->
            onToggle(enabled)
          }
        )
      }

      // Expandable API key section
      if (isExpanded) {
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
          value = apiKey,
          onValueChange = { newKey ->
            apiKey = newKey
            onUpdateKey(newKey)
          },
          placeholder = { Text("Enter API Key") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          supportingText = { Text(provider.baseUrl ?: "No base URL configured") }
        )
      } else {
        TextButton(onClick = { isExpanded = true }) {
          Text("Configure API Key")
        }
      }
    }
  }
}
