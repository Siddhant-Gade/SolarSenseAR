package com.solarsensear.ui.screens.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.Divider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.solarsensear.domain.SolarCalculator
import com.solarsensear.ui.components.AnimatedMetricCard
import com.solarsensear.ui.components.InputStepper
import com.solarsensear.ui.components.SubsidyBreakdownBar
import com.solarsensear.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen() {

    var panelCount by remember { mutableIntStateOf(10) }
    var panelWatt by remember { mutableIntStateOf(400) }
    var monthlyBill by remember { mutableStateOf("2500") }
    var irradiance by remember { mutableDoubleStateOf(5.5) }
    var selectedRoofIndex by remember { mutableIntStateOf(0) }
    var selectedCity by remember { mutableStateOf("Delhi") }
    var wattDropdownExpanded by remember { mutableStateOf(false) }
    var cityDropdownExpanded by remember { mutableStateOf(false) }

    val roofTypes = listOf("Flat", "Sloped 15°", "Sloped 30°")
    val wattOptions = listOf(330, 400, 540)
    val cities = mapOf(
        "Nagpur" to 5.8, "Mumbai" to 5.2, "Pune" to 5.5, "Delhi" to 5.5
    )

    val bill = monthlyBill.toDoubleOrNull() ?: 2000.0

    val report by remember(panelCount, panelWatt, irradiance, bill) {
        derivedStateOf {
            SolarCalculator.calculate(
                panelCount,
                panelWatt,
                irradiance,
                bill,
                selectedCity
            )
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Text("Solar Calculator", style = MaterialTheme.typography.headlineSmall)
        }

        item {
            ElevatedCard {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    Text("Location")

                    ExposedDropdownMenuBox(
                        expanded = cityDropdownExpanded,
                        onExpandedChange = { cityDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCity,
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = cityDropdownExpanded,
                            onDismissRequest = { cityDropdownExpanded = false }
                        ) {
                            cities.keys.forEach {
                                DropdownMenuItem(
                                    text = { Text(it) },
                                    onClick = {
                                        selectedCity = it
                                        irradiance = cities[it]!!
                                        cityDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Divider()

                    Text("Panel Count")

                    InputStepper(
                        value = panelCount,
                        modifier = Modifier,
                        onValueChange = { panelCount = it }
                    )
                    Divider()

                    Text("Monthly Bill")
                    OutlinedTextField(
                        value = monthlyBill,
                        onValueChange = { monthlyBill = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }

        item {
            Text("Results")
        }

        item {
            AnimatedMetricCard(
                icon = Icons.Filled.SolarPower,
                label = "System Size",
                targetValue = report.capacityKw.toInt()
            )
        }
    }
}