package com.solarsensear.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solarsensear.ui.theme.Amber500
import com.solarsensear.ui.theme.Navy500
import com.solarsensear.ui.theme.Success

@Composable
fun SubsidyBreakdownBar(
    capacityKw: Double,
    modifier: Modifier = Modifier
) {
    val slabs = listOf(
        SubsidySlab("≤1 kW", 30000, 0.33f, Navy500),
        SubsidySlab("1–2 kW", 60000, 0.33f, Amber500),
        SubsidySlab(">2 kW", 78000, 0.34f, Success)
    )

    val activeIndex = when {
        capacityKw <= 1.0 -> 0
        capacityKw <= 2.0 -> 1
        else -> 2
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "PM Surya Ghar Subsidy Slabs",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            slabs.forEachIndexed { index, slab ->
                Box(
                    modifier = Modifier
                        .weight(slab.weight)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            if (index <= activeIndex) slab.color
                            else slab.color.copy(alpha = 0.2f)
                        )
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            slabs.forEachIndexed { index, slab ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = slab.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (index == activeIndex) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = if (index == activeIndex) FontWeight.Bold else FontWeight.Normal
                    )
                    Text(
                        text = "₹${slab.amount / 1000}K",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (index == activeIndex) {
                            slab.color
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontWeight = if (index == activeIndex) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

private data class SubsidySlab(
    val label: String,
    val amount: Int,
    val weight: Float,
    val color: androidx.compose.ui.graphics.Color
)
