package com.bolt.diy.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore for persisting app settings and provider configurations.
 */
val Context.dataStore: DataStore<androidx.datastore.preferences.core.Preferences> by preferencesDataStore(name = "bolt_settings")

object SettingsRepository {
  private const val PROVIDER_SETTINGS_KEY = "provider_settings"
  private const val AUTO_ENABLED_PROVIDERS_KEY = "auto_enabled_providers"
  private const val THEME_KEY = "theme"
  private const val CONTEXT_OPTIMIZATION_KEY = "context_optimization"
  private const val EVENT_LOGS_KEY = "event_logs"
  private const val DEVELOPER_MODE_KEY = "developer_mode"
  private const val PROMPT_ID_KEY = "prompt_id"

  fun getTheme(context: Context): Flow<String> {
    return context.dataStore.data.map { prefs ->
      prefs[themePreferencesKey] ?: "dark"
    }
  }

  fun updateTheme(context: Context, theme: String) {
    val key = themePreferencesKey()
    context.dataStore.edit { prefs ->
      prefs[key] = theme
    }
  }

  fun getContextOptimization(context: Context): Flow<Boolean> {
    return context.dataStore.data.map { prefs ->
      prefs[contextOptimizationPreferencesKey] ?: true
    }
  }

  fun updateContextOptimization(context: Context, enabled: Boolean) {
    val key = contextOptimizationPreferencesKey()
    context.dataStore.edit { prefs ->
      prefs[key] = enabled
    }
  }

  fun getDeveloperMode(context: Context): Flow<Boolean> {
    return context.dataStore.data.map { prefs ->
      prefs[developerModePreferencesKey] ?: false
    }
  }

  fun updateDeveloperMode(context: Context, enabled: Boolean) {
    val key = developerModePreferencesKey()
    context.dataStore.edit { prefs ->
      prefs[key] = enabled
    }
  }

  fun getPromptId(context: Context): Flow<String> {
    return context.dataStore.data.map { prefs ->
      prefs[promptIdPreferencesKey] ?: "default"
    }
  }

  fun updatePromptId(context: Context, id: String) {
    val key = promptIdPreferencesKey()
    context.dataStore.edit { prefs ->
      prefs[key] = id
    }
  }

  // Custom keys for provider settings storage
  private fun themePreferencesKey(): Preferences.Key<String> = stringPreferencesKey("theme")
  private fun contextOptimizationPreferencesKey(): Preferences.Key<Boolean> =
    booleanPreferencesKey("context_optimization")

  private fun developerModePreferencesKey(): Preferences.Key<Boolean> = booleanPreferencesKey("developer_mode")
  private fun promptIdPreferencesKey(): Preferences.Key<String> = stringPreferencesKey("prompt_id")
}
