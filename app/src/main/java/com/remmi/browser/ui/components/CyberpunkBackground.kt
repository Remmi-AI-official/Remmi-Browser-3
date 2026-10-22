package com.remmi.browser.ui.components

import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.remmi.browser.ui.theme.ThemeCyber
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object BackgroundTypes {
  const val LIGHT_AURA_MESH = "LIGHT_AURA_MESH"
  const val LIGHT_FLOATING_ORBS = "LIGHT_FLOATING_ORBS"
  const val LIGHT_GEOMETRIC_DOTS = "LIGHT_GEOMETRIC_DOTS"
  const val LIGHT_CONSTELLATION = "LIGHT_CONSTELLATION"
  const val CYBERPUNK_GRID = "CYBERPUNK_GRID"
  const val MATRIX_RAIN = "MATRIX_RAIN"
  const val NEON_PARTICLES = "NEON_PARTICLES"
  const val DIGITAL_AURORA = "DIGITAL_AURORA"
  const val MINIMAL_GRADIENT = "MINIMAL_GRADIENT"
  const val CUSTOM_IMAGE = "CUSTOM_IMAGE"
}

/**
 * Live animated background canvas for the New Tab Page.
 * Reads animation phase strictly inside the DrawScope to avoid triggering parent recompositions.
 */
