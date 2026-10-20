package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import com.example.data.model.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AccuWeatherMapCanvas(
    layer: AccuWeatherMapLayer,
    frame: AccuWeatherMapFrame,
    sweepAngle: Float,
    cityName: String,
    tempUnit: TemperatureUnit,
    zoom: Float,
    panOffset: Offset,
    interactive: Boolean,
    onTransform: (panChange: Offset, zoomChange: Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val canvasModifier = if (interactive) {
        modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoomFactor, _ ->
                    onTransform(pan, zoomFactor)
                }
            }
    } else {
        modifier.fillMaxSize()
    }

    Canvas(modifier = canvasModifier.graphicsLayer()) {
        val centerX = size.width / 2f + panOffset.x
        val centerY = size.height / 2f + panOffset.y
        val minDim = minOf(size.width, size.height)

        drawGeographicBase(centerX, centerY, minDim, zoom)

        when (layer) {
            AccuWeatherMapLayer.RADAR_PRECIPITATION -> {
                drawPrecipitationEchoes(frame.precipitationEchoes, centerX, centerY, minDim, zoom)
                drawRadarSweep(centerX, centerY, minDim * 0.9f * zoom, sweepAngle)
            }
            AccuWeatherMapLayer.TEMPERATURE -> {
                drawTemperatureIsotherms(frame.temperatureIsotherms, centerX, centerY, minDim, zoom)
            }
            AccuWeatherMapLayer.WIND_STREAM -> {
                drawWindVectors(frame.windStreamVectors, centerX, centerY, minDim, zoom)
            }
            AccuWeatherMapLayer.CLOUDS_SATELLITE -> {
                drawCloudPatches(frame.cloudBlobs, centerX, centerY, minDim, zoom)
            }
            AccuWeatherMapLayer.SEVERE_STORMS -> {
                drawSevereStormCells(frame.severeCells, centerX, centerY, minDim, zoom)
                drawPrecipitationEchoes(frame.precipitationEchoes, centerX, centerY, minDim, zoom)
            }
        }

        drawCityMarker(centerX, centerY)
    }
}

private val STROKE_RING = Stroke(width = 1.2f)
private val STROKE_ISOTHERM = Stroke(width = 1.8f)
private val STROKE_WARNING = Stroke(width = 2.5f)

