package com.solarsensear.ui.screens.ar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.solarsensear.ui.components.ShadowSlider
import com.solarsensear.ui.theme.Amber500

/**
 * AR camera screen with Compose overlay controls.
 *
 * Phase 2: UI overlay layout only (camera feed is a placeholder).
 * Phase 4: ARSceneView will replace the placeholder Box, and actual
 * AR interactions (tap-to-place, pinch-to-scale, drag-to-move) will be wired.
 *
 * Controls overlay:
 * - Back button (top left)
 * - Panel count +/- (right side)
 * - Shadow timeline slider (bottom, toggleable)
 * - Capture button (center bottom)
 */
@Composable
fun ARScreen(
    panelCount: Int,
    roofType: String,
    onBack: () -> Unit,
    onCapture: (panelCount: Int) -> Unit
) {
    var currentPanelCount by remember { mutableIntStateOf(panelCount) }
    var showShadowSlider by remember { mutableStateOf(false) }
    var shadowHour by remember { mutableFloatStateOf(12f) }

    Box(modifier = Modifier.fillMaxSize()) {
        // ── AR Camera View Placeholder ──
        // Phase 4: Replace with ARSceneView
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1A1A2E)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CameraAlt,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.3f),
                    modifier = Modifier.size(64.dp)
                )
                Text(
                    text = "AR Camera View",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.5f)
                )
                Text(
                    text = "Point at a flat surface to detect your roof",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.3f)
                )
            }
        }

        // ── Top Bar Overlay ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.4f)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            // Panel count label
            Text(
                text = "$currentPanelCount panels · $roofType",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier
                    .background(
                        color = Color.Black.copy(alpha = 0.4f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        // ── Right Side Panel Controls ──
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SmallFloatingActionButton(
                onClick = {
                    if (currentPanelCount < 50) currentPanelCount++
                },
                containerColor = Color.Black.copy(alpha = 0.5f),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add panel")
            }

            Text(
                text = "$currentPanelCount",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            SmallFloatingActionButton(
                onClick = {
                    if (currentPanelCount > 1) currentPanelCount--
                },
                containerColor = Color.Black.copy(alpha = 0.5f),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Remove, contentDescription = "Remove panel")
            }
        }

        // ── Bottom Controls ──
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Shadow slider (toggleable)
            if (showShadowSlider) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f))
                ) {
                    ShadowSlider(
                        hourOfDay = shadowHour,
                        onHourChanged = { shadowHour = it }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shadow toggle
                IconButton(
                    onClick = { showShadowSlider = !showShadowSlider },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (showShadowSlider) Amber500 else Color.White.copy(alpha = 0.2f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.WbSunny,
                        contentDescription = "Toggle shadow",
                        tint = if (showShadowSlider) Color.White else Color.White.copy(alpha = 0.7f)
                    )
                }

                // Capture button
                FloatingActionButton(
                    onClick = { onCapture(currentPanelCount) },
                    containerColor = Amber500,
                    contentColor = Color.White,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "Capture",
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Spacer to balance the row
                IconButton(
                    onClick = { },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Transparent
                    ),
                    enabled = false
                ) {
                    // Empty placeholder for symmetry
                }
            }
        }
    }
}
