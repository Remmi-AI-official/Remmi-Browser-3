package com.remmi.browser.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.remmi.browser.ui.theme.ThemeCyber

/**
 * Static, zero-flicker colorful pictorial illustration of the Remmi mascot and privacy shield.
 * Designed in a warm editorial Notes illustration style with zero continuous frame invalidation.
 */
@Composable
fun PandaMascotArt(
  modifier: Modifier = Modifier,
  size: Dp = 120.dp,
  isDarkTheme: Boolean = false,
  isGhost: Boolean = false,
  accentColor: Color = ThemeCyber.colors.primary,
) {
  val secondaryAccent = if (isGhost) Color(0xFF6E48AA) else Color(0xFF2E7D5B)
  val warmGold = Color(0xFFD97706)
  val cardCream = Color(0xFFFFFBF4)
  val borderWarm = Color(0xFFE6DEC8)

  Box(
    modifier = modifier.size(size),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = this.size.width
      val h = this.size.height
      val cx = w / 2f
      val cy = h / 2f

      // 1. Soft colorful pictorial squircle backdrop
      drawRoundRect(
        brush = Brush.linearGradient(
          colors = listOf(
            accentColor.copy(alpha = 0.18f),
            secondaryAccent.copy(alpha = 0.16f),
            warmGold.copy(alpha = 0.14f)
          ),
          start = Offset(0f, 0f),
          end = Offset(w, h)
        ),
        topLeft = Offset(w * 0.06f, h * 0.06f),
        size = Size(w * 0.88f, h * 0.88f),
        cornerRadius = CornerRadius(w * 0.28f, w * 0.28f)
      )

      // 2. Inner warm note medallion
      drawCircle(
        color = cardCream,
        radius = w * 0.38f,
        center = Offset(cx, cy)
      )
      drawCircle(
        color = borderWarm,
        radius = w * 0.38f,
        center = Offset(cx, cy),
        style = Stroke(width = w * 0.018f)
      )

      // 3. Decorative colorful pictorial dots (Notes sticker aesthetic)
      drawCircle(
        color = accentColor,
        radius = w * 0.045f,
        center = Offset(cx - w * 0.30f, cy - h * 0.28f)
      )
      drawCircle(
        color = secondaryAccent,
        radius = w * 0.038f,
        center = Offset(cx + w * 0.31f, cy - h * 0.24f)
      )
      drawCircle(
        color = warmGold,
        radius = w * 0.04f,
        center = Offset(cx - w * 0.29f, cy + h * 0.26f)
      )

      // 4. Panda Ears (Warm Charcoal)
      val charcoal = Color(0xFF23201D)
      val earRadius = w * 0.095f
      drawCircle(
        color = charcoal,
        radius = earRadius,
        center = Offset(cx - w * 0.17f, cy - h * 0.17f)
      )
      drawCircle(
        color = accentColor.copy(alpha = 0.65f),
        radius = earRadius * 0.52f,
        center = Offset(cx - w * 0.17f, cy - h * 0.17f)
      )
      drawCircle(
        color = charcoal,
        radius = earRadius,
        center = Offset(cx + w * 0.17f, cy - h * 0.17f)
      )
      drawCircle(
        color = accentColor.copy(alpha = 0.65f),
        radius = earRadius * 0.52f,
        center = Offset(cx + w * 0.17f, cy - h * 0.17f)
      )

      // 5. Cozy Scarf / Collar in vibrant accent color
      val scarfPath = Path().apply {
        moveTo(cx - w * 0.20f, cy + h * 0.12f)
        quadraticTo(cx, cy + h * 0.26f, cx + w * 0.20f, cy + h * 0.12f)
        lineTo(cx + w * 0.24f, cy + h * 0.28f)
        quadraticTo(cx, cy + h * 0.36f, cx - w * 0.24f, cy + h * 0.28f)
        close()
      }
      drawPath(path = scarfPath, color = accentColor)

      // 6. Panda Head
      drawOval(
        color = Color.White,
        topLeft = Offset(cx - w * 0.22f, cy - h * 0.18f),
        size = Size(w * 0.44f, h * 0.36f)
      )
      drawOval(
        color = charcoal,
        topLeft = Offset(cx - w * 0.22f, cy - h * 0.18f),
        size = Size(w * 0.44f, h * 0.36f),
        style = Stroke(width = w * 0.022f)
      )

      // 7. Eye Patches & Friendly Eyes
      drawOval(
        color = charcoal,
        topLeft = Offset(cx - w * 0.15f, cy - h * 0.06f),
        size = Size(w * 0.11f, h * 0.09f)
      )
      drawOval(
        color = charcoal,
        topLeft = Offset(cx + w * 0.04f, cy - h * 0.06f),
        size = Size(w * 0.11f, h * 0.09f)
      )
      // Eye sparkles
      drawCircle(
        color = Color.White,
        radius = w * 0.02f,
        center = Offset(cx - w * 0.09f, cy - h * 0.02f)
      )
      drawCircle(
        color = Color.White,
        radius = w * 0.02f,
        center = Offset(cx + w * 0.09f, cy - h * 0.02f)
      )

      // Rosy cheeks
      drawOval(
        color = Color(0xFFF48FB1).copy(alpha = 0.55f),
        topLeft = Offset(cx - w * 0.17f, cy + h * 0.03f),
        size = Size(w * 0.07f, h * 0.04f)
      )
      drawOval(
        color = Color(0xFFF48FB1).copy(alpha = 0.55f),
        topLeft = Offset(cx + w * 0.10f, cy + h * 0.03f),
        size = Size(w * 0.07f, h * 0.04f)
      )

      // Nose & Smile
      drawOval(
        color = charcoal,
        topLeft = Offset(cx - w * 0.035f, cy + h * 0.02f),
        size = Size(w * 0.07f, h * 0.045f)
      )

      // 8. Colorful Pictorial Shield Badge (Bottom Right)
      val sx = cx + w * 0.22f
      val sy = cy + h * 0.20f
      val sr = w * 0.12f
      val shieldPath = Path().apply {
        moveTo(sx, sy - sr)
        lineTo(sx + sr * 0.85f, sy - sr * 0.55f)
        lineTo(sx + sr * 0.85f, sy + sr * 0.15f)
        quadraticTo(sx + sr * 0.85f, sy + sr * 0.85f, sx, sy + sr * 1.1f)
        quadraticTo(sx - sr * 0.85f, sy + sr * 0.85f, sx - sr * 0.85f, sy + sr * 0.15f)
        lineTo(sx - sr * 0.85f, sy - sr * 0.55f)
        close()
      }
      drawPath(path = shieldPath, color = secondaryAccent)
      drawPath(
        path = shieldPath,
        color = Color.White,
        style = Stroke(width = w * 0.02f, join = StrokeJoin.Round)
      )
      // Checkmark inside shield
      val checkPath = Path().apply {
        moveTo(sx - sr * 0.35f, sy)
        lineTo(sx - sr * 0.08f, sy + sr * 0.28f)
        lineTo(sx + sr * 0.40f, sy - sr * 0.25f)
      }
      drawPath(
        path = checkPath,
        color = Color.White,
        style = Stroke(width = w * 0.024f, cap = StrokeCap.Round, join = StrokeJoin.Round)
      )
    }
  }
}
