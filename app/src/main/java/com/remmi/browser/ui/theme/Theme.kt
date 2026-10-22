package com.remmi.browser.ui.theme

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
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
  cornerRadius: Dp = 10.dp,
  contentDescription: String? = null,
) {
  Box(
    modifier = modifier
      .size(size)
      .clip(RoundedCornerShape(cornerRadius))
      .background(color.copy(alpha = 0.15f))
      .border(0.8.dp, color.copy(alpha = 0.35f), RoundedCornerShape(cornerRadius)),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = contentDescription,
      tint = color,
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

  val activeFontFamily = if (cyberHudEnabled && browserFont == BrowserFont.CHROME_SANS) {
    CyberMonoFamily
  } else {
    browserFont.fontFamily
  }

  val isCustomAccent = cyberTheme != CyberTheme.NORMAL_DEFAULT
  val cyberColorScheme = if (isLight) {
    val primary = cyberTheme.primaryAccent
    val secondary = cyberTheme.secondaryAccent
    val bgTint = if (cyberHudEnabled) 0.07f else if (isCustomAccent) 0.035f else 0f
    val surfTint = if (cyberHudEnabled) 0.045f else if (isCustomAccent) 0.015f else 0f
    val surfLightTint = if (cyberHudEnabled) 0.13f else if (isCustomAccent) 0.065f else 0f
    val borderTint = if (cyberHudEnabled) 0.45f else if (isCustomAccent) 0.25f else 0f

    CyberColorScheme(
      primary = primary,
      secondary = secondary,
      tertiary = cyberTheme.tertiaryAccent,
      background = lerp(Color(0xFFF8FAFC), primary, bgTint),
      backgroundDarker = lerp(Color(0xFFF1F5F9), primary, bgTint + 0.03f),
      surface = lerp(Color(0xFFFFFFFF), primary, surfTint),
      surfaceLight = lerp(Color(0xFFF1F5F9), primary, surfLightTint),
      surfaceBorder = lerp(Color(0xFFE2E8F0), primary, borderTint),
      glow = cyberTheme.glowColor,
      textPrimary = Color(0xFF0F172A),
      textSecondary = Color(0xFF475569),
      textMuted = Color(0xFF64748B),
      neonCyan = if (isCustomAccent) primary else Color(0xFF0284C7),
      dangerRed = Color(0xFFDC2626),
      successGreen = Color(0xFF1E8E3E),
      warningYellow = Color(0xFFF57C00),
      torPurple = Color(0xFF8E24AA),
      successContainer = Color(0xFFE8F5E9),
      dangerContainer = Color(0xFFFEE2E2),
      isLight = true,
      isNormalTheme = !cyberHudEnabled,
    )
  } else {
    val primary = when (cyberTheme) {
      CyberTheme.NORMAL_DEFAULT -> if (cyberHudEnabled) Color(0xFF00F0FF) else Color(0xFF3B82F6)
      CyberTheme.MINIMAL_DARK -> Color(0xFF818CF8)
      CyberTheme.JARVIS -> Color(0xFF22D3EE)
      CyberTheme.STARK_IND -> Color(0xFFFBBF24)
      CyberTheme.VERONICA -> Color(0xFFF472B6)
      CyberTheme.CYBER_MATRIX -> Color(0xFF34D399)
    }
    val secondary = when (cyberTheme) {
      CyberTheme.NORMAL_DEFAULT -> if (cyberHudEnabled) Color(0xFF38BDF8) else cyberTheme.secondaryAccent
      else -> lerp(cyberTheme.secondaryAccent, Color.White, 0.22f)
    }
    val surfTint = if (cyberHudEnabled) 0.14f else if (isCustomAccent) 0.08f else 0f
    val surfLightTint = if (cyberHudEnabled) 0.22f else if (isCustomAccent) 0.13f else 0f
    val borderTint = if (cyberHudEnabled) 0.55f else if (isCustomAccent) 0.32f else 0f

    val bg = if (pureBlackOled) {
      Color(0xFF000000)
    } else {
      lerp(Color(0xFF0B1120), primary, if (cyberHudEnabled) 0.10f else if (isCustomAccent) 0.05f else 0f)
    }
    val bgDarker = if (pureBlackOled) {
      Color(0xFF000000)
    } else {
      lerp(Color(0xFF070B14), primary, if (cyberHudEnabled) 0.08f else 0.03f)
    }
    val surface = if (pureBlackOled) {
      lerp(Color(0xFF090C12), primary, surfTint)
    } else {
      lerp(Color(0xFF1E293B), primary, surfTint)
    }
    val surfaceLight = if (pureBlackOled) {
      lerp(Color(0xFF131822), primary, surfLightTint)
    } else {
      lerp(Color(0xFF334155), primary, surfLightTint)
    }
    val border = if (pureBlackOled) {
      lerp(Color(0xFF1E293B), primary, borderTint)
    } else {
      lerp(Color(0xFF334155), primary, borderTint)
    }

    CyberColorScheme(
      primary = primary,
      secondary = secondary,
      tertiary = cyberTheme.tertiaryAccent,
      background = bg,
      backgroundDarker = bgDarker,
      surface = surface,
      surfaceLight = surfaceLight,
      surfaceBorder = border,
      glow = primary.copy(alpha = if (cyberHudEnabled) 0.28f else 0.16f),
      textPrimary = Color(0xFFF8FAFC),
      textSecondary = Color(0xFFCBD5E1),
      textMuted = Color(0xFF94A3B8),
      neonCyan = primary,
      dangerRed = Color(0xFFEF4444),
      successGreen = Color(0xFF22C55E),
      warningYellow = Color(0xFFF59E0B),
      torPurple = Color(0xFFC084FC),
      successContainer = Color(0xFF064E3B),
      dangerContainer = Color(0xFF450A0A),
      isLight = false,
      isNormalTheme = !cyberHudEnabled,
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
        val forceWebDark = com.remmi.browser.storage.SettingsRepository
          .getInstance(view.context).settings.value.darkThemeForAllWebPages
        com.remmi.browser.engine.GeckoEngineManager.getInstance(view.context)
          .updateThemeBackgroundColor(webCanvasArgb, !isLight || forceWebDark)
      } catch (_: Throwable) {}
    }
  }

  CompositionLocalProvider(
    LocalCyberColors provides cyberColorScheme,
    LocalCyberFontFamily provides activeFontFamily,
  ) {
    MaterialTheme(
      colorScheme = materialColorScheme,
      typography = getBrowserTypography(activeFontFamily, !cyberHudEnabled),
      content = content,
    )
  }
}
