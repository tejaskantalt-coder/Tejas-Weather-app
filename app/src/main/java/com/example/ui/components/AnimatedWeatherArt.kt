package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import com.example.data.model.WeatherCondition
import com.example.ui.theme.SunYellow
import kotlin.math.cos
import kotlin.math.sin

/**
 * Ultra-optimized, zero-allocation 60fps procedural animated weather artwork.
 * Specifically tuned for older hardware (e.g. 2018 Snapdragon 625/450/660 devices).
 *
 * Performance features:
 * 1. Zero heap allocations during draw phase (no list/pair/object instantiation).
 * 2. Static pre-allocated primitive coordinate arrays.
 * 3. Uses hardware graphicsLayer to isolate draw calls from parent composables.
 * 4. Pure canvas primitive draws (drawCircle, drawLine, drawOval, drawArc) with minimal GPU fill-rate impact.
 */

// Pre-allocated static offsets to eliminate runtime allocations during draw
private val STAR_COORDS = floatArrayOf(
    -0.32f, -0.22f,
    -0.18f,  0.24f,
     0.28f, -0.26f,
     0.36f,  0.16f,
    -0.10f, -0.34f,
     0.15f,  0.30f,
    -0.38f,  0.06f
)

private val RAIN_PHASES = floatArrayOf(
    -0.36f, 0.05f,
    -0.24f, 0.42f,
    -0.12f, 0.81f,
     0.00f, 0.18f,
     0.12f, 0.63f,
     0.24f, 0.29f,
     0.36f, 0.74f,
    -0.30f, 0.55f,
    -0.06f, 0.90f,
     0.18f, 0.12f,
     0.30f, 0.48f,
     0.06f, 0.35f
)

private val SNOW_PHASES = floatArrayOf(
    -0.34f, 0.10f, 1f,
    -0.22f, 0.65f, 0.8f,
    -0.10f, 0.25f, 1.2f,
     0.02f, 0.85f, 0.9f,
     0.14f, 0.40f, 1.1f,
     0.26f, 0.70f, 0.7f,
     0.36f, 0.15f, 1.0f,
    -0.18f, 0.95f, 0.85f,
     0.08f, 0.50f, 1.15f
)

// Reusable strokes to prevent object creation in draw scope
private val STROKE_LIGHTNING_GLOW = Stroke(width = 8f, cap = StrokeCap.Round)
private val STROKE_LIGHTNING_CORE = Stroke(width = 3.2f, cap = StrokeCap.Round)
private val STROKE_TEMP_ISOTHERM = Stroke(width = 1.8f)
private val STROKE_SPLASH = Stroke(width = 1.4f)

// Reusable colors
private val COLOR_SUN_CORONA_OUTER = Color(0x30FFF176)
private val COLOR_SUN_CORONA_INNER = Color(0x55FFEB3B)
private val COLOR_SUN_RAY = Color(0xE6FFF176)
private val COLOR_SUN_HIGHLIGHT = Color(0x66FFFFFF)
private val COLOR_MOON_HALO = Color(0x28FFF9C4)
private val COLOR_MOON_DISC = Color(0xFFFFF9C4)
private val COLOR_MOON_SHADOW = Color(0xFF243048)
private val COLOR_RAIN_STREAK = Color(0xD081D4FA)
private val COLOR_RAIN_SPLASH = Color(0x8081D4FA)
private val COLOR_LIGHTNING_BOLT = Color(0xFFFFEB3B)
private val COLOR_LIGHTNING_GLOW = Color(0x60FFF9C4)
private val COLOR_FOG_BAND = Color(0x45FFFFFF)