private fun DrawScope.drawGeographicBase(
    cx: Float,
    cy: Float,
    minDim: Float,
    zoom: Float
) {
    val ringColor = Color(0x224A90E2)
    val gridColor = Color(0x15FFFFFF)

    val r1 = minDim * 0.22f * zoom
    val r2 = minDim * 0.44f * zoom
    val r3 = minDim * 0.68f * zoom

    drawCircle(color = ringColor, radius = r1, center = Offset(cx, cy), style = STROKE_RING)
    drawCircle(color = ringColor, radius = r2, center = Offset(cx, cy), style = STROKE_RING)
    drawCircle(color = ringColor, radius = r3, center = Offset(cx, cy), style = STROKE_RING)

    val step = (60f * zoom).coerceIn(45f, 200f)
    var x = cx % step
    var linesX = 0
    while (x < size.width && linesX < 16) {
        drawLine(
            color = gridColor,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f
        )
        x += step
        linesX++
    }

    var y = cy % step
    var linesY = 0
    while (y < size.height && linesY < 16) {
        drawLine(
            color = gridColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
        y += step
        linesY++
    }

    val axisColor = Color(0x35FFFFFF)
    drawLine(
        color = axisColor,
        start = Offset(cx - 30f, cy),
        end = Offset(cx + 30f, cy),
        strokeWidth = 1.5f
    )
    drawLine(
        color = axisColor,
        start = Offset(cx, cy - 30f),
        end = Offset(cx, cy + 30f),
        strokeWidth = 1.5f
    )
}

private fun DrawScope.drawRadarSweep(
    cx: Float,
    cy: Float,
    radius: Float,
    angleDeg: Float
) {
    val rad = Math.toRadians(angleDeg.toDouble())
    val endX = cx + (cos(rad) * radius).toFloat()
    val endY = cy + (sin(rad) * radius).toFloat()

    drawArc(
        color = Color(0x30F05A28),
        startAngle = angleDeg - 35f,
        sweepAngle = 35f,
        useCenter = true,
        topLeft = Offset(cx - radius, cy - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
    )

    drawLine(
        color = Color(0xFFF05A28),
        start = Offset(cx, cy),
        end = Offset(endX, endY),
        strokeWidth = 2.0f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawPrecipitationEchoes(
    echoes: List<AccuWeatherEcho>,
    cx: Float,
    cy: Float,
    minDim: Float,
    zoom: Float
) {
    for (echo in echoes) {
        val posX = cx + echo.relX * minDim * zoom
        val posY = cy + echo.relY * minDim * zoom
        val r = echo.radius * minDim * zoom

        drawCircle(
            color = echo.color.copy(alpha = 0.35f),
            radius = r,
            center = Offset(posX, posY)
        )
        drawCircle(
            color = echo.color.copy(alpha = 0.65f),
            radius = r * 0.65f,
            center = Offset(posX, posY)
        )
        drawCircle(
            color = echo.color,
            radius = r * 0.35f,
            center = Offset(posX, posY)
        )
    }
}

private fun DrawScope.drawTemperatureIsotherms(
    isotherms: List<AccuWeatherIsotherm>,
    cx: Float,
    cy: Float,
    minDim: Float,
    zoom: Float
) {
    for (iso in isotherms) {
        val posX = cx + iso.relX * minDim * zoom
        val posY = cy + iso.relY * minDim * zoom
        val r = iso.radius * minDim * zoom

        drawCircle(
            color = iso.color.copy(alpha = 0.22f),
            radius = r,
            center = Offset(posX, posY)
        )
        drawCircle(
            color = iso.color.copy(alpha = 0.45f),
            radius = r * 0.6f,
            center = Offset(posX, posY)
        )
        drawCircle(
            color = iso.color.copy(alpha = 0.85f),
            radius = r * 0.85f,
            center = Offset(posX, posY),
            style = STROKE_ISOTHERM
        )
    }
}

private fun DrawScope.drawWindVectors(
    vectors: List<AccuWeatherWindVector>,
    cx: Float,
    cy: Float,
    minDim: Float,
    zoom: Float
) {
    val vectorColor = Color(0xFF26A69A)
    for (vec in vectors) {
        val sx = cx + vec.startRelX * minDim * zoom
        val sy = cy + vec.startRelY * minDim * zoom
        val ex = cx + vec.endRelX * minDim * zoom
        val ey = cy + vec.endRelY * minDim * zoom

        drawLine(
            color = vectorColor.copy(alpha = 0.85f),
            start = Offset(sx, sy),
            end = Offset(ex, ey),
            strokeWidth = 2.2f,
            cap = StrokeCap.Round
        )

        val dx = ex - sx
        val dy = ey - sy
        val len = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
        val udx = dx / len
        val udy = dy / len
        val arrowSize = 8f

        drawLine(
            color = vectorColor,
            start = Offset(ex, ey),
            end = Offset(ex - udx * arrowSize - udy * arrowSize * 0.5f, ey - udy * arrowSize + udx * arrowSize * 0.5f),
            strokeWidth = 2f
        )
        drawLine(
            color = vectorColor,
            start = Offset(ex, ey),
            end = Offset(ex - udx * arrowSize + udy * arrowSize * 0.5f, ey - udy * arrowSize - udx * arrowSize * 0.5f),
            strokeWidth = 2f
        )
    }
}

private fun DrawScope.drawCloudPatches(
    clouds: List<AccuWeatherCloudPatch>,
    cx: Float,
    cy: Float,
    minDim: Float,
    zoom: Float
) {
    for (cloud in clouds) {
        val posX = cx + cloud.relX * minDim * zoom
        val posY = cy + cloud.relY * minDim * zoom
        val rx = cloud.radiusX * minDim * zoom
        val ry = cloud.radiusY * minDim * zoom

        drawOval(
            color = Color.White.copy(alpha = cloud.opacity * 0.5f),
            topLeft = Offset(posX - rx, posY - ry),
            size = androidx.compose.ui.geometry.Size(rx * 2, ry * 2)
        )
    }
}

private fun DrawScope.drawSevereStormCells(
    cells: List<AccuWeatherSevereCell>,
    cx: Float,
    cy: Float,
    minDim: Float,
    zoom: Float
) {
    val warningColor = Color(0xFFE53935)
    for (cell in cells) {
        val posX = cx + cell.relX * minDim * zoom
        val posY = cy + cell.relY * minDim * zoom
        val r = 0.28f * minDim * zoom

        drawCircle(
            color = warningColor.copy(alpha = 0.25f),
            radius = r,
            center = Offset(posX, posY)
        )
        drawCircle(
            color = warningColor,
            radius = r,
            center = Offset(posX, posY),
            style = STROKE_WARNING
        )
        drawCircle(
            color = Color.Yellow,
            radius = 8f * zoom,
            center = Offset(posX, posY)
        )
    }
}

private fun DrawScope.drawCityMarker(
    cx: Float,
    cy: Float
) {
    drawCircle(
        color = Color.White.copy(alpha = 0.6f),
        radius = 12f,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = Color(0xFFF05A28),
        radius = 6f,
        center = Offset(cx, cy)
    )
    drawCircle(
        color = Color.White,
        radius = 2.5f,
        center = Offset(cx, cy)
    )
}
