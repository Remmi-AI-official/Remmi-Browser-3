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
    displayName = "Warm Sand",
    subtitle = "Warm paper tones & terracotta accents",
    primaryAccent = Color(0xFFD96B27),
    secondaryAccent = Color(0xFF2E7D5B),
    tertiaryAccent = Color(0xFF6E48AA),
    glowColor = Color(0x00000000),
    hexCode = "#D96B27",
    isLight = true,
    isNormalTheme = true,
  ),
  MINIMAL_DARK(
    id = "MINIMAL_DARK",
    displayName = "Charcoal Dark",
    subtitle = "Warm dark slate & amber accents",
    primaryAccent = Color(0xFFF08C4B),
    secondaryAccent = Color(0xFF4CC38A),
    tertiaryAccent = Color(0xFFA78BFA),
    glowColor = Color(0x00000000),
    hexCode = "#F08C4B",
    isLight = false,
    isNormalTheme = true,
  ),
  JARVIS(
    id = "JARVIS",
    displayName = "Ocean Blue",
    subtitle = "Crisp cobalt ink & cyan accents",
    primaryAccent = Color(0xFF2563EB),
    secondaryAccent = Color(0xFF0D9488),
    tertiaryAccent = Color(0xFF7C3AED),
    glowColor = Color(0x00000000),
    hexCode = "#2563EB",
    isLight = true,
    isNormalTheme = true,
  ),
  STARK_IND(
    id = "STARK_IND",
    displayName = "Amber Gold",
    subtitle = "Golden honey & warm terracotta",
    primaryAccent = Color(0xFFD97706),
    secondaryAccent = Color(0xFFEA580C),
    tertiaryAccent = Color(0xFF2E7D5B),
    glowColor = Color(0x00000000),
    hexCode = "#D97706",
    isLight = true,
    isNormalTheme = true,
  ),
  VERONICA(
    id = "VERONICA",
    displayName = "Rose Berry",
    subtitle = "Soft coral rose & plum accents",
    primaryAccent = Color(0xFFE11D48),
    secondaryAccent = Color(0xFF9333EA),
    tertiaryAccent = Color(0xFFD97706),
    glowColor = Color(0x00000000),
    hexCode = "#E11D48",
    isLight = true,
    isNormalTheme = true,
  ),
  CYBER_MATRIX(
    id = "CYBER_MATRIX",
    displayName = "Sage Mint",
    subtitle = "Botanical emerald & forest green",
    primaryAccent = Color(0xFF16A34A),
    secondaryAccent = Color(0xFF0D9488),
    tertiaryAccent = Color(0xFF2563EB),
    glowColor = Color(0x00000000),
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
  val neonCyan: Color = Color(0xFF0D9488),
  val dangerRed: Color = Color(0xFFDC2626),
  val successGreen: Color = Color(0xFF16A34A),
  val warningYellow: Color = Color(0xFFD97706),
  val torPurple: Color = Color(0xFF6E48AA),
  val successContainer: Color = Color(0xFFDCFCE7),
  val dangerContainer: Color = Color(0xFFFEE2E2),
  val isLight: Boolean = true,
  val isNormalTheme: Boolean = true,
)

val LocalCyberColors = staticCompositionLocalOf {
  CyberColorScheme(
    primary = CyberTheme.NORMAL_DEFAULT.primaryAccent,
    secondary = CyberTheme.NORMAL_DEFAULT.secondaryAccent,
    tertiary = CyberTheme.NORMAL_DEFAULT.tertiaryAccent,
    background = Color(0xFFF7F3EB),
    backgroundDarker = Color(0xFFEFE9DD),
    surface = Color(0xFFFFFDF9),
    surfaceLight = Color(0xFFF3EDE2),
    surfaceBorder = Color(0xFFE5DEC9),
    glow = Color(0x00000000),
    textPrimary = Color(0xFF1F1C18),
    textSecondary = Color(0xFF5C554C),
    textMuted = Color(0xFF857D73),
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
