package com.remmi.browser.ui.theme

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.remmi.browser.storage.AppearanceMode

/**
 * Palette of vivid, warm pictorial icon colors inspired by modern Notes & craft apps.
 */
object PictorialPalette {
  val Coral = Color(0xFFD96B27) // Warm Terracotta Orange
  val Emerald = Color(0xFF2E7D5B) // Forest Sage Emerald
  val Purple = Color(0xFF6E48AA) // Royal Amethyst Purple
  val Blue = Color(0xFF2563EB) // Ocean Cobalt Blue
  val Sky = Color(0xFF0284C7) // Vivid Sky Blue
  val Amber = Color(0xFFD97706) // Golden Amber
  val Rose = Color(0xFFE11D48) // Coral Rose Red
  val Teal = Color(0xFF0D9488) // Botanical Teal
  val Indigo = Color(0xFF4F46E5) // Deep Indigo

  val all = listOf(Coral, Emerald, Purple, Blue, Sky, Amber, Rose, Teal, Indigo)
  val size: Int get() = all.size
  operator fun get(index: Int): Color = all[index % all.size]
}

fun pictorialColorFor(key: String): Color {
  var hash = 0
  for (ch in key) {
    hash = (hash * 31) + ch.code
  }
  val idx = (hash and 0x7FFFFFFF) % PictorialPalette.size
  return PictorialPalette[idx]
}

/**
 * Reusable colorful pictorial icon badge with duotone gradient squircle and corner accent dot.
 */
@Composable
fun PictorialIconBadge(
  icon: ImageVector,
  color: Color,
  modifier: Modifier = Modifier,
  size: Dp = 40.dp,
  iconSize: Dp = 20.dp,
  cornerRadius: Dp = 13.dp,
  contentDescription: String? = null,
) {
  val secondaryTone = PictorialPalette[((color.hashCode() and 0x7FFFFFFF) + 3) % PictorialPalette.size]
  Surface(
    shape = RoundedCornerShape(cornerRadius),
    color = Color.Transparent,
    border = BorderStroke(0.9.dp, color.copy(alpha = 0.30f)),
    modifier = modifier.size(size)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.linearGradient(
            colors = listOf(
              color.copy(alpha = 0.20f),
              secondaryTone.copy(alpha = 0.12f)
            )
          )
        ),
      contentAlignment = Alignment.Center
    ) {
      // Pictorial corner sparkle dot
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .offset(x = (-5).dp, y = 5.dp)
          .size(5.dp)
          .clip(CircleShape)
          .background(color.copy(alpha = 0.55f))
      )
      Icon(
        imageVector = icon,
        contentDescription = contentDescription,
        tint = color,
        modifier = Modifier.size(iconSize)
      )
    }
  }
}