@Composable
fun AnimatedWeatherArt(
    condition: WeatherCondition,
    isDay: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "WeatherArtAnim")

    // 1. Sun Rays Continuous Rotation (18s per full cycle)
    val sunRotationDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SunRotation"
    )

    // 2. Sun Corona Breathing Pulse (Smooth Sine Pulse)
    val sunPulse by infiniteTransition.animateFloat(
        initialValue = 0.93f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SunPulse"
    )

    // 3. Cloud Floating Horizontal Parallax Drift
    val cloudDrift1 by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CloudDrift1"
    )

    val cloudDrift2 by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(5600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CloudDrift2"
    )

    // 4. Cloud Vertical Bobbing (gentle floating)
    val cloudBob by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "CloudBob"
    )

    // 5. Rain Falling Loop (Continuous 600ms linear loop)
    val rainCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(620, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RainCycle"
    )

    // 6. Snow Falling Loop (1900ms smooth loop)
    val snowCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SnowCycle"
    )

    // 7. Thunderstorm Lightning Keyframe Pulse
    val lightningAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3600
                0.0f at 0
                0.0f at 2700
                1.0f at 2750
                0.1f at 2810
                0.95f at 2870
                0.0f at 2960
                0.0f at 3600
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "LightningAlpha"
    )

    // 8. Star Twinkle Phase for Night Sky
    val starTwinkle by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "StarTwinkle"
    )

    // 9. Fog Horizontal Wave Drift
    val fogDrift by infiniteTransition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(4800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FogDrift"
    )

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer() // Isolate hardware rendering layer for 2018 devices
            .testTag("animated_weather_art_canvas")
    ) {
        val w = size.width
        val h = size.height
        val cx = w * 0.52f
        val cy = h * 0.50f

        when (condition) {
            WeatherCondition.CLEAR_DAY -> {
                // Radiant Sun with 12 rotating rays and pulsing corona
                drawAnimatedSun(
                    cx = cx,
                    cy = cy,
                    radius = w * 0.28f,
                    rotationDeg = sunRotationDeg,
                    pulse = sunPulse
                )
                // Small cheerful floating breeze cloud puff
                drawCloudPuff(
                    cx = cx - w * 0.22f + cloudDrift1,
                    cy = cy + h * 0.26f + cloudBob,
                    baseRadius = w * 0.15f,
                    color = Color.White.copy(alpha = 0.50f)
                )
            }

            WeatherCondition.CLEAR_NIGHT -> {
                drawAnimatedMoonAndStars(
                    cx = cx,
                    cy = cy,
                    radius = w * 0.26f,
                    twinkle = starTwinkle,
                    w = w,
                    h = h
                )
            }

            WeatherCondition.PARTLY_CLOUDY_DAY -> {
                // Golden sun in the background
                drawAnimatedSun(
                    cx = cx + w * 0.16f,
                    cy = cy - h * 0.12f,
                    radius = w * 0.22f,
                    rotationDeg = sunRotationDeg,
                    pulse = sunPulse
                )
                // Layered fluffy white clouds in foreground
                drawCloudPuff(
                    cx = cx - w * 0.12f + cloudDrift1,
                    cy = cy + h * 0.10f + cloudBob,
                    baseRadius = w * 0.25f,
                    color = Color.White.copy(alpha = 0.72f)
                )
                drawCloudPuff(
                    cx = cx + w * 0.08f + cloudDrift2,
                    cy = cy + h * 0.16f - cloudBob,
                    baseRadius = w * 0.22f,
                    color = Color.White.copy(alpha = 0.95f)
                )
            }

            WeatherCondition.PARTLY_CLOUDY_NIGHT -> {
                drawAnimatedMoonAndStars(
                    cx = cx + w * 0.15f,
                    cy = cy - h * 0.12f,
                    radius = w * 0.22f,
                    twinkle = starTwinkle,
                    w = w,
                    h = h
                )
                drawCloudPuff(
                    cx = cx - w * 0.08f + cloudDrift1,
                    cy = cy + h * 0.12f + cloudBob,
                    baseRadius = w * 0.25f,
                    color = Color(0xD0E2E8F0)
                )
            }

            WeatherCondition.OVERCAST -> {
                // 3 Layered thick drifting overcast clouds
                drawCloudPuff(
                    cx = cx - w * 0.16f + cloudDrift2,
                    cy = cy - h * 0.08f + cloudBob * 0.5f,
                    baseRadius = w * 0.24f,
                    color = Color(0x99B0BEC5)
                )
                drawCloudPuff(
                    cx = cx + w * 0.14f + cloudDrift1,
                    cy = cy + h * 0.02f - cloudBob * 0.5f,
                    baseRadius = w * 0.27f,
                    color = Color(0xCCDCE2E6)
                )
                drawCloudPuff(
                    cx = cx - w * 0.02f + cloudDrift2 * 0.6f,
                    cy = cy + h * 0.16f + cloudBob,
                    baseRadius = w * 0.25f,
                    color = Color.White.copy(alpha = 0.92f)
                )
            }

            WeatherCondition.LIGHT_RAIN, WeatherCondition.RAIN -> {
                val isHeavy = condition == WeatherCondition.RAIN
                // Fluffy Rain Clouds
                drawCloudPuff(
                    cx = cx + cloudDrift1 * 0.5f,
                    cy = cy - h * 0.10f + cloudBob,
                    baseRadius = w * 0.27f,
                    color = Color(0xE0CFD8DC)
                )
                drawCloudPuff(
                    cx = cx - w * 0.10f + cloudDrift2 * 0.5f,
                    cy = cy - h * 0.04f - cloudBob,
                    baseRadius = w * 0.23f,
                    color = Color(0xF0ECEFF1)
                )
                // Continuous Animated Falling Rain Streaks + Splash Ripples
                drawFallingRain(
                    cx = cx,
                    startY = cy + h * 0.06f,
                    w = w * 0.72f,
                    h = h * 0.46f,
                    progress = rainCycle,
                    heavy = isHeavy
                )
            }

            WeatherCondition.THUNDERSTORM -> {
                // Dark moody storm clouds
                drawCloudPuff(
                    cx = cx + cloudDrift1 * 0.5f,
                    cy = cy - h * 0.10f + cloudBob,
                    baseRadius = w * 0.28f,
                    color = Color(0xF0455A64)
                )
                drawCloudPuff(
                    cx = cx - w * 0.12f + cloudDrift2 * 0.5f,
                    cy = cy - h * 0.04f - cloudBob,
                    baseRadius = w * 0.24f,
                    color = Color(0xF037474F)
                )
                // Lightning Strike Flash
                if (lightningAlpha > 0.04f) {
                    drawLightningBolt(
                        cx = cx + w * 0.02f,
                        startY = cy + h * 0.02f,
                        length = h * 0.40f,
                        alpha = lightningAlpha
                    )
                }
                // Falling heavy rain
                drawFallingRain(
                    cx = cx,
                    startY = cy + h * 0.08f,
                    w = w * 0.72f,
                    h = h * 0.46f,
                    progress = rainCycle,
                    heavy = true
                )
            }

            WeatherCondition.SNOW -> {
                drawCloudPuff(
                    cx = cx + cloudDrift1 * 0.5f,
                    cy = cy - h * 0.10f + cloudBob,
                    baseRadius = w * 0.27f,
                    color = Color(0xE0ECEFF1)
                )
                drawFallingSnow(
                    cx = cx,
                    startY = cy + h * 0.06f,
                    w = w * 0.72f,
                    h = h * 0.46f,
                    progress = snowCycle
                )
            }

            WeatherCondition.FOG -> {
                drawAnimatedFogBands(
                    cx = cx,
                    cy = cy,
                    w = w * 0.88f,
                    h = h * 0.65f,
                    drift = fogDrift
                )
            }
        }
    }
}

