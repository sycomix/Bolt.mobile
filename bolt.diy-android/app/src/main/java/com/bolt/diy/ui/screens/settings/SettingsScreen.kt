package com.bolt.diy.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  onBack: () -> Unit,
  currentApiKey: String = "",
  onApiKeyChange: (String) -> Unit,
  currentModel: String = "openai/gpt-4o",
  onModelChange: (String) -> Unit,
  temperature: Double = 0.7,
  onTemperatureChange: (Double) -> Unit,
  maxTokens: Int = 4096,
  onMaxTokensChange: (Int) -> Unit
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Settings") },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, "Back")
          }
        },
        actions = {
          IconButton(onClick = { /* save settings */ }) {
            Icon(Icons.Default.Save, "Save")
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text("API Configuration", style = MaterialTheme.typography.titleMedium)
      OutlinedTextField(
        value = currentApiKey,
        onValueChange = onApiKeyChange,
        label = { Text("API Key") },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("sk-...") }
      )

      Text("Model Selection", style = MaterialTheme.typography.titleMedium)
      ExposedDropdownMenuBox(
        expanded = false,
        onExpandedChange = {}
      ) {
        OutlinedTextField(
          value = currentModel,
          onValueChange = {},
          readOnly = true,
          label = { Text("Model") },
          modifier = Modifier.fillMaxWidth()
        )
      }

      Text("Generation Settings", style = MaterialTheme.typography.titleMedium)
      Slider(
        value = temperature.toFloat(),
        onValueChange = { onTemperatureChange(it.toDouble()) },
        valueRange = 0f..1f,
        steps = 10
      )
      Text("Temperature: $temperature")

      OutlinedTextField(
        value = maxTokens.toString(),
        onValueChange = { onMaxTokensChange(it.toIntOrNull() ?: 4096) },
        label = { Text("Max Tokens") },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(
          keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
        )
      )

      Button(onClick = onBack, modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally)) {
        Text("Done")
      }
    }
  }
}
