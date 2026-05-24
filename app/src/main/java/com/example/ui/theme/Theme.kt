package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val CinematicDarkColorScheme = darkColorScheme(
    primary = CinemaRed,
    secondary = CinemaAmber,
    tertiary = GoldPremium,
    background = DarkBackground,
    surface = DarkCardBg,
    onPrimary = WhiteSoft,
    onSecondary = DarkBackground,
    onBackground = WhiteSoft,
    onSurface = WhiteSoft,
    surfaceVariant = DarkBorder,
    onSurfaceVariant = SlateGray
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force movie dark mode as default for streaming apps
  dynamicColor: Boolean = false, // Disable dynamic content-aware scheme to enforce cinematic aesthetic
  content: @Composable () -> Unit,
) {
  val colorScheme = CinematicDarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
