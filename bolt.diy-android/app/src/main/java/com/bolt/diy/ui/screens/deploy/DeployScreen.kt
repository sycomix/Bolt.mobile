package com.bolt.diy.ui.screens.deploy

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeployScreen(
  onBack: () -> Unit
) {
  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Deploy") },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, "Back")
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
      Text("Deployment Options", style = MaterialTheme.typography.titleMedium)

      DeployOptionCard(
        title = "Netlify",
        description = "Deploy to Netlify with one click",
        icon = Icons.Default.CloudUpload,
        onClick = {}
      )

      DeployOptionCard(
        title = "Vercel",
        description = "Deploy to Vercel with one click",
        icon = Icons.Default.CloudUpload,
        onClick = {}
      )

      DeployOptionCard(
        title = "GitHub Pages",
        description = "Deploy to GitHub Pages",
        icon = Icons.Default.CloudUpload,
        onClick = {}
      )

      DeployOptionCard(
        title = "GitLab Pages",
        description = "Deploy to GitLab Pages",
        icon = Icons.Default.CloudUpload,
        onClick = {}
      )

      DeployOptionCard(
        title = "Supabase",
        description = "Deploy to Supabase",
        icon = Icons.Default.CloudUpload,
        onClick = {}
      )
    }
  }
}

@Composable
fun DeployOptionCard(
  title: String,
  description: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(icon, title, tint = MaterialTheme.colorScheme.primary)
      Column(modifier = Modifier.weight(1f)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(description, style = MaterialTheme.typography.bodySmall)
      }
      Button(onClick = onClick) {
        Text("Deploy")
      }
    }
  }
}