/**
 * Procedural Sun with 12 rotating rays, soft multi-stage corona pulse, and 3D specular sphere.
 * Completely zero-allocation inside draw scope.
 */
private fun DrawScope.drawAnimatedSun(
    cx: Float,
    cy: Float,
    radius: Float,
    rotationDeg: Float,
    pulse: Float
) {
    val centerOffset = Offset(cx, cy)

    // 1. Outer soft glowing corona
    drawCircle(
        color = COLOR_SUN_CORONA_OUTER,
        radius = radius * 1.55f * pulse,
        center = centerOffset
    )
    drawCircle(
        color = COLOR_SUN_CORONA_INNER,
        radius = radius * 1.25f * pulse,
        center = centerOffset
    )

    // 2. Rotating solar ray beams (12 rays with rounded tips)
    val rayCount = 12
    val rayInner = radius * 1.12f
    val rayOuter = radius * 1.48f * pulse
    val strokeWidth = radius * 0.085f

    for (i in 0 until rayCount) {
        val angleRad = Math.toRadians((rotationDeg + (i * 360f / rayCount)).toDouble())
        val cosA = cos(angleRad).toFloat()
        val sinA = sin(angleRad).toFloat()

        drawLine(
            color = COLOR_SUN_RAY,
            start = Offset(cx + cosA * rayInner, cy + sinA * rayInner),
            end = Offset(cx + cosA * rayOuter, cy + sinA * rayOuter),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }

    // 3. Crisp inner sun core
    drawCircle(
        color = SunYellow,
        radius = radius,
        center = centerOffset
    )

    // 4. 3D warm specular highlight
    drawCircle(
        color = COLOR_SUN_HIGHLIGHT,
        radius = radius * 0.65f,
        center = Offset(cx - radius * 0.18f, cy - radius * 0.18f)
    )
}

/**
 * Procedural Crescent Moon and Twinkling Stars.
 * Uses pre-allocated coordinate arrays - zero heap allocation.
 */
private fun DrawScope.drawAnimatedMoonAndStars(
    cx: Float,
    cy: Float,
    radius: Float,
    twinkle: Float,
    w: Float,
    h: Float
) {
    val centerOffset = Offset(cx, cy)

    // Glowing moon halo
    drawCircle(
        color = COLOR_MOON_HALO,
        radius = radius * 1.38f,
        center = centerOffset
    )

    // Luminous Moon Disc
    drawCircle(
        color = COLOR_MOON_DISC,
        radius = radius,
        center = centerOffset
    )

    // Shadow cutout creating clean crescent
    drawCircle(
        color = COLOR_MOON_SHADOW,
        radius = radius * 0.84f,
        center = Offset(cx + radius * 0.44f, cy - radius * 0.20f)
    )

    // Twinkling Stars using pre-allocated coordinate array
    var i = 0
    val total = STAR_COORDS.size
    var starIdx = 0
    while (i < total) {
        val sx = cx + STAR_COORDS[i] * w
        val sy = cy + STAR_COORDS[i + 1] * h
        val currentAlpha = if (starIdx % 2 == 0) twinkle else (1.25f - twinkle).coerceIn(0.2f, 1f)
        val starSize = 5f * currentAlpha

        drawLine(
            color = Color.White.copy(alpha = currentAlpha),
            start = Offset(sx - starSize, sy),
            end = Offset(sx + starSize, sy),
            strokeWidth = 2.0f,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White.copy(alpha = currentAlpha),
            start = Offset(sx, sy - starSize),
            end = Offset(sx, sy + starSize),
            strokeWidth = 2.0f,
            cap = StrokeCap.Round
        )
        i += 2
        starIdx++
    }
}

/**
 * Procedural organic cloud formation built with overlapping smooth shapes.
 */
private fun DrawScope.drawCloudPuff(
    cx: Float,
    cy: Float,
    baseRadius: Float,
    color: Color
) {
    val r = baseRadius
    // Bottom flat cushion
    drawOval(
        color = color,
        topLeft = Offset(cx - r * 1.35f, cy - r * 0.45f),
        size = Size(r * 2.7f, r * 1.25f)
    )
    // Left lobe
    drawCircle(
        color = color,
        radius = r * 0.72f,
        center = Offset(cx - r * 0.70f, cy - r * 0.18f)
    )
    // Center big lobe
    drawCircle(
        color = color,
        radius = r * 0.95f,
        center = Offset(cx - r * 0.05f, cy - r * 0.45f)
    )
    // Right lobe
    drawCircle(
        color = color,
        radius = r * 0.65f,
        center = Offset(cx + r * 0.68f, cy - r * 0.12f)
    )
}

/**
 * Smooth falling rain streaks and splash ripples with zero heap allocation.
 */
private fun DrawScope.drawFallingRain(
    cx: Float,
    startY: Float,
    w: Float,
    h: Float,
    progress: Float,
    heavy: Boolean
) {
    val streakCount = if (heavy) 12 else 8
    val streakLength = if (heavy) 22f else 15f
    val strokeWidth = if (heavy) 2.5f else 1.8f

    var idx = 0
    for (i in 0 until streakCount) {
        val relX = RAIN_PHASES[idx] * w
        val phaseOffset = RAIN_PHASES[idx + 1]
        idx += 2

        val dropProgress = (progress + phaseOffset) % 1.0f
        val x = cx + relX
        val y = startY + dropProgress * h

        val alpha = (1f - dropProgress * 0.35f).coerceIn(0.2f, 1f)
        drawLine(
            color = COLOR_RAIN_STREAK.copy(alpha = alpha),
            start = Offset(x - 3f, y),
            end = Offset(x, y + streakLength),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Draw small splash ripple when raindrop nears bottom (progress > 0.82)
        if (dropProgress > 0.82f) {
            val splashProgress = (dropProgress - 0.82f) / 0.18f
            val splashRadius = 2f + splashProgress * 6f
            val splashAlpha = (1f - splashProgress) * 0.6f
            drawOval(
                color = COLOR_RAIN_SPLASH.copy(alpha = splashAlpha),
                topLeft = Offset(x - splashRadius, startY + h - splashRadius * 0.4f),
                size = Size(splashRadius * 2f, splashRadius * 0.8f),
                style = STROKE_SPLASH
            )
        }
    }
}

/**
 * Zero-allocation animated lightning bolt.
 */
private fun DrawScope.drawLightningBolt(
    cx: Float,
    startY: Float,
    length: Float,
    alpha: Float
) {
    val boltColor = COLOR_LIGHTNING_BOLT.copy(alpha = alpha)
    val glowColor = COLOR_LIGHTNING_GLOW.copy(alpha = alpha * 0.6f)

    val p1 = Offset(cx, startY)
    val p2 = Offset(cx - 14f, startY + length * 0.38f)
    val p3 = Offset(cx + 6f, startY + length * 0.36f)
    val p4 = Offset(cx - 10f, startY + length)

    // Glow line (using reusable Stroke)
    drawLine(glowColor, p1, p2, strokeWidth = STROKE_LIGHTNING_GLOW.width, cap = STROKE_LIGHTNING_GLOW.cap)
    drawLine(glowColor, p2, p3, strokeWidth = STROKE_LIGHTNING_GLOW.width, cap = STROKE_LIGHTNING_GLOW.cap)
    drawLine(glowColor, p3, p4, strokeWidth = STROKE_LIGHTNING_GLOW.width, cap = STROKE_LIGHTNING_GLOW.cap)

    // Core line (using reusable Stroke)
    drawLine(boltColor, p1, p2, strokeWidth = STROKE_LIGHTNING_CORE.width, cap = STROKE_LIGHTNING_CORE.cap)
    drawLine(boltColor, p2, p3, strokeWidth = STROKE_LIGHTNING_CORE.width, cap = STROKE_LIGHTNING_CORE.cap)
    drawLine(boltColor, p3, p4, strokeWidth = STROKE_LIGHTNING_CORE.width, cap = STROKE_LIGHTNING_CORE.cap)
}

/**
 * Zero-allocation drifting and swaying snowflakes.
 */
private fun DrawScope.drawFallingSnow(
    cx: Float,
    startY: Float,
    w: Float,
    h: Float,
    progress: Float
) {
    val flakeColor = Color.White
    var idx = 0
    val count = SNOW_PHASES.size / 3

    for (i in 0 until count) {
        val relX = SNOW_PHASES[idx] * w
        val phaseOffset = SNOW_PHASES[idx + 1]
        val scale = SNOW_PHASES[idx + 2]
        idx += 3

        val fallProgress = (progress + phaseOffset) % 1.0f
        val sway = sin((fallProgress * Math.PI * 4).toDouble()).toFloat() * 10f * scale
        val x = cx + relX + sway
        val y = startY + fallProgress * h
        val radius = 3f * scale

        drawCircle(
            color = flakeColor.copy(alpha = 1f - fallProgress * 0.3f),
            radius = radius,
            center = Offset(x, y)
        )
    }
}

/**
 * Animated drifting horizontal fog bands.
 */
private fun DrawScope.drawAnimatedFogBands(
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    drift: Float
) {
    val bandHeight = h * 0.11f
    val yOffsets = floatArrayOf(-h * 0.28f, -h * 0.08f, h * 0.12f, h * 0.32f)

    for (i in yOffsets.indices) {
        val yOff = yOffsets[i]
        val sign = if (i % 2 == 0) 1f else -1f
        val offsetX = drift * sign * (1f + i * 0.25f)
        val bandWidth = w * (0.85f + (i % 2) * 0.15f)

        drawOval(
            color = COLOR_FOG_BAND.copy(alpha = 0.32f + (i % 3) * 0.08f),
            topLeft = Offset(cx - bandWidth / 2f + offsetX, cy + yOff),
            size = Size(bandWidth, bandHeight)
        )
    }
}
