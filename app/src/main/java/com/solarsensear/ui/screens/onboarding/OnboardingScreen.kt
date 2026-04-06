package com.solarsensear.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solarsensear.ui.theme.*
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val iconTint: Color,
    val gradientStart: Color,
    val gradientEnd: Color,
    val badge: String,
    val title: String,
    val subtitle: String
)

private val PAGES = listOf(
    OnboardingPage(
        icon = Icons.Filled.CameraAlt,
        iconTint = Amber500,
        gradientStart = Navy900,
        gradientEnd = Navy700,
        badge = "AUGMENTED REALITY",
        title = "Point & See Solar\nIn Real Life",
        subtitle = "Aim your camera at your rooftop and watch solar panels appear instantly in AR. No guesswork — see exactly where they'll go."
    ),
    OnboardingPage(
        icon = Icons.Filled.SolarPower,
        iconTint = Color(0xFF22C55E),
        gradientStart = Color(0xFF0A2218),
        gradientEnd = Color(0xFF0F3A25),
        badge = "AI ANALYSIS",
        title = "AI Calculates\nYour Savings",
        subtitle = "Our AI analyses roof depth, obstacles, and sun paths to predict your exact energy output — accurate to within 10% of actual generation."
    ),
    OnboardingPage(
        icon = Icons.Filled.CurrencyRupee,
        iconTint = Color(0xFFF5A623),
        gradientStart = Color(0xFF1A1200),
        gradientEnd = Color(0xFF2A1F00),
        badge = "PM SURYA GHAR",
        title = "Claim Up To\n₹78,000 Subsidy",
        subtitle = "We auto-calculate your PM Surya Ghar subsidy eligibility and generate a full cost, ROI, and payback report — in under 10 seconds."
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { PAGES.size })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPage(page = PAGES[page])
        }

        // Dots indicator
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(PAGES.size) { index ->
                val isSelected = pagerState.currentPage == index
                val width by animateFloatAsState(
                    targetValue = if (isSelected) 28f else 8f,
                    animationSpec = tween(300),
                    label = "dot_width"
                )
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(width.dp)
                        .background(
                            color = if (isSelected) Amber500 else Color.White.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )
            }
        }

        // Bottom action area
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (pagerState.currentPage == PAGES.lastIndex) {
                Button(
                    onClick = onFinish,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Amber500,
                        contentColor = Navy900
                    )
                ) {
                    Text(
                        text = "Get Started",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Filled.ArrowForward, contentDescription = null)
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onFinish) {
                        Text(
                            text = "Skip",
                            color = Color.White.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }

                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                        containerColor = Amber500,
                        contentColor = Navy900,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Filled.ArrowForward, contentDescription = "Next")
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(page: OnboardingPage) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val iconScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "icon_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(page.gradientStart, page.gradientEnd))
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .padding(top = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(iconScale)
                    .background(
                        color = page.iconTint.copy(alpha = 0.12f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            color = page.iconTint.copy(alpha = 0.18f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        tint = page.iconTint,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            // Badge
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600, delayMillis = 200)) +
                        slideInVertically(tween(500, 200)) { it / 2 }
            ) {
                Surface(
                    shape = CircleShape,
                    color = page.iconTint.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = page.badge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        color = page.iconTint,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            // Title
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(600, delayMillis = 300)) +
                        slideInVertically(tween(500, 300)) { it / 2 }
            ) {
                Text(
                    text = page.title,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 44.sp
                )
            }

            // Subtitle
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(700, delayMillis = 400)) +
                        slideInVertically(tween(600, 400)) { it / 2 }
            ) {
                Text(
                    text = page.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    lineHeight = 26.sp
                )
            }
        }
    }
}