@Composable
fun CyberpunkBackground(
  backgroundType: String = BackgroundTypes.LIGHT_AURA_MESH,
  customWallpaperUri: String? = null,
  wallpaperDimLevel: Float = 0.0f,
  wallpaperScaleMode: String = "CROP",
  modifier: Modifier = Modifier,
) {
  val colors = ThemeCyber.colors
  val backgroundColor = colors.background
  val isLight = colors.isLight
  val primaryColor = colors.primary
  val secondaryColor = colors.secondary
  val tertiaryColor = colors.tertiary
  val context = LocalContext.current

  val contentScale = when (wallpaperScaleMode.uppercase()) {
    "FIT" -> ContentScale.Fit
    "FILL" -> ContentScale.FillBounds
    else -> ContentScale.Crop
  }

  val infiniteTransition = rememberInfiniteTransition(label = "live_bg_transition")
  val phase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = (2f * PI).toFloat(),
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 14000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "bg_phase"
  )
  val pulse by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 4500, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "bg_pulse"
  )

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(backgroundColor)
  ) {
    if (!customWallpaperUri.isNullOrBlank()) {
      val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
          .data(Uri.parse(customWallpaperUri))
          .crossfade(false)
          .build()
      )
      Image(
        painter = painter,
        contentDescription = "Custom Wallpaper",
        modifier = Modifier.fillMaxSize(),
        contentScale = contentScale,
      )

      if (wallpaperDimLevel > 0.01f) {
        val dimFactor = wallpaperDimLevel.coerceIn(0f, 1f)
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              if (isLight) Color.White.copy(alpha = dimFactor * 0.55f)
              else Color.Black.copy(alpha = dimFactor * 0.65f)
            )
        )
      }
    } else {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val t = phase
        val p = pulse

        when (backgroundType) {
          BackgroundTypes.LIGHT_AURA_MESH -> {
            // Soft Aura Waves (matches screenshot pastel blue, lavender, and mint ambient mesh)
            val blueAura = if (isLight) Color(0xFF93C5FD).copy(alpha = 0.36f) else primaryColor.copy(alpha = 0.20f)
            val lavenderAura = if (isLight) Color(0xFFD8B4FE).copy(alpha = 0.32f) else tertiaryColor.copy(alpha = 0.18f)
            val mintAura = if (isLight) Color(0xFFA7F3D0).copy(alpha = 0.28f) else secondaryColor.copy(alpha = 0.16f)
            val skyAura = if (isLight) Color(0xFFBAE6FD).copy(alpha = 0.30f) else Color(0xFF38BDF8).copy(alpha = 0.15f)

            val c1 = Offset(
              x = w * (0.28f + 0.14f * cos(t)),
              y = h * (0.18f + 0.08f * sin(t))
            )
            val c2 = Offset(
              x = w * (0.78f + 0.12f * sin(t * 0.8f)),
              y = h * (0.42f + 0.10f * cos(t * 0.8f))
            )
            val c3 = Offset(
              x = w * (0.22f + 0.12f * sin(t + 1.4f)),
              y = h * (0.68f + 0.09f * cos(t + 1.4f))
            )
            val c4 = Offset(
              x = w * (0.72f + 0.10f * cos(t + 2.5f)),
              y = h * (0.82f + 0.07f * sin(t + 2.5f))
            )

            drawCircle(
              brush = Brush.radialGradient(
                colors = listOf(blueAura, Color.Transparent),
                center = c1,
                radius = w * (0.68f + 0.06f * p)
              ),
              radius = w * 0.74f,
              center = c1
            )
            drawCircle(
              brush = Brush.radialGradient(
                colors = listOf(lavenderAura, Color.Transparent),
                center = c2,
                radius = w * (0.65f + 0.05f * (1f - p))
              ),
              radius = w * 0.70f,
              center = c2
            )
            drawCircle(
              brush = Brush.radialGradient(
                colors = listOf(mintAura, Color.Transparent),
                center = c3,
                radius = w * 0.62f
              ),
              radius = w * 0.62f,
              center = c3
            )
            drawCircle(
              brush = Brush.radialGradient(
                colors = listOf(skyAura, Color.Transparent),
                center = c4,
                radius = w * 0.58f
              ),
              radius = w * 0.58f,
              center = c4
            )
          }

          BackgroundTypes.LIGHT_FLOATING_ORBS -> {
            // Floating Pastel Orbs
            val orbColors = if (isLight) {
              listOf(
                Color(0xFF60A5FA).copy(alpha = 0.28f),
                Color(0xFFC084FC).copy(alpha = 0.25f),
                Color(0xFF34D399).copy(alpha = 0.24f),
                Color(0xFFF472B6).copy(alpha = 0.22f),
                Color(0xFF38BDF8).copy(alpha = 0.26f),
                Color(0xFFFBBF24).copy(alpha = 0.20f)
              )
            } else {
              listOf(
                primaryColor.copy(alpha = 0.22f),
                tertiaryColor.copy(alpha = 0.20f),
                secondaryColor.copy(alpha = 0.18f),
                Color(0xFFEC4899).copy(alpha = 0.18f),
                Color(0xFF38BDF8).copy(alpha = 0.20f),
                Color(0xFFF59E0B).copy(alpha = 0.16f)
              )
            }

            for (i in 0 until 6) {
              val seed = i * 1.15f
              val cx = w * (0.18f + 0.64f * ((sin(t + seed) + 1f) * 0.5f))
              val cy = h * (0.12f + 0.76f * ((cos(t * 0.7f + seed * 1.3f) + 1f) * 0.5f))
              val r = w * (0.24f + (i % 3) * 0.08f + 0.03f * sin(t + i))
              val col = orbColors[i % orbColors.size]
              drawCircle(
                brush = Brush.radialGradient(
                  colors = listOf(col, col.copy(alpha = col.alpha * 0.35f), Color.Transparent),
                  center = Offset(cx, cy),
                  radius = r
                ),
                radius = r,
                center = Offset(cx, cy)
              )
            }
          }

          BackgroundTypes.LIGHT_GEOMETRIC_DOTS -> {
            // Pulsing Dot Grid
            val cols = 11
            val rows = 20
            val spacingX = w / cols
            val spacingY = h / rows
            val baseDotColor = if (isLight) primaryColor.copy(alpha = 0.22f) else primaryColor.copy(alpha = 0.28f)

            for (r in 0..rows) {
              for (c in 0..cols) {
                val wave = sin(t + c * 0.45f + r * 0.35f)
                val radius = (2.2f + 1.8f * ((wave + 1f) * 0.5f)) * density
                val alphaScale = 0.35f + 0.65f * ((wave + 1f) * 0.5f)
                drawCircle(
                  color = baseDotColor.copy(alpha = baseDotColor.alpha * alphaScale),
                  radius = radius,
                  center = Offset(c * spacingX + spacingX * 0.5f, r * spacingY + spacingY * 0.5f)
                )
              }
            }
          }

          BackgroundTypes.LIGHT_CONSTELLATION -> {
            // Starry Constellation (connected drifting nodes)
            val nodeCount = 14
            val nodes = Array(nodeCount) { i ->
              val seedX = (i * 73 % 97) / 97f
              val seedY = (i * 41 % 89) / 89f
              val nx = w * (0.08f + 0.84f * ((seedX + 0.08f * sin(t + i * 0.9f)).coerceIn(0f, 1f)))
              val ny = h * (0.08f + 0.84f * ((seedY + 0.08f * cos(t + i * 1.1f)).coerceIn(0f, 1f)))
              Offset(nx, ny)
            }
            val maxDist = w * 0.38f
            val lineColor = if (isLight) primaryColor.copy(alpha = 0.16f) else primaryColor.copy(alpha = 0.24f)
            val nodeColor = if (isLight) primaryColor.copy(alpha = 0.42f) else primaryColor.copy(alpha = 0.55f)

            for (i in 0 until nodeCount) {
              for (j in i + 1 until nodeCount) {
                val dx = nodes[i].x - nodes[j].x
                val dy = nodes[i].y - nodes[j].y
                val dist = sqrt(dx * dx + dy * dy)
                if (dist < maxDist) {
                  val ratio = 1f - (dist / maxDist)
                  drawLine(
                    color = lineColor.copy(alpha = lineColor.alpha * ratio),
                    start = nodes[i],
                    end = nodes[j],
                    strokeWidth = 1.4f * density
                  )
                }
              }
            }
            for (pt in nodes) {
              drawCircle(
                color = nodeColor,
                radius = 3.5f * density,
                center = pt
              )
            }
          }

          BackgroundTypes.DIGITAL_AURORA -> {
            // Flowing Aurora Ribbons
            val auroraColors = listOf(
              primaryColor.copy(alpha = if (isLight) 0.18f else 0.24f),
              tertiaryColor.copy(alpha = if (isLight) 0.16f else 0.22f),
              secondaryColor.copy(alpha = if (isLight) 0.15f else 0.20f)
            )
            for (band in 0..2) {
              val path = Path()
              val baseY = h * (0.24f + band * 0.24f)
              path.moveTo(0f, baseY)
              val steps = 16
              for (s in 0..steps) {
                val x = (w / steps) * s
                val y = baseY + sin(t + s * 0.45f + band * 1.3f) * (h * 0.055f)
                if (s == 0) path.moveTo(x, y) else path.lineTo(x, y)
              }
              path.lineTo(w, h)
              path.lineTo(0f, h)
              path.close()
              drawPath(
                path = path,
                brush = Brush.verticalGradient(
                  colors = listOf(auroraColors[band], Color.Transparent),
                  startY = baseY - h * 0.06f,
                  endY = baseY + h * 0.28f
                )
              )
            }
          }

          BackgroundTypes.NEON_PARTICLES -> {
            // Drifting Glowing Particles
            val count = 18
            for (i in 0 until count) {
              val seed = i * 0.77f
              val px = w * ((i * 37 % 100) / 100f + 0.05f * sin(t + seed)).coerceIn(0.04f, 0.96f)
              val rawY = ((i * 53 % 100) / 100f - (t / (2f * PI.toFloat())) * 0.35f) % 1f
              val py = h * (if (rawY < 0f) rawY + 1f else rawY)
              val r = (3f + (i % 4) * 1.8f) * density
              val col = when (i % 3) {
                0 -> primaryColor
                1 -> tertiaryColor
                else -> secondaryColor
              }.copy(alpha = if (isLight) 0.28f else 0.42f)
              drawCircle(
                brush = Brush.radialGradient(
                  colors = listOf(col, Color.Transparent),
                  center = Offset(px, py),
                  radius = r * 2.4f
                ),
                radius = r * 2.4f,
                center = Offset(px, py)
              )
            }
          }

          BackgroundTypes.CYBERPUNK_GRID -> {
            // Perspective Horizon Grid
            val horizonY = h * 0.55f
            val gridColor = primaryColor.copy(alpha = if (isLight) 0.15f else 0.24f)
            val vLines = 10
            for (i in -vLines..vLines) {
              val topX = w * 0.5f + i * (w * 0.04f)
              val bottomX = w * 0.5f + i * (w * 0.16f)
              drawLine(
                color = gridColor,
                start = Offset(topX, horizonY),
                end = Offset(bottomX, h),
                strokeWidth = 1.2f * density
              )
            }
            val progressOffset = (t / (2f * PI.toFloat()))
            for (r in 0..8) {
              val frac = ((r + progressOffset) / 9f).coerceIn(0f, 1f)
              val y = horizonY + (h - horizonY) * (frac * frac)
              drawLine(
                color = gridColor.copy(alpha = gridColor.alpha * frac),
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1.2f * density
              )
            }
          }

          BackgroundTypes.MATRIX_RAIN -> {
            // Digital Falling Streams
            val columns = 14
            val colWidth = w / columns
            val streamColor = secondaryColor.copy(alpha = if (isLight) 0.22f else 0.34f)
            val progress = t / (2f * PI.toFloat())
            for (c in 0 until columns) {
              val speed = 0.6f + (c % 4) * 0.25f
              val headY = ((progress * speed + (c * 0.19f)) % 1f) * h
              val tailLen = h * 0.18f
              drawLine(
                brush = Brush.verticalGradient(
                  colors = listOf(Color.Transparent, streamColor),
                  startY = (headY - tailLen).coerceAtLeast(0f),
                  endY = headY
                ),
                start = Offset(c * colWidth + colWidth * 0.5f, (headY - tailLen).coerceAtLeast(0f)),
                end = Offset(c * colWidth + colWidth * 0.5f, headY),
                strokeWidth = 2.2f * density
              )
            }
          }

          BackgroundTypes.MINIMAL_GRADIENT -> {
            drawRect(
              brush = Brush.verticalGradient(
                colors = listOf(
                  primaryColor.copy(alpha = if (isLight) 0.06f else 0.10f),
                  backgroundColor
                )
              )
            )
          }

          else -> {
            // Fallback to Soft Aura Waves
            val blueAura = if (isLight) Color(0xFF93C5FD).copy(alpha = 0.32f) else primaryColor.copy(alpha = 0.18f)
            val lavenderAura = if (isLight) Color(0xFFD8B4FE).copy(alpha = 0.28f) else tertiaryColor.copy(alpha = 0.16f)
            drawCircle(
              brush = Brush.radialGradient(
                colors = listOf(blueAura, Color.Transparent),
                center = Offset(w * 0.3f, h * 0.2f),
                radius = w * 0.7f
              ),
              radius = w * 0.7f,
              center = Offset(w * 0.3f, h * 0.2f)
            )
            drawCircle(
              brush = Brush.radialGradient(
                colors = listOf(lavenderAura, Color.Transparent),
                center = Offset(w * 0.75f, h * 0.45f),
                radius = w * 0.65f
              ),
              radius = w * 0.65f,
              center = Offset(w * 0.75f, h * 0.45f)
            )
          }
        }
      }
    }
  }
}
