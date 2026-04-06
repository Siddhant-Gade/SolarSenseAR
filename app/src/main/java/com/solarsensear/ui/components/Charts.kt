package com.solarsensear.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solarsensear.ui.theme.Amber500
import com.solarsensear.ui.theme.Navy700

private val MONTHS = listOf("J","F","M","A","M","J","J","A","S","O","N","D")

/**
 * Animated monthly generation bar chart (Jan–Dec).
 */
@Composable
fun MonthlyBarChart(
    values: List<Int>,
    modifier: Modifier = Modifier,
    barColor: Color = Amber500,
    labelColor: Color = Color.Gray,
    heightDp: Dp = 160.dp
) {
    val maxVal = values.maxOrNull()?.toFloat() ?: 1f
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 900, easing = EaseOut),
        label = "bar_anim"
    )

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightDp)
        ) {
            val barAreaWidth = size.width / values.size
            val barPadding = barAreaWidth * 0.2f
            val barWidth = barAreaWidth - barPadding * 2

            values.forEachIndexed { index, value ->
                val barHeight = (value / maxVal) * size.height * animProgress
                val left = index * barAreaWidth + barPadding
                val top = size.height - barHeight

                drawRoundRect(
                    color = barColor.copy(alpha = 0.15f),
                    topLeft = Offset(left, 0f),
                    size = Size(barWidth, size.height),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(left, top),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }
        }

        // Month labels
        Row(modifier = Modifier.fillMaxWidth()) {
            MONTHS.forEach { month ->
                Text(
                    text = month,
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    fontSize = 10.sp
                )
            }
        }
    }
}

/**
 * 25-year cumulative savings line chart.
 */
@Composable
fun SavingsLineChart(
    annualSavingsInr: Int,
    netCostInr: Int,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF22C55E),
    heightDp: Dp = 140.dp
) {
    val points = (0..25).map { year ->
        (annualSavingsInr * year - netCostInr).toFloat()
    }
    val minVal = points.min()
    val maxVal = points.max().coerceAtLeast(1f)
    val range = maxVal - minVal

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 1100, easing = EaseOut),
        label = "line_anim"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp)
    ) {
        val stepX = size.width / 25f
        val playedPoints = (points.size * animProgress).toInt().coerceAtLeast(2)

        // Zero line
        val zeroY = size.height * (1f - (-minVal / range))
        drawLine(
            color = Color.Gray.copy(alpha = 0.3f),
            start = Offset(0f, zeroY),
            end = Offset(size.width, zeroY),
            strokeWidth = 1f
        )

        // Fill under line
        val path = androidx.compose.ui.graphics.Path()
        for (i in 0 until playedPoints) {
            val x = i * stepX
            val y = size.height * (1f - ((points[i] - minVal) / range))
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        val lastX = (playedPoints - 1) * stepX
        path.lineTo(lastX, size.height)
        path.lineTo(0f, size.height)
        path.close()
        drawPath(path, color = lineColor.copy(alpha = 0.1f))

        // Line
        for (i in 1 until playedPoints) {
            val x1 = (i - 1) * stepX
            val x2 = i * stepX
            val y1 = size.height * (1f - ((points[i - 1] - minVal) / range))
            val y2 = size.height * (1f - ((points[i] - minVal) / range))
            drawLine(
                color = lineColor,
                start = Offset(x1, y1),
                end = Offset(x2, y2),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * Animated horizontal progress bar for payback period.
 */
@Composable
fun PaybackProgressBar(
    paybackYears: Double,
    maxYears: Int = 10,
    modifier: Modifier = Modifier,
    barColor: Color = Amber500,
    trackColor: Color = Navy700
) {
    val progress by animateFloatAsState(
        targetValue = (paybackYears / maxYears).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800, easing = EaseOut),
        label = "payback_progress"
    )

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Payback: ${String.format("%.1f", paybackYears)} yrs",
                style = MaterialTheme.typography.labelMedium,
                color = barColor,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${maxYears} yr max",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
        ) {
            drawRoundRect(
                color = trackColor,
                size = Size(size.width, size.height),
                cornerRadius = CornerRadius(5f, 5f)
            )
            drawRoundRect(
                color = barColor,
                size = Size(size.width * progress, size.height),
                cornerRadius = CornerRadius(5f, 5f)
            )
        }
    }
}

/**
 * Simple donut chart for usage coverage percentage.
 */
@Composable
fun CoverageDonut(
    coveragePercent: Int,
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFF22C55E),
    trackColor: Color = Navy700,
    size: Dp = 120.dp
) {
    val sweep by animateFloatAsState(
        targetValue = coveragePercent / 100f * 360f,
        animationSpec = tween(durationMillis = 1000, easing = EaseOut),
        label = "donut_sweep"
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = this.size.width * 0.14f
            val inset = strokeWidth / 2
            val arcSize = Size(this.size.width - strokeWidth, this.size.height - strokeWidth)
            val topLeft = Offset(inset, inset)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )
            drawArc(
                color = primaryColor,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$coveragePercent%",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = primaryColor
            )
            Text(
                text = "covered",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }
    }
}
