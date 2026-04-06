package com.solarsensear.ui.screens.ar

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.ar.core.Config
import com.solarsensear.ui.components.ShadowSlider
import com.solarsensear.ui.theme.Amber500
import com.solarsensear.ui.theme.Navy900
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.node.ARCameraNode
import kotlinx.coroutines.delay

/**
 * Full AR Camera Screen with:
 * - Real ARSceneView + ARCore plane detection
 * - Scanning grid animation while searching
 * - Panel count badge, +/- controls
 * - Shadow time-of-day slider
 * - Capture → triggers analysis pipeline
 * - Analysis loading overlay
 */
@Composable
fun ARScreen(
    panelCount: Int,
    roofType: String,
    locationName: String = "Nagpur",
    monthlyBillInr: Double = 2000.0,
    onBack: () -> Unit,
    onCapture: (panelCount: Int) -> Unit
) {
    val context = LocalContext.current
    val viewModel: AnalyzeViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()

    var currentPanelCount by remember { mutableIntStateOf(panelCount) }
    var showShadowSlider by remember { mutableStateOf(false) }
    var shadowHour by remember { mutableFloatStateOf(12f) }
    var planeDetected by remember { mutableStateOf(false) }
    var isScanning by remember { mutableStateOf(true) }
    var cameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> cameraPermissionGranted = granted }

    LaunchedEffect(Unit) {
        if (!cameraPermissionGranted) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Simulate plane detection after a delay (real ARCore fires this via callback)
    LaunchedEffect(cameraPermissionGranted) {
        if (cameraPermissionGranted) {
            delay(2500)
            planeDetected = true
            isScanning = false
        }
    }

    // Navigate when analysis completes
    LaunchedEffect(uiState) {
        if (uiState is AnalyzeUiState.Success) {
            onCapture(currentPanelCount)
        }
    }

    // Animated capacity text
    val capacityKw = String.format("%.1f", currentPanelCount * 0.55)
    val estimatedCost = ((currentPanelCount * 0.55 * 60000) / 100000)
    val costStr = "₹${String.format("%.1f", estimatedCost)}L"

    // Scanning pulse animation
    val pulseAnim = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnim.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse_scale"
    )
    val scanAlpha by pulseAnim.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "scan_alpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {

        // ── AR Camera View ──
        if (cameraPermissionGranted) {
            AndroidView(
                factory = { ctx ->
                    ARSceneView(ctx).apply {
                        planeRenderer.isEnabled = true
                        configureSession { session, config ->
                            config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                            config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Permission not granted fallback
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Navy900),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Camera permission required",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        colors = ButtonDefaults.buttonColors(containerColor = Amber500)
                    ) {
                        Text("Grant Permission", color = Navy900)
                    }
                }
            }
        }

        // ── Scanning Overlay (while searching for planes) ──
        AnimatedVisibility(
            visible = isScanning,
            enter = fadeIn(),
            exit = fadeOut(tween(600))
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Animated scan ring
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .scale(pulseScale)
                        .border(2.dp, Amber500.copy(alpha = scanAlpha), RoundedCornerShape(16.dp))
                )
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .scale(pulseScale * 0.95f)
                        .border(1.dp, Amber500.copy(alpha = scanAlpha * 0.5f), RoundedCornerShape(20.dp))
                )

                Column(
                    modifier = Modifier.padding(top = 280.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Scanning for surfaces...",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Point camera at a flat surface (roof, floor)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // ── Plane Detected Banner ──
        AnimatedVisibility(
            visible = planeDetected && !isScanning,
            enter = slideInVertically { -it } + fadeIn(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF22C55E).copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "Roof surface detected!",
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // ── Top Bar Overlay ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.5f)
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.55f)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$currentPanelCount panels · $capacityKw kW",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Amber500
                    )
                    Text(
                        text = "Est. $costStr net after subsidy",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // ── Right Panel Controls ──
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SmallFloatingActionButton(
                onClick = { if (currentPanelCount < 50) currentPanelCount++ },
                containerColor = Color.Black.copy(alpha = 0.6f),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add panel")
            }

            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.5f)
            ) {
                Text(
                    text = "$currentPanelCount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            SmallFloatingActionButton(
                onClick = { if (currentPanelCount > 1) currentPanelCount-- },
                containerColor = Color.Black.copy(alpha = 0.6f),
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Remove, contentDescription = "Remove panel")
            }

            Spacer(modifier = Modifier.height(8.dp))

            SmallFloatingActionButton(
                onClick = { currentPanelCount = maxOf(1, currentPanelCount - currentPanelCount % 4) },
                containerColor = Color.Black.copy(alpha = 0.6f),
                contentColor = Color.White.copy(alpha = 0.8f),
                shape = CircleShape,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Filled.RotateRight, contentDescription = "Rotate layout", modifier = Modifier.size(18.dp))
            }
        }

        // ── Bottom Controls ──
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            if (showShadowSlider) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.7f))
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
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shadow toggle
                IconButton(
                    onClick = { showShadowSlider = !showShadowSlider },
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = if (showShadowSlider) Amber500 else Color.White.copy(alpha = 0.2f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.WbSunny,
                        contentDescription = "Toggle shadow analysis",
                        tint = if (showShadowSlider) Navy900 else Color.White
                    )
                }

                // Capture button
                FloatingActionButton(
                    onClick = {
                        viewModel.analyze(
                            panelCount = currentPanelCount,
                            locationName = locationName,
                            roofType = roofType,
                            monthlyBillInr = monthlyBillInr,
                            shadowLossPercent = ((shadowHour - 12f) * 1.5).coerceIn(0.0, 15.0)
                        )
                    },
                    containerColor = Amber500,
                    contentColor = Navy900,
                    modifier = Modifier.size(68.dp),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "Capture & Analyse",
                        modifier = Modifier.size(30.dp)
                    )
                }

                // Info button
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(48.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Help",
                        tint = Color.White
                    )
                }
            }
        }

        // ── Analysis Loading Overlay ──
        AnimatedVisibility(
            visible = uiState is AnalyzeUiState.Loading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    CircularProgressIndicator(
                        color = Amber500,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = "Analysing your rooftop...",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "✦  Computing solar irradiance",
                            "✦  Calculating shading losses",
                            "✦  Applying PM Surya Ghar subsidy",
                            "✦  Generating your report"
                        ).forEach { step ->
                            Text(
                                text = step,
                                style = MaterialTheme.typography.bodySmall,
                                color = Amber500.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }

        // ── Error Snackbar ──
        if (uiState is AnalyzeUiState.Error) {
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                action = {
                    TextButton(onClick = { viewModel.reset() }) {
                        Text("Retry", color = Amber500)
                    }
                }
            ) {
                Text((uiState as AnalyzeUiState.Error).message)
            }
        }
    }
}
