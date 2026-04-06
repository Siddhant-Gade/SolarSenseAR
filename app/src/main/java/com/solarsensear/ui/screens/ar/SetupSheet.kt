package com.solarsensear.ui.screens.ar

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solarsensear.data.local.LocationHelper
import com.solarsensear.ui.theme.Amber500
import com.solarsensear.ui.theme.Navy700
import kotlinx.coroutines.launch

private val ROOF_TYPES = listOf("Flat" to "flat", "Sloped" to "sloped")
private val STATES = listOf(
    "Andhra Pradesh", "Delhi", "Gujarat", "Karnataka", "Kerala",
    "Madhya Pradesh", "Maharashtra", "Rajasthan", "Tamil Nadu",
    "Telangana", "Uttar Pradesh", "West Bengal"
)
private val BRANDS = listOf("Any Brand", "Tata Power", "Adani Solar", "Waaree", "Luminous", "Vikram Solar")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupSheet(
    onDismiss: () -> Unit,
    onStartScan: (panelCount: Int, roofType: String, locationName: String, monthlyBill: Double) -> Unit
) {
    var selectedRoofType by remember { mutableStateOf("flat") }
    var panelCount by remember { mutableIntStateOf(12) }
    var roofAreaSqFt by remember { mutableFloatStateOf(800f) }
    var monthlyBill by remember { mutableFloatStateOf(3000f) }
    var locationName by remember { mutableStateOf("Nagpur") }
    var selectedBrand by remember { mutableStateOf("Any Brand") }
    var brandExpanded by remember { mutableStateOf(false) }
    var isFetchingLocation by remember { mutableStateOf(false) }
    var locationError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Runtime permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                      grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            isFetchingLocation = true
            locationError = null
            scope.launch {
                try {
                    locationName = LocationHelper.fetchCityName(context)
                } catch (e: Exception) {
                    locationError = "Could not get location: ${e.localizedMessage}"
                } finally {
                    isFetchingLocation = false
                }
            }
        } else {
            locationError = "Location permission denied"
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Configure Your Scan",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "All fields are optional",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }

            Divider()

            // Location field with GPS auto-detect
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it; locationError = null },
                    label = { Text("City / Location") },
                    leadingIcon = {
                        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Amber500)
                    },
                    trailingIcon = {
                        if (isFetchingLocation) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Amber500
                            )
                        } else {
                            IconButton(
                                onClick = {
                                    if (LocationHelper.hasPermission(context)) {
                                        isFetchingLocation = true
                                        locationError = null
                                        scope.launch {
                                            try {
                                                locationName = LocationHelper.fetchCityName(context)
                                            } catch (e: Exception) {
                                                locationError = "Could not get location: ${e.localizedMessage}"
                                            } finally {
                                                isFetchingLocation = false
                                            }
                                        }
                                    } else {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.GpsFixed,
                                    contentDescription = "Use my location",
                                    tint = Amber500
                                )
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = locationError != null
                )
                locationError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // Roof type selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Roof Type",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ROOF_TYPES.forEach { (label, value) ->
                        val selected = selectedRoofType == value
                        Surface(
                            selected = selected,
                            onClick = { selectedRoofType = value },
                            shape = RoundedCornerShape(12.dp),
                            color = if (selected) Amber500.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (selected)
                                ButtonDefaults.outlinedButtonBorder.copy(
                                    brush = androidx.compose.ui.graphics.SolidColor(Amber500)
                                )
                            else null,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(vertical = 14.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = if (selected) Amber500 else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Panel count stepper
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Panel Count",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$panelCount panels · ${String.format("%.1f", panelCount * 0.55)} kW",
                        style = MaterialTheme.typography.bodySmall,
                        color = Amber500,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Slider(
                    value = panelCount.toFloat(),
                    onValueChange = { panelCount = it.toInt() },
                    valueRange = 4f..40f,
                    steps = 35,
                    colors = SliderDefaults.colors(
                        thumbColor = Amber500,
                        activeTrackColor = Amber500,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("4", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("40", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }

            // Roof area slider
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Roof Area (optional)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${roofAreaSqFt.toInt()} sq.ft",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Slider(
                    value = roofAreaSqFt,
                    onValueChange = { roofAreaSqFt = it },
                    valueRange = 200f..3000f,
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Monthly bill slider
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Monthly Bill",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "₹${monthlyBill.toInt()}/month",
                        style = MaterialTheme.typography.bodySmall,
                        color = Amber500,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Slider(
                    value = monthlyBill,
                    onValueChange = { monthlyBill = it },
                    valueRange = 500f..15000f,
                    colors = SliderDefaults.colors(
                        thumbColor = Amber500,
                        activeTrackColor = Amber500,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("₹500", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("₹15,000", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }

            // Brand dropdown
            ExposedDropdownMenuBox(
                expanded = brandExpanded,
                onExpandedChange = { brandExpanded = !brandExpanded }
            ) {
                OutlinedTextField(
                    value = selectedBrand,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Preferred Brand (optional)") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = brandExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = brandExpanded,
                    onDismissRequest = { brandExpanded = false }
                ) {
                    BRANDS.forEach { brand ->
                        DropdownMenuItem(
                            text = { Text(brand) },
                            onClick = {
                                selectedBrand = brand
                                brandExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CTA button
            Button(
                onClick = {
                    onStartScan(panelCount, selectedRoofType, locationName, monthlyBill.toDouble())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Amber500
                )
            ) {
                Text(
                    text = "Start AR Scan",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Navy700
                )
            }
        }
    }
}
