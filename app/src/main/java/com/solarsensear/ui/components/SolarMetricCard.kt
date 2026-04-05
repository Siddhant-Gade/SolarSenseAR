package com.solarsensear.ui.components

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SolarMetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String = "",
    iconTint: Color = MaterialTheme.colorScheme.secondary,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedMetricCard(
    icon: ImageVector,
    label: String,
    targetValue: Int,
    prefix: String = "",
    suffix: String = "",
    unit: String = "",
    iconTint: Color = MaterialTheme.colorScheme.secondary,
    modifier: Modifier = Modifier
) {
    var animationPlayed by remember { mutableIntStateOf(0) }
    val animatedValue by animateIntAsState(
        targetValue = animationPlayed,
        animationSpec = tween(durationMillis = 1200),
        label = "counter"
    )

    LaunchedEffect(targetValue) {
        animationPlayed = targetValue
    }

    SolarMetricCard(
        icon = icon,
        label = label,
        value = "$prefix${formatIndianNumber(animatedValue)}$suffix",
        unit = unit,
        iconTint = iconTint,
        modifier = modifier
    )
}

private fun formatIndianNumber(number: Int): String {
    if (number < 1000) return number.toString()
    val str = number.toString()
    val lastThree = str.takeLast(3)
    val remaining = str.dropLast(3)
    val formatted = remaining.reversed().chunked(2).joinToString(",").reversed()
    return "$formatted,$lastThree"
}
