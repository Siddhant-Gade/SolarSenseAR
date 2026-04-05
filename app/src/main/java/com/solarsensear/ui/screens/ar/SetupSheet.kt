package com.solarsensear.ui.screens.ar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.solarsensear.ui.components.AccentButton
import com.solarsensear.ui.components.InputStepper

/**
 * Bottom sheet displayed before launching the AR scan.
 * Collects: GPS location (auto), roof type, monthly electricity bill.
 * Pre-fills with compelling demo defaults per README Phase 7.3.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupSheet(
    onDismiss: () -> Unit,
    onStartScan: (panelCount: Int, roofType: String, monthlyBill: Double) -> Unit,
    detectedCity: String = "Detecting...",
    isLocating: Boolean = false
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var panelCount by remember { mutableIntStateOf(12) }
    var monthlyBill by remember { mutableStateOf("3000") }
    var selectedRoofIndex by remember { mutableIntStateOf(0) }
    var roofDropdownExpanded by remember { mutableStateOf(false) }

    val roofTypes = listOf("Flat", "Sloped 15°", "Sloped 30°")
    val roofApiValues = listOf("flat", "sloped_15", "sloped_30")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Setup Solar Scan",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // ── Location (auto-detected via GPS) ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Location",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isLocating) "Detecting location..." else detectedCity,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { /* Re-detect location */ }) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = "Detect location",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // ── Roof Type Dropdown ──
            ExposedDropdownMenuBox(
                expanded = roofDropdownExpanded,
                onExpandedChange = { roofDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = roofTypes[selectedRoofIndex],
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Roof Type") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = roofDropdownExpanded,
                    onDismissRequest = { roofDropdownExpanded = false }
                ) {
                    roofTypes.forEachIndexed { index, type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                selectedRoofIndex = index
                                roofDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // ── Panel Count ──
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Number of Panels",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                InputStepper(
                    value = panelCount,
                    onValueChange = { panelCount = it },
                    minValue = 1,
                    maxValue = 50
                )
            }

            // ── Monthly Bill ──
            OutlinedTextField(
                value = monthlyBill,
                onValueChange = { monthlyBill = it.filter { c -> c.isDigit() } },
                label = { Text("Monthly Electricity Bill (₹)") },
                placeholder = { Text("e.g. 3000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("₹ ") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(4.dp))

            // ── Start Scan Button ──
            AccentButton(
                text = "Start AR Scan",
                onClick = {
                    val bill = monthlyBill.toDoubleOrNull() ?: 2000.0
                    onStartScan(panelCount, roofApiValues[selectedRoofIndex], bill)
                }
            )
        }
    }
}