@Composable
fun RemmiTheme(
  appearanceMode: AppearanceMode = AppearanceMode.LIGHT,
  cyberTheme: CyberTheme = CyberTheme.NORMAL_DEFAULT,
  cyberHudEnabled: Boolean = false,
  pureBlackOled: Boolean = false,
  browserFont: BrowserFont = BrowserFont.CHROME_SANS,
  content: @Composable () -> Unit,
) {
  val systemDark = isSystemInDarkTheme()
  val isLight = !pureBlackOled && when (appearanceMode) {
    AppearanceMode.LIGHT -> true
    AppearanceMode.DARK -> false
    AppearanceMode.SYSTEM -> !systemDark && cyberTheme.isLight
  }

  val activeFontFamily = browserFont.fontFamily

  val cyberColorScheme = if (isLight) {
    val bg = when (cyberTheme) {
      CyberTheme.CYBER_MATRIX -> Color(0xFFEEF5F0)
      CyberTheme.VERONICA -> Color(0xFFF9F1F4)
      CyberTheme.JARVIS -> Color(0xFFF0F4FA)
      CyberTheme.STARK_IND -> Color(0xFFFAF5EA)
      else -> Color(0xFFF7F3EB) // Warm Sand Notes Paper
    }
    val cardSurface = Color(0xFFFFFDF9)
    val cardSurfaceAlt = when (cyberTheme) {
      CyberTheme.CYBER_MATRIX -> Color(0xFFE3EFE7)
      CyberTheme.VERONICA -> Color(0xFFF3E5EB)
      CyberTheme.JARVIS -> Color(0xFFE4ECF7)
      else -> Color(0xFFF1ECE1)
    }
    val borderCol = when (cyberTheme) {
      CyberTheme.CYBER_MATRIX -> Color(0xFFD0E2D6)
      CyberTheme.VERONICA -> Color(0xFFE8D2DC)
      CyberTheme.JARVIS -> Color(0xFFD2DFEE)
      else -> Color(0xFFE5DEC9)
    }
    CyberColorScheme(
      primary = cyberTheme.primaryAccent,
      secondary = cyberTheme.secondaryAccent,
      tertiary = cyberTheme.tertiaryAccent,
      background = bg,
      backgroundDarker = cardSurfaceAlt,
      surface = cardSurface,
      surfaceLight = cardSurfaceAlt,
      surfaceBorder = borderCol,
      glow = Color.Transparent,
      textPrimary = Color(0xFF1F1C18),
      textSecondary = Color(0xFF5C554C),
      textMuted = Color(0xFF857D73),
      neonCyan = Color(0xFF0D9488),
      dangerRed = Color(0xFFDC2626),
      successGreen = Color(0xFF16A34A),
      warningYellow = Color(0xFFD97706),
      torPurple = Color(0xFF6E48AA),
      successContainer = Color(0xFFDCFCE7),
      dangerContainer = Color(0xFFFEE2E2),
      isLight = true,
      isNormalTheme = true,
    )
  } else {
    val bg = if (pureBlackOled) Color(0xFF000000) else Color(0xFF151619)
    val surface = if (pureBlackOled) Color(0xFF0C0D10) else Color(0xFF1F2126)
    val surfaceLight = if (pureBlackOled) Color(0xFF16181D) else Color(0xFF282B32)
    val border = if (pureBlackOled) Color(0xFF22252C) else Color(0xFF32363F)
    val primary = if (cyberTheme == CyberTheme.NORMAL_DEFAULT) Color(0xFFF08C4B) else cyberTheme.primaryAccent
    CyberColorScheme(
      primary = primary,
      secondary = cyberTheme.secondaryAccent,
      tertiary = cyberTheme.tertiaryAccent,
      background = bg,
      backgroundDarker = Color(0xFF0E0F12),
      surface = surface,
      surfaceLight = surfaceLight,
      surfaceBorder = border,
      glow = Color.Transparent,
      textPrimary = Color(0xFFF5F2EB),
      textSecondary = Color(0xFFB5AFA6),
      textMuted = Color(0xFF7E7970),
      neonCyan = Color(0xFF2DD4BF),
      dangerRed = Color(0xFFEF4444),
      successGreen = Color(0xFF22C55E),
      warningYellow = Color(0xFFF59E0B),
      torPurple = Color(0xFFA78BFA),
      successContainer = Color(0xFF064E3B),
      dangerContainer = Color(0xFF450A0A),
      isLight = false,
      isNormalTheme = true,
    )
  }

  val materialColorScheme = if (isLight) {
    lightColorScheme(
      primary = cyberColorScheme.primary,
      onPrimary = Color.White,
      secondary = cyberColorScheme.secondary,
      onSecondary = Color.White,
      tertiary = cyberColorScheme.tertiary,
      background = cyberColorScheme.background,
      onBackground = cyberColorScheme.textPrimary,
      surface = cyberColorScheme.surface,
      onSurface = cyberColorScheme.textPrimary,
      surfaceVariant = cyberColorScheme.surfaceLight,
      onSurfaceVariant = cyberColorScheme.textSecondary,
      error = cyberColorScheme.dangerRed,
      onError = Color.White,
    )
  } else {
    darkColorScheme(
      primary = cyberColorScheme.primary,
      onPrimary = Color(0xFF151619),
      secondary = cyberColorScheme.secondary,
      onSecondary = Color(0xFF151619),
      tertiary = cyberColorScheme.tertiary,
      background = cyberColorScheme.background,
      onBackground = cyberColorScheme.textPrimary,
      surface = cyberColorScheme.surface,
      onSurface = cyberColorScheme.textPrimary,
      surfaceVariant = cyberColorScheme.surfaceLight,
      onSurfaceVariant = cyberColorScheme.textSecondary,
      error = cyberColorScheme.dangerRed,
      onError = Color.White,
    )
  }

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = cyberColorScheme.background.toArgb()
        window.navigationBarColor = cyberColorScheme.surface.toArgb()
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = isLight
        insetsController.isAppearanceLightNavigationBars = isLight
      }
    }
  }

  CompositionLocalProvider(
    LocalCyberColors provides cyberColorScheme,
    LocalCyberFontFamily provides activeFontFamily,
  ) {
    MaterialTheme(
      colorScheme = materialColorScheme,
      typography = getBrowserTypography(activeFontFamily, true),
      content = content,
    )
  }
}
