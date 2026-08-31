package com.bolt.diy.ui.screens.deploy

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Deployment screen mirroring the web app's deploy functionality.
 */
@Composable
fun DeployScreen(
  onDeployToNetlify: () -> Unit,
  onDeployToVercel: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier.fillMaxSize().padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Text(
      text = "Deployments",
      style = MaterialTheme.typography.headlineMedium,
      modifier = Modifier.padding(bottom = 8.dp)
    )

    DeployOptionCard(
      title = "Netlify",
      description = "Deploy to Netlify for fast hosting with continuous deployment.",
      icon = Icons.Default.Cloud,
      onDeploy = onDeployToNetlify
    )

    DeployOptionCard(
      title = "Vercel",
      description = "Deploy to Vercel for edge hosting with preview deployments.",
      icon = Icons.Default.Speed,
      onDeploy = onDeployToVercel
    )

    // GitHub deployment
    var showGitHubDialog by remember { mutableStateOf(false) }
    DeployOptionCard(
      title = "GitHub",
      description = "Connect to GitHub for repository-based deployment.",
      icon = Icons.Default.Code,
      onDeploy = { showGitHubDialog = true }
    )

    // GitLab deployment
    var showGitLabDialog by remember { mutableStateOf(false) }
    DeployOptionCard(
      title = "GitLab",
      description = "Connect to GitLab for repository-based deployment.",
      icon = Icons.Default.Code,
      onDeploy = { showGitLabDialog = true }
    )

    // Supabase connection
    var supabaseConnected by remember { mutableStateOf(false) }
    DeployOptionCard(
      title = "Supabase",
      description = "Connect to Supabase for database and authentication.",
      icon = Icons.Default.Database,
      onDeploy = { /* toggle Supabase */ }
    )
  }

  // GitHub deployment dialog
  if (showGitHubDialog) {
    GitHubDeploymentDialog(onDismiss = { showGitHubDialog = false })
  }

  // GitLab deployment dialog
  if (showGitLabDialog) {
    GitLabDeploymentDialog(onDismiss = { showGitLabDialog = false })
  }
}

@Composable
private fun DeployOptionCard(
  title: String,
  description: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onDeploy: () -> Unit
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(title, style = MaterialTheme.typography.titleMedium)
          Text(description, style = MaterialTheme.typography.bodySmall)
        }
      }

      Button(
        onClick = onDeploy,
        modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
      ) {
        Text("Deploy")
      }
    }
  }
}

@Composable
private fun GitHubDeploymentDialog(onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("GitHub Deployment") },
    text = {
      Column {
        OutlinedTextField(
          value = "",
          onValueChange = {},
          placeholder = { Text("GitHub Repository URL") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
          value = "",
          onValueChange = {},
          placeholder = { Text("GitHub Personal Access Token (optional)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) { Text("Connect") }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@Composable
private fun GitLabDeploymentDialog(onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("GitLab Deployment") },
    text = {
      Column {
        OutlinedTextField(
          value = "",
          onValueChange = {},
          placeholder = { Text("GitLab Repository URL") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
          value = "",
          onValueChange = {},
          placeholder = { Text("GitLab Personal Access Token (optional)") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) { Text("Connect") }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
