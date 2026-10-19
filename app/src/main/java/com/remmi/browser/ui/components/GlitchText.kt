package com.remmi.browser.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.remmi.browser.ui.theme.CyberMonoFamily
import com.remmi.browser.ui.theme.ThemeCyber

@Composable
fun GlitchText(
  text: String,
  modifier: Modifier = Modifier,
  fontSize: TextUnit = 20.sp,
  color: Color = ThemeCyber.colors.textPrimary,
  glitchEnabled: Boolean = false,
  fontWeight: FontWeight = FontWeight.Bold,
  fontFamily: FontFamily = CyberMonoFamily,
) {
  Text(
    text = text,
    modifier = modifier,
    fontSize = fontSize,
    color = color,
    fontWeight = fontWeight,
    fontFamily = fontFamily,
  )
}
