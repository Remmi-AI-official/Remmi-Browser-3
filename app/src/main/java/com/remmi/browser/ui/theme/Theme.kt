package com.remmi.browser.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.remmi.browser.storage.AppearanceMode

/**
 * Palette of vivid pictorial icon colors.
 */
object PictorialPalette {
  val Coral = Color(0xFFEA580C)
  val Emerald = Color(0xFF1E8E3E)
  val Purple = Color(0xFF8E24AA)
  val Blue = Color(0xFF1A73E8)
  val Sky = Color(0xFF0284C7)
  val Amber = Color(0xFFF57C00)
  val Rose = Color(0xFFE11D48)
  val Teal = Color(0xFF00897B)
  val Indigo = Color(0xFF4F46E5)

  val all = listOf(Blue, Purple, Teal, Amber, Rose, Emerald, Sky, Coral, Indigo)
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

@Composable
fun PictorialIconBadge(
  icon: ImageVector,
  color: Color,
  modifier: Modifier = Modifier,
  size: Dp = 40.dp,
  iconSize: Dp = 20.dp,
  cornerRadius: Dp = 8.dp,
  contentDescription: String? = null,
) {
  val effectiveTint = when (color) {
    PictorialPalette.Rose -> ThemeCyber.colors.dangerRed
    PictorialPalette.Purple -> ThemeCyber.colors.torPurple
    else -> ThemeCyber.colors.primary
  }
  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = effectiveTint,
      modifier = Modifier.size(iconSize)
    )
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
    CyberColorScheme(
      primary = cyberTheme.primaryAccent,
      secondary = cyberTheme.secondaryAccent,
      tertiary = cyberTheme.tertiaryAccent,
      background = Color(0xFFF8FAFC),
      backgroundDarker = Color(0xFFF1F5F9),
      surface = Color(0xFFFFFFFF),
      surfaceLight = Color(0xFFF1F5F9),
      surfaceBorder = Color(0xFFE2E8F0),
      glow = cyberTheme.glowColor,
      textPrimary = Color(0xFF0F172A),
      textSecondary = Color(0xFF475569),
      textMuted = Color(0xFF64748B),
      neonCyan = Color(0xFF0284C7),
      dangerRed = Color(0xFFDC2626),
      successGreen = Color(0xFF1E8E3E),
      warningYellow = Color(0xFFF57C00),
      torPurple = Color(0xFF8E24AA),
      successContainer = Color(0xFFE8F5E9),
      dangerContainer = Color(0xFFFEE2E2),
      isLight = true,
      isNormalTheme = true,
    )
  } else {
    val bg = if (pureBlackOled) Color(0xFF000000) else Color(0xFF0F172A)
    val surface = if (pureBlackOled) Color(0xFF0A0D14) else Color(0xFF1E293B)
    val surfaceLight = if (pureBlackOled) Color(0xFF141923) else Color(0xFF334155)
    val border = if (pureBlackOled) Color(0xFF1E293B) else Color(0xFF334155)
    val primary = if (cyberTheme == CyberTheme.NORMAL_DEFAULT) Color(0xFF3B82F6) else cyberTheme.primaryAccent
    CyberColorScheme(
      primary = primary,
      secondary = cyberTheme.secondaryAccent,
      tertiary = cyberTheme.tertiaryAccent,
      background = bg,
      backgroundDarker = Color(0xFF090D16),
      surface = surface,
      surfaceLight = surfaceLight,
      surfaceBorder = border,
      glow = cyberTheme.glowColor,
      textPrimary = Color(0xFFF8FAFC),
      textSecondary = Color(0xFF94A3B8),
      textMuted = Color(0xFF64748B),
      neonCyan = Color(0xFF38BDF8),
      dangerRed = Color(0xFFEF4444),
      successGreen = Color(0xFF22C55E),
      warningYellow = Color(0xFFF59E0B),
      torPurple = Color(0xFFC084FC),
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
      onPrimary = Color(0xFF0F172A),
      secondary = cyberColorScheme.secondary,
      onSecondary = Color(0xFF0F172A),
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
  val bgArgb = cyberColorScheme.background.toArgb()
  val surfaceArgb = cyberColorScheme.surface.toArgb()
  val webCanvasArgb = if (isLight) Color.White.toArgb() else bgArgb

  if (!view.isInEditMode) {
    LaunchedEffect(bgArgb, surfaceArgb, isLight) {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        window.statusBarColor = bgArgb
        window.navigationBarColor = surfaceArgb
        window.decorView.setBackgroundColor(bgArgb)
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = isLight
        insetsController.isAppearanceLightNavigationBars = isLight
      }
      try {
        com.remmi.browser.engine.GeckoEngineManager.getInstance(view.context)
          .updateThemeBackgroundColor(webCanvasArgb, !isLight)
      } catch (_: Throwable) {}
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
