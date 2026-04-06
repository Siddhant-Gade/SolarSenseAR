package com.solarsensear.ui.screens.calculator

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solarsensear.domain.LocalSolarCalculator
import com.solarsensear.ui.components.MonthlyBarChart
import com.solarsensear.ui.components.PaybackProgressBar
import com.solarsensear.ui.theme.*

private val INDIAN_CITIES = listOf(
    "Nagpur", "Mumbai", "Delhi", "Bangalore", "Hyderabad", "Chennai",
    "Kolkata", "Pune", "Ahmedabad", "Jaipur", "Lucknow", "Bhopal", "Surat"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen() {
    var selectedCity by remember { mutableStateOf("Nagpur") }
    var cityExpanded by remember { mutableStateOf(false) }
    var roofType by remember { mutableStateOf("flat") }
    var panelCount by remember { mutableIntStateOf(12) }
    var monthlyBill by remember { mutableFloatStateOf(3000f) }
    var shadowLoss by remember { mutableFloatStateOf(5f) }

    val result by remember(selectedCity, roofType, panelCount, monthlyBill, shadowLoss) {
        derivedStateOf {
            LocalSolarCalculator.calculate(
                panelCount = panelCount,
                locationName = selectedCity,
                roofType = roofType,
                monthlyBillInr = monthlyBill.toDouble(),
                shadowLossPercent = shadowLoss.toDouble()
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Navy800, Navy700)))
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Solar Calculator",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Configure parameters and see results instantly",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // City selector
            ExposedDropdownMenuBox(
                expanded = cityExpanded,
                onExpandedChange = { cityExpanded = !cityExpanded }
            ) {
                OutlinedTextField(
                    value = selectedCity,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("City") },
                    leadingIcon = { Icon(Icons.Filled.LocationCity, null, tint = Amber500) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(cityExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = cityExpanded,
                    onDismissRequest = { cityExpanded = false }
                ) {
                    INDIAN_CITIES.forEach { city ->
                        DropdownMenuItem(
                            text = { Text(city) },
                            onClick = { selectedCity = city; cityExpanded = false }
                        )
                    }
                }
            }

            // Roof type
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("Flat" to "flat", "Sloped" to "sloped").forEach { (label, value) ->
                    val sel = roofType == value
                    OutlinedButton(
                        onClick = { roofType = value },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (sel) Amber500.copy(alpha = 0.12f) else Color.Transparent,
                            contentColor = if (sel) Amber500 else MaterialTheme.colorScheme.onSurface
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = androidx.compose.ui.graphics.SolidColor(
                                if (sel) Amber500 else MaterialTheme.colorScheme.outline
                            )
                        )
                    ) {
                        Text(label, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }

            // Panel count
            SliderField(
                label = "Panel Count",
                value = panelCount.toFloat(),
                displayValue = "$panelCount panels · ${String.format("%.1f", panelCount * 0.55)} kW",
                onValueChange = { panelCount = it.toInt() },
                range = 4f..40f,
                steps = 35,
                color = Amber500
            )

            // Monthly bill
            SliderField(
                label = "Monthly Electricity Bill",
                value = monthlyBill,
                displayValue = "₹${monthlyBill.toInt()}/month",
                onValueChange = { monthlyBill = it },
                range = 500f..15000f,
                steps = 0,
                color = Amber500
            )

            // Shadow loss
            SliderField(
                label = "Shadow Loss Estimate",
                value = shadowLoss,
                displayValue = "${shadowLoss.toInt()}% loss",
                onValueChange = { shadowLoss = it },
                range = 0f..25f,
                steps = 24,
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider()

            // ── Live Results ──
            Text(
                "Live Results",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            AnimatedContent(
                targetState = result,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "result_anim"
            ) { r ->
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Hero savings
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Navy800,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("25-Year Savings", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(0.7f))
                            Text(
                                formatHero(r.savings25yrInr),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Amber500
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ResultChip("Gross Cost", "₹${fmt(r.installationCostInr)}", modifier = Modifier.weight(1f))
                        ResultChip("Subsidy", "₹${fmt(r.subsidyInr)}", valueColor = Color(0xFF22C55E), modifier = Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ResultChip("Net Cost", "₹${fmt(r.netCostInr)}", valueColor = Amber500, modifier = Modifier.weight(1f))
                        ResultChip("Annual Savings", "₹${fmt(r.annualSavingsInr)}", valueColor = Color(0xFF22C55E), modifier = Modifier.weight(1f))
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ResultChip("Monthly Output", "${r.monthlyGenerationUnits} kWh", modifier = Modifier.weight(1f))
                        ResultChip("CO₂ Saved", "${r.co2KgAnnual} kg/yr", valueColor = Color(0xFF22C55E), modifier = Modifier.weight(1f))
                    }

                    ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Payback Period", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            PaybackProgressBar(paybackYears = r.paybackYears)
                        }
                    }

                    ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Monthly Generation Forecast", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                            MonthlyBarChart(values = r.monthlyBreakdown)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SliderField(
    label: String,
    value: Float,
    displayValue: String,
    onValueChange: (Float) -> Unit,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(displayValue, style = MaterialTheme.typography.bodySmall, color = color, fontWeight = FontWeight.SemiBold)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            steps = steps,
            colors = SliderDefaults.colors(thumbColor = color, activeTrackColor = color)
        )
    }
}

@Composable
private fun ResultChip(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = valueColor)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

private fun fmt(amount: Int): String = when {
    amount >= 100000 -> "${String.format("%.1f", amount / 100000.0)}L"
    amount >= 1000   -> "${amount / 1000}K"
    else             -> "$amount"
}

private fun formatHero(amount: Int): String = when {
    amount >= 10000000 -> "₹${String.format("%.2f", amount / 10000000.0)} Crore"
    amount >= 100000  -> "₹${String.format("%.1f", amount / 100000.0)} Lakh"
    else              -> "₹$amount"
}