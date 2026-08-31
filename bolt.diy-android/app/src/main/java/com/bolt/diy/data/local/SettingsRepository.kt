package com.bolt.diy.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.bolt.diy.data.model.LlmProvider
import com.bolt.diy.data.model.Settings
import com.bolt.diy.data.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<androidx.datastore.preferences.preferencesProto.Preferences> by preferencesDataStore(
  name = "bolt_settings"
)

class SettingsRepository(private val context: Context) {
  private val dataStore = context.dataStore

  private val DEFAULT_MODEL = stringPreferencesKey("default_model")
  private val TEMPERATURE = floatPreferencesKey("temperature")
  private val MAX_TOKENS = intPreferencesKey("max_tokens")
  private val THEME_MODE = stringPreferencesKey("theme_mode")
  private val CONTEXT_OPTIMIZATION = booleanPreferencesKey("context_optimization")
  private val AUTO_SAVE_INTERVAL = longPreferencesKey("auto_save_interval")
  private val PROVIDERS_JSON = stringPreferencesKey("providers_json")

  fun getSettings(): Flow<Settings> {
    return dataStore.data.map { prefs ->
      Settings(
        defaultModel = prefs[DEFAULT_MODEL] ?: "openai/gpt-4o",
        temperature = prefs[TEMPERATURE]?.toDouble() ?: 0.7,
        maxTokens = prefs[MAX_TOKENS] ?: 4096,
        theme = try {
          ThemeMode.valueOf(prefs[THEME_MODE] ?: "SYSTEM")
        } catch (e: Exception) {
          ThemeMode.SYSTEM
        },
        contextOptimization = prefs[CONTEXT_OPTIMIZATION] ?: true,
        autoSaveInterval = prefs[AUTO_SAVE_INTERVAL] ?: 5000,
        providers = parseProviders(prefs[PROVIDERS_JSON])
      )
    }
  }

  suspend fun saveSettings(settings: Settings) {
    dataStore.edit { prefs ->
      prefs[DEFAULT_MODEL] = settings.defaultModel
      prefs[TEMPERATURE] = settings.temperature.toFloat()
      prefs[MAX_TOKENS] = settings.maxTokens
      prefs[THEME_MODE] = settings.theme.name
      prefs[CONTEXT_OPTIMIZATION] = settings.contextOptimization
      prefs[AUTO_SAVE_INTERVAL] = settings.autoSaveInterval
      prefs[PROVIDERS_JSON] = Json.encodeToString(settings.providers)
    }
  }

  private fun parseProviders(json: String?): List<LlmProvider> {
    return try {
      if (json.isNullOrEmpty()) emptyList()
      else Json.decodeFromString(json)
    } catch (e: Exception) {
      emptyList()
    }
  }
}
