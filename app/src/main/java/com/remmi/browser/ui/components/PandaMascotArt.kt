package com.remmi.browser.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.remmi.browser.ui.theme.ThemeCyber
import kotlinx.coroutines.delay
import kotlin.math.sin

/**
 * Animated 3D-shaded cute Panda Mascot matching the Remmi Home screenshot.
 * Gently sways, bobs up and down, wiggles its ears, glances around, and blinks naturally.
 */
@Composable
fun PandaMascotArt(
  modifier: Modifier = Modifier,
  size: Dp = 110.dp,
  isDarkTheme: Boolean = false,
  isGhost: Boolean = false,
  accentColor: Color = ThemeCyber.colors.primary,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "panda_live_anim")

  // Smooth rocking / swaying angle ("hilta dulta")
  val swayAngle by infiniteTransition.animateFloat(
    initialValue = -6.5f,
    targetValue = 6.5f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "panda_sway"
  )

  // Smooth floating / bobbing motion
  val bobProgress by infiniteTransition.animateFloat(
    initialValue = -1f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "panda_bob"
  )

  // Ear wiggle phase
  val earPhase by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 6.283185f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 1800, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "panda_ear_phase"
  )

  // Eye glance left-right
  val eyeGlance by infiniteTransition.animateFloat(
    initialValue = -1f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = keyframes {
        durationMillis = 5200
        0f at 0
        0f at 1400
        -1f at 1900
        -1f at 2700
        1f at 3300
        1f at 4100
        0f at 4600
        0f at 5200
      },
      repeatMode = RepeatMode.Restart
    ),
    label = "panda_eye_glance"
  )

  // Periodic natural blink (1f = open, 0.08f = closed)
  val blinkScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = keyframes {
        durationMillis = 3800
        1f at 0
        1f at 3350
        0.08f at 3480
        1f at 3620
        1f at 3800
      },
      repeatMode = RepeatMode.Restart
    ),
    label = "panda_blink"
  )

  // Interactive tap bounce
  var isTapped by remember { mutableStateOf(false) }
  LaunchedEffect(isTapped) {
    if (isTapped) {
      delay(350)
      isTapped = false
    }
  }
  val tapScale by animateFloatAsState(
    targetValue = if (isTapped) 1.12f else 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "panda_tap_scale"
  )

  val badgeColor = if (isGhost) Color(0xFF9333EA) else accentColor

  Box(
    modifier = modifier
      .size(size)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = { isTapped = true }
      ),
    contentAlignment = Alignment.Center
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = this.size.width
      val h = this.size.height
      val cx = w * 0.5f
      val cy = h * 0.47f

      val bobOffsetY = bobProgress * (h * 0.032f)
      val shadowScale = 1f - (bobProgress * 0.08f)

      // 1. Soft 3D Ground Shadow Oval under the Panda
      val shadowWidth = w * 0.66f * shadowScale
      val shadowHeight = h * 0.11f * shadowScale
      drawOval(
        brush = Brush.radialGradient(
          colors = listOf(
            Color(0xFF475569).copy(alpha = if (isDarkTheme) 0.36f else 0.18f),
            Color.Transparent
          ),
          center = Offset(cx, h * 0.89f),
          radius = shadowWidth * 0.55f
        ),
        topLeft = Offset(cx - shadowWidth * 0.5f, h * 0.835f),
        size = Size(shadowWidth, shadowHeight)
      )

      // 2. Transform group for swaying, bobbing, and tap-bouncing Panda head
      withTransform({
        translate(left = swayAngle * (w * 0.004f), top = bobOffsetY)
        rotate(degrees = swayAngle, pivot = Offset(cx, cy + h * 0.12f))
        scale(scaleX = tapScale, scaleY = tapScale, pivot = Offset(cx, cy))
      }) {
        val earColor = Color(0xFF1B1F26)
        val earInnerColor = Color(0xFF2D333F)
        val earWiggleLeft = sin(earPhase) * (h * 0.012f)
        val earWiggleRight = -sin(earPhase) * (h * 0.012f)
        val earRadius = w * 0.145f

        // Left Ear
        val leftEarCenter = Offset(cx - w * 0.255f, cy - h * 0.235f + earWiggleLeft)
        drawCircle(
          color = earColor,
          radius = earRadius,
          center = leftEarCenter
        )
        drawCircle(
          color = earInnerColor,
          radius = earRadius * 0.55f,
          center = Offset(leftEarCenter.x + w * 0.01f, leftEarCenter.y + h * 0.01f)
        )

        // Right Ear
        val rightEarCenter = Offset(cx + w * 0.255f, cy - h * 0.235f + earWiggleRight)
        drawCircle(
          color = earColor,
          radius = earRadius,
          center = rightEarCenter
        )
        drawCircle(
          color = earInnerColor,
          radius = earRadius * 0.55f,
          center = Offset(rightEarCenter.x - w * 0.01f, rightEarCenter.y + h * 0.01f)
        )

        // 3. Plump 3D-Shaded White Panda Head
        val headWidth = w * 0.74f
        val headHeight = h * 0.66f
        val headLeft = cx - headWidth * 0.5f
        val headTop = cy - headHeight * 0.5f

        // Base 3D sphere shading gradient
        drawOval(
          brush = Brush.radialGradient(
            colors = listOf(
              Color(0xFFFFFFFF),
              Color(0xFFF8FAFC),
              Color(0xFFE2E8F0),
              Color(0xFFCBD5E1)
            ),
            center = Offset(cx - w * 0.06f, cy - h * 0.10f),
            radius = headWidth * 0.62f
          ),
          topLeft = Offset(headLeft, headTop),
          size = Size(headWidth, headHeight)
        )

        // Subtle outer rim definition
        drawOval(
          color = Color(0xFFCBD5E1).copy(alpha = 0.7f),
          topLeft = Offset(headLeft, headTop),
          size = Size(headWidth, headHeight),
          style = Stroke(width = w * 0.012f)
        )

        // 4. Soft Pink Rosy Cheeks
        val cheekColor = Color(0xFFF48FB1).copy(alpha = 0.68f)
        val cheekW = w * 0.115f
        val cheekH = h * 0.075f
        drawOval(
          color = cheekColor,
          topLeft = Offset(cx - w * 0.275f, cy + h * 0.035f),
          size = Size(cheekW, cheekH)
        )
        drawOval(
          color = cheekColor,
          topLeft = Offset(cx + w * 0.160f, cy + h * 0.035f),
          size = Size(cheekW, cheekH)
        )

        // 5. Tilted Black Oval Eye Patches
        val patchColor = Color(0xFF181C22)
        val patchW = w * 0.195f
        val patchH = h * 0.235f
        val leftPatchCenter = Offset(cx - w * 0.135f, cy - h * 0.015f)
        val rightPatchCenter = Offset(cx + w * 0.135f, cy - h * 0.015f)

        rotate(degrees = 20f, pivot = leftPatchCenter) {
          drawOval(
            color = patchColor,
            topLeft = Offset(leftPatchCenter.x - patchW * 0.5f, leftPatchCenter.y - patchH * 0.5f),
            size = Size(patchW, patchH)
          )
        }
        rotate(degrees = -20f, pivot = rightPatchCenter) {
          drawOval(
            color = patchColor,
            topLeft = Offset(rightPatchCenter.x - patchW * 0.5f, rightPatchCenter.y - patchH * 0.5f),
            size = Size(patchW, patchH)
          )
        }

        // 6. Expressive Eyes with Glance & Blink
        val glanceShiftX = eyeGlance * (w * 0.014f)
        val leftEyeCenter = Offset(leftPatchCenter.x + w * 0.01f + glanceShiftX, leftPatchCenter.y - h * 0.01f)
        val rightEyeCenter = Offset(rightPatchCenter.x - w * 0.01f + glanceShiftX, rightPatchCenter.y - h * 0.01f)

        if (blinkScale > 0.25f) {
          val sparkleR1 = w * 0.034f
          val sparkleR2 = w * 0.014f
          // Left eye highlights
          drawCircle(
            color = Color.White,
            radius = sparkleR1,
            center = Offset(leftEyeCenter.x - w * 0.015f, leftEyeCenter.y - h * 0.022f * blinkScale)
          )
          drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = sparkleR2,
            center = Offset(leftEyeCenter.x + w * 0.022f, leftEyeCenter.y + h * 0.022f * blinkScale)
          )

          // Right eye highlights
          drawCircle(
            color = Color.White,
            radius = sparkleR1,
            center = Offset(rightEyeCenter.x - w * 0.015f, rightEyeCenter.y - h * 0.022f * blinkScale)
          )
          drawCircle(
            color = Color.White.copy(alpha = 0.85f),
            radius = sparkleR2,
            center = Offset(rightEyeCenter.x + w * 0.022f, rightEyeCenter.y + h * 0.022f * blinkScale)
          )
        } else {
          // Cute happy closed-eye arcs during blink
          val lidStroke = Stroke(width = w * 0.022f, cap = StrokeCap.Round)
          drawLine(
            color = Color.White.copy(alpha = 0.9f),
            start = Offset(leftEyeCenter.x - w * 0.04f, leftEyeCenter.y),
            end = Offset(leftEyeCenter.x + w * 0.04f, leftEyeCenter.y),
            strokeWidth = lidStroke.width,
            cap = StrokeCap.Round
          )
          drawLine(
            color = Color.White.copy(alpha = 0.9f),
            start = Offset(rightEyeCenter.x - w * 0.04f, rightEyeCenter.y),
            end = Offset(rightEyeCenter.x + w * 0.04f, rightEyeCenter.y),
            strokeWidth = lidStroke.width,
            cap = StrokeCap.Round
          )
        }

        // 7. Cute Nose & Curved "w" Mouth
        val noseW = w * 0.085f
        val noseH = h * 0.055f
        val noseY = cy + h * 0.055f
        drawOval(
          color = patchColor,
          topLeft = Offset(cx - noseW * 0.5f, noseY - noseH * 0.5f),
          size = Size(noseW, noseH)
        )
        // Tiny nose shine
        drawOval(
          color = Color.White.copy(alpha = 0.65f),
          topLeft = Offset(cx - noseW * 0.22f, noseY - noseH * 0.32f),
          size = Size(noseW * 0.35f, noseH * 0.25f)
        )

        // Cute "w" smile path
        val mouthPath = Path().apply {
          val my = noseY + noseH * 0.55f
          moveTo(cx - w * 0.068f, my + h * 0.015f)
          quadraticTo(cx - w * 0.034f, my + h * 0.062f, cx, my + h * 0.012f)
          quadraticTo(cx + w * 0.034f, my + h * 0.062f, cx + w * 0.068f, my + h * 0.015f)
        }
        drawPath(
          path = mouthPath,
          color = patchColor,
          style = Stroke(width = w * 0.024f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // 8. Glowing Blue Collar Ring Badge at Bottom of Chin (exact match to screenshot)
        val badgeCenter = Offset(cx, headTop + headHeight - h * 0.055f)
        val badgeRadius = w * 0.052f
        // Outer soft glow
        drawCircle(
          color = badgeColor.copy(alpha = 0.28f),
          radius = badgeRadius * 1.45f,
          center = badgeCenter
        )
        // Blue circle ring
        drawCircle(
          color = badgeColor,
          radius = badgeRadius,
          center = badgeCenter
        )
        // White inner dot
        drawCircle(
          color = Color.White,
          radius = badgeRadius * 0.42f,
          center = badgeCenter
        )
      }
    }
  }
}
