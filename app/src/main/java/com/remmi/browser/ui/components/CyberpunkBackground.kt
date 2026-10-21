package com.remmi.browser.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.remmi.browser.ui.theme.ThemeCyber

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
 * Clean, static, zero-flicker Notes paper background surface.
 * Eliminates all continuous infinite canvas animations to guarantee 120fps butter-smooth scrolling.
 */
@Composable
fun CyberpunkBackground(
  backgroundType: String = BackgroundTypes.LIGHT_AURA_MESH,
  customWallpaperUri: String? = null,
  wallpaperDimLevel: Float = 0.0f,
  wallpaperScaleMode: String = "CROP",
  modifier: Modifier = Modifier,
) {
  val backgroundColor = ThemeCyber.colors.background
  val surfaceLight = ThemeCyber.colors.surfaceLight
  val isLight = ThemeCyber.colors.isLight
  val context = LocalContext.current

  val contentScale = when (wallpaperScaleMode.uppercase()) {
    "FIT" -> ContentScale.Fit
    "FILL" -> ContentScale.FillBounds
    else -> ContentScale.Crop
  }

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
    }
  }
}
