package com.bolt.diy.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat

private val LightColorScheme = lightColorScheme(
  primary = Color(0xFF0284C7),
  onPrimary = Color.White,
  secondary = Color(0xFF6366F1),
  onSecondary = Color.White,
  tertiary = Color(0xFF8B5CF6),
  onTertiary = Color.White,
  background = Color(0xFFF8FAFC),
  onBackground = Color(0xFF1E293B),
  surface = Color(0xFFF1F5F9),
  onSurface = Color(0xFF334155),
  surfaceVariant = Color(0xFFE2E8F0),
  onSurfaceVariant = Color(0xFF64748B),
  error = Color(0xFFDC2626),
  onError = Color.White,
  primaryContainer = Color(0xFFBAE6FD),
  onPrimaryContainer = Color(0xFF083C59),
  secondaryContainer = Color(0xFFDDD6FE),
  onSecondaryContainer = Color(0xFF312B7A),
  surfaceBright = Color(0xFFF8FAFC),
  surfaceDim = Color(0xFFE2E8F0)
)

private val DarkColorScheme = darkColorScheme(
  primary = Color(0xFF60A5FA),
  onPrimary = Color.Black,
  secondary = Color(0xFFA78BFA),
  onSecondary = Color.Black,
  tertiary = Color(0xFFC4B5FD),
  onTertiary = Color.Black,
  background = Color(0xFF0F172A),
  onBackground = Color(0xFFE2E8F0),
  surface = Color(0xFF1E293B),
  onSurface = Color(0xFFCBD5E1),
  surfaceVariant = Color(0xFF334155),
  onSurfaceVariant = Color(0xFF94A3B8),
  error = Color(0xFFF87171),
  onError = Color.Black,
  primaryContainer = Color(0xFF083C59),
  onPrimaryContainer = Color(0xFFBAE6FD),
  secondaryContainer = Color(0xFF2A236B),
  onSecondaryContainer = Color(0xFFDDD6FE),
  surfaceBright = Color(0xFF1E293B),
  surfaceDim = Color(0xFF0F172A)
)

@Composable
fun BoltTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      androidx.compose.material3.dynamicColorScheme(darkTheme)
    }

    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = android.graphics.Color.TRANSPARENT
      WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars = !darkTheme
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = BoltTypography,
    content = content
  )
}

val BoltTypography = Typography(
  displayLarge = TextStyle.Default.copy(fontSize = androidx.compose.ui.unit.TextUnit(57f)),
  displayMedium = TextStyle.Default.copy(fontSize = androidx.compose.ui.unit.TextUnit(45f)),
  displaySmall = TextStyle.Default.copy(fontSize = androidx.compose.ui.unit.TextUnit(36f)),
  headlineLarge = TextStyle.Default.copy(fontSize = androidx.compose.ui.unit.TextUnit(32f)),
  headlineMedium = TextStyle.Default.copy(fontSize = androidx.compose.ui.unit.TextUnit(28f)),
  headlineSmall = TextStyle.Default.copy(fontSize = androidx.compose.ui.unit.TextUnit(24f)),
  titleLarge = TextStyle.Default.copy(fontSize = androidx.compose.ui.unit.TextUnit(22f)),
  titleMedium = TextStyle.Default.copy(fontSize = androidx.compose.ui.unit.TextUnit(16f)),
  bodyLarge = TextStyle.Default.copy(fontSize = androidx.compose.ui.unit.TextUnit(16f)),
  bodyMedium = TextStyle.Default.copy(fontSize = androidx.compose.ui.unit.TextUnit(14f)),
  labelLarge = TextStyle.Default.copy(fontSize = androidx.compose.ui.unit.TextUnit(14f))
)
