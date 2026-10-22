package com.remmi.browser.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class CyberTheme(
  val id: String,
  val displayName: String,
  val subtitle: String,
  val primaryAccent: Color,
  val secondaryAccent: Color,
  val tertiaryAccent: Color,
  val glowColor: Color,
  val hexCode: String,
  val isLight: Boolean = false,
  val isNormalTheme: Boolean = false,
) {
  NORMAL_DEFAULT(
    id = "NORMAL_DEFAULT",
    displayName = "Clean Light",
    subtitle = "Crisp white surfaces & vibrant blue accents",
    primaryAccent = Color(0xFF1A73E8),
    secondaryAccent = Color(0xFF1E8E3E),
    tertiaryAccent = Color(0xFF9333EA),
    glowColor = Color(0x221A73E8),
    hexCode = "#1A73E8",
    isLight = true,
    isNormalTheme = true,
  ),
  MINIMAL_DARK(
    id = "MINIMAL_DARK",
    displayName = "Midnight Dark",
    subtitle = "Deep slate surfaces & electric blue accents",
    primaryAccent = Color(0xFF3B82F6),
    secondaryAccent = Color(0xFF22C55E),
    tertiaryAccent = Color(0xFFA855F7),
    glowColor = Color(0x333B82F6),
    hexCode = "#3B82F6",
    isLight = false,
    isNormalTheme = true,
  ),
  JARVIS(
    id = "JARVIS",
    displayName = "Ocean Cyan",
    subtitle = "Cool azure tones & teal highlights",
    primaryAccent = Color(0xFF0284C7),
    secondaryAccent = Color(0xFF0D9488),
    tertiaryAccent = Color(0xFF6366F1),
    glowColor = Color(0x280284C7),
    hexCode = "#0284C7",
    isLight = true,
    isNormalTheme = true,
  ),
  STARK_IND(
    id = "STARK_IND",
    displayName = "Solar Amber",
    subtitle = "Golden amber & warm sunset accents",
    primaryAccent = Color(0xFFD97706),
    secondaryAccent = Color(0xFFEA580C),
    tertiaryAccent = Color(0xFF16A34A),
    glowColor = Color(0x28D97706),
    hexCode = "#D97706",
    isLight = true,
    isNormalTheme = true,
  ),
  VERONICA(
    id = "VERONICA",
    displayName = "Pastel Berry",
    subtitle = "Soft rose pink & royal violet accents",
    primaryAccent = Color(0xFFE11D48),
    secondaryAccent = Color(0xFF9333EA),
    tertiaryAccent = Color(0xFFF59E0B),
    glowColor = Color(0x28E11D48),
    hexCode = "#E11D48",
    isLight = true,
    isNormalTheme = true,
  ),
  CYBER_MATRIX(
    id = "CYBER_MATRIX",
    displayName = "Emerald Mint",
    subtitle = "Fresh emerald green & botanical teal",
    primaryAccent = Color(0xFF16A34A),
    secondaryAccent = Color(0xFF0D9488),
    tertiaryAccent = Color(0xFF2563EB),
    glowColor = Color(0x2816A34A),
    hexCode = "#16A34A",
    isLight = true,
    isNormalTheme = true,
  );

  companion object {
    val LIGHT_MINIMAL: CyberTheme get() = NORMAL_DEFAULT
    val SOLAR_GOLD: CyberTheme get() = STARK_IND
    val PASTEL_DREAM: CyberTheme get() = VERONICA

    fun fromId(id: String?): CyberTheme {
      return when (id?.uppercase()) {
        "NORMAL_DEFAULT", "NORMAL DEFAULT", "MINIMAL_LIGHT", "NORMAL (LIGHT)", "DEFAULT", "LIGHT", "LIGHT_MINIMAL" -> NORMAL_DEFAULT
        "MINIMAL_DARK", "NORMAL_DARK", "NORMAL (DARK)", "DARK" -> MINIMAL_DARK
        "STARK_IND", "STARK", "SOLAR_GOLD" -> STARK_IND
        "VERONICA", "PASTEL_DREAM" -> VERONICA
        "CYBER_MATRIX", "MATRIX" -> CYBER_MATRIX
        "JARVIS" -> JARVIS
        else -> entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: NORMAL_DEFAULT
      }
    }
  }
}

data class CyberColorScheme(
  val primary: Color,
  val secondary: Color,
  val tertiary: Color,
  val background: Color,
  val backgroundDarker: Color,
  val surface: Color,
  val surfaceLight: Color,
  val surfaceBorder: Color,
  val glow: Color,
  val textPrimary: Color,
  val textSecondary: Color,
  val textMuted: Color,
  val neonCyan: Color = Color(0xFF0284C7),
  val dangerRed: Color = Color(0xFFDC2626),
  val successGreen: Color = Color(0xFF1E8E3E),
  val warningYellow: Color = Color(0xFFD97706),
  val torPurple: Color = Color(0xFF9333EA),
  val successContainer: Color = Color(0xFFE8F5E9),
  val dangerContainer: Color = Color(0xFFFEE2E2),
  val isLight: Boolean = true,
  val isNormalTheme: Boolean = true,
)

val LocalCyberColors = staticCompositionLocalOf {
  CyberColorScheme(
    primary = CyberTheme.NORMAL_DEFAULT.primaryAccent,
    secondary = CyberTheme.NORMAL_DEFAULT.secondaryAccent,
    tertiary = CyberTheme.NORMAL_DEFAULT.tertiaryAccent,
    background = Color(0xFFF8FAFC),
    backgroundDarker = Color(0xFFF1F5F9),
    surface = Color(0xFFFFFFFF),
    surfaceLight = Color(0xFFF1F5F9),
    surfaceBorder = Color(0xFFE2E8F0),
    glow = Color(0x221A73E8),
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textMuted = Color(0xFF64748B),
    isLight = true,
    isNormalTheme = true,
  )
}

val LocalCyberFontFamily = staticCompositionLocalOf<androidx.compose.ui.text.font.FontFamily> {
  androidx.compose.ui.text.font.FontFamily.SansSerif
}

object ThemeCyber {
  val colors: CyberColorScheme
    @Composable
    @ReadOnlyComposable
    get() = LocalCyberColors.current

  val fontFamily: androidx.compose.ui.text.font.FontFamily
    @Composable
    @ReadOnlyComposable
    get() = LocalCyberFontFamily.current
}
