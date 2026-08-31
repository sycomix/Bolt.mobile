package com.bolt.diy.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = Color(0xFF8AB4F8),
  onPrimary = Color.Black,
  primaryContainer = Color(0xFF003265),
  secondary = Color(0xFFBDDCFF),
  tertiary = Color(0xFFDCC1E9)
)

private val LightColorScheme = lightColorScheme(
  primary = Color(0xFF0058A7),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFD1E4FF),
  secondary = Color(0xFF396DA0),
  tertiary = Color(0xFF7255A2)
)

@Composable
fun BoltTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = TypographyDefaults.materialTypography(),
    content = content
  )
}
