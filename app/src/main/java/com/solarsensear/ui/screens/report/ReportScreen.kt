package com.solarsensear.ui.screens.report

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solarsensear.data.mock.MockData
import com.solarsensear.data.models.SolarReport
import com.solarsensear.ui.components.*
import com.solarsensear.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ReportScreen(
    reportIndex: Int,
    report: SolarReport? = null,
    onBack: () -> Unit
) {
    val displayReport = report ?: MockData.sampleReports.getOrNull(reportIndex)
        ?: MockData.sampleReports.first()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Summary", "Financials", "Energy", "Actions")
    val tabIcons = listOf(Icons.Filled.Dashboard, Icons.Filled.CurrencyRupee, Icons.Filled.Bolt, Icons.Filled.TaskAlt)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            displayReport.locationName,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "${displayReport.panelCount} panels · ${displayReport.capacityKw} kW · ${displayReport.roofType} roof",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Animated Tab Row ──
            TabRow(
                selectedTabIndex = selectedTab,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        height = 3.dp,
                        color = Amber500
                    )
                },
                containerColor = MaterialTheme.colorScheme.surface,
                divider = { Divider(thickness = 0.5.dp) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                tabIcons[index],
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        text = {
                            Text(
                                title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selectedContentColor = Amber500,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    slideInHorizontally(
                        animationSpec = tween(280),
                        initialOffsetX = { if (targetState > initialState) it else -it }
                    ) + fadeIn(tween(200)) togetherWith
                    slideOutHorizontally(
                        animationSpec = tween(280),
                        targetOffsetX = { if (targetState > initialState) -it else it }
                    ) + fadeOut(tween(150))
                },
                label = "tab_transition"
            ) { tab ->
                when (tab) {
                    0 -> SummaryTab(displayReport)
                    1 -> FinancialsTab(displayReport)
                    2 -> EnergyTab(displayReport)
                    3 -> ActionsTab(displayReport)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 1 — SUMMARY
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun SummaryTab(report: SolarReport) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero gradient banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Navy800, Navy700)))
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "25-Year Savings",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    formatHero(report.savings25yrInr),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Amber500
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Payback in ${String.format("%.1f", report.paybackYears)} years",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Quick stats 2×2 grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryStatCard("Net Cost", "₹${fmtLakh(report.netCostInr)}", Icons.Filled.CurrencyRupee, Amber500, Modifier.weight(1f))
                SummaryStatCard("Annual Savings", "₹${fmtLakh(report.annualSavingsInr)}", Icons.Filled.AccountBalance, Success, Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SummaryStatCard("Capacity", "${report.capacityKw} kW", Icons.Filled.SolarPower, Info, Modifier.weight(1f))
                SummaryStatCard("Coverage", "${report.usageCoveragePercent}%", Icons.Filled.WbSunny, Warning, Modifier.weight(1f))
            }

            // Payback progress
            ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Investment Payback", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    PaybackProgressBar(paybackYears = report.paybackYears)
                }
            }

            // AI Narrative card
            if (report.aiNarrative.isNotBlank()) {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp).padding(top = 2.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "AI Analysis",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Text(
                                report.aiNarrative,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            // System details row
            ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("System Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Divider()
                    DetailRow("Solar Irradiance", "${report.irradianceKwhM2Day} kWh/m²/day")
                    DetailRow("Shadow Loss", "${report.shadowLossPercent}%")
                    DetailRow("Panel Rating", "${report.panelWatt}W each")
                    DetailRow("Subsidy Scheme", report.subsidyScheme)
                    DetailRow("State", report.state)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 2 — FINANCIALS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun FinancialsTab(report: SolarReport) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        // Cost breakdown stacked bar (waterfall style)
        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Cost Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                CostWaterfallBar(
                    grossCost = report.installationCostInr,
                    subsidy = report.subsidyInr,
                    netCost = report.netCostInr
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    LegendItem("Gross Cost", "₹${fmtLakh(report.installationCostInr)}", Navy500)
                    LegendItem("Subsidy", "-₹${fmtLakh(report.subsidyInr)}", Success)
                    LegendItem("Net Cost", "₹${fmtLakh(report.netCostInr)}", Amber500)
                }
            }
        }

        // Subsidy slab breakdown
        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                SubsidyBreakdownBar(capacityKw = report.capacityKw)
            }
        }

        // 25-yr cumulative savings line chart
        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("25-Year Savings Trajectory", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    "Cumulative savings vs. initial investment",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                SavingsLineChart(
                    annualSavingsInr = report.annualSavingsInr,
                    netCostInr = report.netCostInr,
                    heightDp = 150.dp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Year 0", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text(
                        "Break-even @ ${String.format("%.1f", report.paybackYears)}y",
                        style = MaterialTheme.typography.labelSmall,
                        color = Success,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("Year 25", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }

        // Financial KPI cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FinanceKpiCard("Annual Savings", "₹${fmtLakh(report.annualSavingsInr)}", Success, Modifier.weight(1f))
            FinanceKpiCard("25-Yr Total", formatHero(report.savings25yrInr), Amber500, Modifier.weight(1f))
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 3 — ENERGY
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun EnergyTab(report: SolarReport) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        // Coverage donut + key stats
        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                CoverageDonut(
                    coveragePercent = report.usageCoveragePercent,
                    size = 110.dp
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    EnergyStatRow("Monthly Output", "${report.monthlyGenerationUnits} kWh")
                    EnergyStatRow("Annual Output", "${report.annualGenerationUnits} kWh")
                    EnergyStatRow("Irradiance", "${report.irradianceKwhM2Day} kWh/m²/d")
                    EnergyStatRow("Shadow Loss", "${report.shadowLossPercent}%")
                }
            }
        }

        // Monthly generation bar chart
        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Monthly Generation Forecast",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Estimated kWh per month (Jan–Dec)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val breakdown = report.monthlyGenerationBreakdown.takeIf { it.size == 12 }
                    ?: List(12) { report.monthlyGenerationUnits }
                MonthlyBarChart(values = breakdown, heightDp = 150.dp)
            }
        }

        // Environmental impact
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Environmental Impact",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    EcoStatCard(
                        icon = Icons.Filled.Park,
                        value = "${report.treesEquivalent}",
                        label = "Trees/yr",
                        tint = Success,
                        modifier = Modifier.weight(1f)
                    )
                    EcoStatCard(
                        icon = Icons.Filled.Air,
                        value = "${report.co2KgAnnual} kg",
                        label = "CO₂ saved/yr",
                        tint = Info,
                        modifier = Modifier.weight(1f)
                    )
                    EcoStatCard(
                        icon = Icons.Filled.WbSunny,
                        value = "${String.format("%.1f", report.irradianceKwhM2Day)}",
                        label = "kWh/m²/d",
                        tint = Amber500,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TAB 4 — ACTIONS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun ActionsTab(report: SolarReport) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        Text(
            "Next Steps",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        ActionCard(
            icon = Icons.Filled.Verified,
            iconTint = Success,
            title = "Apply for PM Surya Ghar Subsidy",
            subtitle = "Claim up to ₹${fmtLakh(report.subsidyInr)} subsidy at pmsuryaghar.gov.in",
            cta = "Apply Now",
            onClick = { /* open URL */ }
        )

        ActionCard(
            icon = Icons.Filled.Engineering,
            iconTint = Info,
            title = "Find Certified Installers",
            subtitle = "Get quotes from top-rated solar installers near ${report.locationName}",
            cta = "Browse Installers",
            onClick = { /* navigate to vendors */ }
        )

        ActionCard(
            icon = Icons.Filled.DownloadForOffline,
            iconTint = Amber500,
            title = "Download Report PDF",
            subtitle = "Save a detailed PDF with financials, energy data and subsidy info",
            cta = "Download",
            onClick = { /* generate PDF */ }
        )

        ActionCard(
            icon = Icons.Filled.Share,
            iconTint = Navy400,
            title = "Share This Analysis",
            subtitle = "Send results to family or your installer for review",
            cta = "Share",
            onClick = { /* share intent */ }
        )

        // Tip card
        ElevatedCard(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Filled.Lightbulb, null, tint = Amber500, modifier = Modifier.size(20.dp))
                Column {
                    Text("Pro Tip", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "South-facing panels on a ${report.roofType} roof in ${report.locationName} " +
                        "generate up to 20% more power. Ensure no shade from trees or structures.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SHARED SUB-COMPOSABLES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SummaryStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier, shape = RoundedCornerShape(14.dp)) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = tint)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CostWaterfallBar(grossCost: Int, subsidy: Int, netCost: Int) {
    val total = grossCost.toFloat()
    val grossFraction = 1f
    val subsidyFraction = subsidy / total
    val netFraction = netCost / total

    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(900, easing = EaseOut),
        label = "waterfall"
    )

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        // Gross cost bar
        WaterfallRow("Gross", grossFraction * animProgress, Navy500, "₹${fmtLakh(grossCost)}")
        // Subsidy bar
        WaterfallRow("Subsidy", subsidyFraction * animProgress, Success, "-₹${fmtLakh(subsidy)}")
        // Net cost bar
        WaterfallRow("Net", netFraction * animProgress, Amber500, "₹${fmtLakh(netCost)}")
    }
}

@Composable
private fun WaterfallRow(label: String, fraction: Float, color: Color, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(40.dp), color = Color.Gray)
        Box(
            modifier = Modifier
                .weight(fraction.coerceAtLeast(0.02f))
                .height(18.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        if (fraction < 0.98f) Spacer(Modifier.weight((1f - fraction).coerceAtLeast(0f)))
        Text(amount, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun LegendItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(2.dp)))
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, textAlign = TextAlign.Center)
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.Center)
    }
}

@Composable
private fun FinanceKpiCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier, shape = RoundedCornerShape(14.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, textAlign = TextAlign.Center)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = color, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun EnergyStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun EcoStatCard(icon: ImageVector, value: String, label: String, tint: Color, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(22.dp))
            Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = tint, textAlign = TextAlign.Center)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, textAlign = TextAlign.Center, fontSize = 10.sp)
        }
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    cta: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = iconTint.copy(alpha = 0.12f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FilledTonalButton(
                onClick = onClick,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(cta, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// FORMATTERS
// ─────────────────────────────────────────────────────────────────────────────

private fun fmtLakh(amount: Int): String = when {
    amount >= 100000 -> "${String.format("%.1f", amount / 100000.0)}L"
    amount >= 1000   -> "${amount / 1000}K"
    else             -> "$amount"
}

private fun formatHero(amount: Int): String = when {
    amount >= 10000000 -> "₹${String.format("%.2f", amount / 10000000.0)} Cr"
    amount >= 100000   -> "₹${String.format("%.1f", amount / 100000.0)}L"
    else               -> "₹$amount"
}