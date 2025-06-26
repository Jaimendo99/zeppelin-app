package com.zeppelin.app.screens.courseSession.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zeppelin.app.screens.courseSession.data.WatchMetricLists
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// Helper data class for processed chart data
private data class LineData(
    val name: String,
    val points: List<Pair<Long, Float>>,
    val color: Color,
)

// **FIX**: Data class to hold chart boundaries with strong types
private data class ChartBounds(
    val minTimestamp: Long,
    val maxTimestamp: Long,
    val minY: Float,
    val maxY: Float,
)

@Composable
fun MetricsPlot(sessionMetrics: WatchMetricLists) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val colors = MaterialTheme.colorScheme

    val processedData = remember(sessionMetrics) {
        listOf(
            LineData(
                name = "Heart Rate",
                points = sessionMetrics.heartRate.mapNotNull {
                    val value = (it.value as? Number)?.toFloat()
                    if (value != null) it.timestamp to value else null
                },
                color = colors.primaryContainer, // Pink
            ),
            LineData(
                name = "Movement",
                points = sessionMetrics.movementIntensity.mapNotNull {
                    val value = (it.value as? Number)?.toFloat()
                    if (value != null) it.timestamp to value else null
                },
                color = colors.secondaryContainer, // Blue
            ),
            LineData(
                name = "RSSI",
                points = sessionMetrics.rssi.mapNotNull {
                    val value = (it.value as? Number)?.toFloat()
                    if (value != null) it.timestamp to value else null
                },
                color = colors.tertiary, // Green
            ),
        ).filter { it.points.isNotEmpty() }
    }

    // **FIX**: Use the ChartBounds data class for type safety
    val bounds = remember(processedData) {
        if (processedData.isEmpty() || processedData.all { it.points.isEmpty() }) {
            return@remember ChartBounds(0L, 1L, 0f, 1f) // Default bounds
        }

        val minTs = processedData.minOf { it.points.minOf { p -> p.first } }
        val maxTs = processedData.maxOf { it.points.maxOf { p -> p.first } }
        val minVal = processedData.minOf { it.points.minOf { p -> p.second } }
        val maxVal = processedData.maxOf { it.points.maxOf { p -> p.second } }

        val yPadding = (maxVal - minVal) * 0.1f
        ChartBounds(
            minTimestamp = minTs,
            maxTimestamp = maxTs,
            minY = minVal - yPadding,
            maxY = maxVal + yPadding,
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Canvas(
            modifier = Modifier.fillMaxWidth().height(250.dp),
        ) {
            val padding = 16.dp.toPx()
            val yAxisLabelWidth = 40.dp.toPx()
            val xAxisLabelHeight = 20.dp.toPx()

            val chartWidth = size.width - padding - yAxisLabelWidth
            val chartHeight = size.height - padding - xAxisLabelHeight

            // **FIX**: This calculation now works correctly with strong types
            val xRange =
                (bounds.maxTimestamp - bounds.minTimestamp).toFloat().takeIf { it > 0 }
                    ?: 1f
            val yRange = (bounds.maxY - bounds.minY).takeIf { it > 0 } ?: 1f

            // --- Draw Grid Lines & Axis Labels ---
            val gridColor = Color.Gray.copy(alpha = 0.5f)
            val labelStyle = TextStyle(fontSize = 12.sp, color = Color.Gray)
            val gridPathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))

            val yGridLineCount = 5
            for (i in 0..yGridLineCount) {
                val value = bounds.minY + (yRange / yGridLineCount) * i
                val y =
                    chartHeight - ((value - bounds.minY) / yRange) * chartHeight + padding

                drawLine(
                    color = gridColor,
                    start = Offset(yAxisLabelWidth, y),
                    end = Offset(size.width - padding, y),
                    pathEffect = gridPathEffect,
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = value.roundToInt().toString(),
                    topLeft = Offset(0f, y - 8.dp.toPx()),
                    style = labelStyle,
                )
            }

            if (bounds.minTimestamp > 0) {
                val timeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                drawText(
                    textMeasurer = textMeasurer,
                    text = timeFormatter.format(Date(bounds.minTimestamp)),
                    topLeft = Offset(yAxisLabelWidth, size.height - xAxisLabelHeight),
                    style = labelStyle,
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = timeFormatter.format(Date(bounds.maxTimestamp)),
                    topLeft = Offset(
                        size.width - padding - 40.dp.toPx(),
                        size.height - xAxisLabelHeight,
                    ),
                    style = labelStyle,
                )
            }

            // --- Draw Data Lines ---
            processedData.forEach { data ->
                if (data.points.size < 2) return@forEach

                val path = Path()
                data.points.forEachIndexed { i, point ->
                    val (timestamp, value) = point
                    val x =
                        ((timestamp - bounds.minTimestamp) / xRange) * chartWidth + yAxisLabelWidth
                    val y =
                        chartHeight - ((value - bounds.minY) / yRange) * chartHeight + padding

                    if (i == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                drawPath(
                    path = path,
                    color = data.color,
                    style = Stroke(width = 2.dp.toPx()),
                )
            }
        }

        ChartLegend(processedData)
    }
}

@Composable
private fun ChartLegend(data: List<LineData>) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        data.forEach { line ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(line.color, shape = MaterialTheme.shapes.small),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = line.name, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
}