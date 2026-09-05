package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Outdoor Slate Dark Scheme (#121E17)
val AgriCalcSlateDarkScheme = darkColorScheme(
  primary = LeafGreenAccent,
  onPrimary = ForestGreenDark,
  primaryContainer = ForestGreenPrimary,
  onPrimaryContainer = LeafGreenBright,
  secondary = LeafGreenBright,
  onSecondary = ForestGreenDark,
  tertiary = EarthAmber,
  onTertiary = Color.White,
  background = SlateDarkBackground,
  onBackground = TextLightHighContrast,
  surface = SlateDarkSurface,
  onSurface = TextLightHighContrast,
  surfaceVariant = SlateDarkCard,
  onSurfaceVariant = TextLightMedium,
  outline = SlateDarkBorder,
  error = EarthAmber
)

// Sunlight High-Contrast White Scheme (#FFFFFF)
val AgriCalcCrispWhiteScheme = lightColorScheme(
  primary = ForestGreenPrimary,
  onPrimary = Color.White,
  primaryContainer = LeafGreenContainer,
  onPrimaryContainer = ForestGreenDark,
  secondary = ForestGreenLight,
  onSecondary = Color.White,
  tertiary = EarthAmber,
  onTertiary = Color.White,
  background = CrispWhiteBackground,
  onBackground = TextDarkHighContrast,
  surface = CrispWhiteSurface,
  onSurface = TextDarkHighContrast,
  surfaceVariant = CrispWhiteCard,
  onSurfaceVariant = TextDarkMedium,
  outline = CrispWhiteBorder,
  error = EarthAmber
)

@Composable
fun AgriCalcTheme(
  sunlightMode: Boolean = false,
  content: @Composable () -> Unit,
) {
  // sunlightMode = true -> Crisp White high contrast (#FFFFFF)
  // sunlightMode = false -> Slate Dark high contrast (#121E17)
  val colorScheme = if (sunlightMode) AgriCalcCrispWhiteScheme else AgriCalcSlateDarkScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

