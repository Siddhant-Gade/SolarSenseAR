package com.solarsensear.ui.screens.report

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlin.math.roundToInt

private val MONTHS_FULL = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = displayReport.locationName.ifBlank { "Solar Report" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${displayReport.panelCount} panels · ${String.format("%.1f", displayReport.capacityKw)} kW",
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
            // Tab row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Amber500,
                        height = 3.dp
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    slideInHorizontally { if (targetState > initialState) it else -it } +
                        fadeIn() togetherWith
                    slideOutHorizontally { if (targetState > initialState) -it else it } +
                        fadeOut()
                },
                label = "tab_content"
            ) { tab ->
                when (tab) {
                    0 -> SummaryTab(report = displayReport)
                    1 -> FinancialsTab(report = displayReport)
                    2 -> EnergyTab(report = displayReport)
                    3 -> ActionsTab(report = displayReport)
                }
            }
        }
    }
}

@Composable
private fun SummaryTab(report: SolarReport) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero savings card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(listOf(Navy800, Navy600))
                )
                .padding(24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "25-Year Savings",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 1.sp
                )
                Text(
                    text = formatInrHero(report.savings25yrInr),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = Amber500,
                    lineHeight = 44.sp
                )
                Text(
                    text = "Net of ₹${formatInr(report.netCostInr)} investment",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }

        // Key metrics grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricChip(
                icon = Icons.Filled.SolarPower,
                label = "Panels",
                value = "${report.panelCount}",
                modifier = Modifier.weight(1f)
            )
            MetricChip(
                icon = Icons.Filled.ElectricBolt,
                label = "System Size",
                value = "${String.format("%.1f", report.capacityKw)} kW",
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricChip(
                icon = Icons.Filled.Timer,
                label = "Payback",
                value = "${String.format("%.1f", report.paybackYears)} yrs",
                iconTint = Amber500,
                modifier = Modifier.weight(1f)
            )
            MetricChip(
                icon = Icons.Filled.Park,
                label = "CO₂ Saved",
                value = "${report.co2KgAnnual} kg/yr",
                iconTint = Success,
                modifier = Modifier.weight(1f)
            )
        }

        // AI Narrative card
        if (report.aiNarrative.isNotBlank()) {
            ElevatedCard(
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = Amber500,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "AI Analysis",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Amber500
                        )
                    }
                    Text(
                        text = report.aiNarrative,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Payback progress
        ElevatedCard(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Investment Recovery", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                PaybackProgressBar(
                    paybackYears = report.paybackYears,
                    barColor = Amber500
                )
            }
        }
    }
}

@Composable
private fun FinancialsTab(report: SolarReport) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Subsidy badge
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Success.copy(alpha = 0.1f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = Success, modifier = Modifier.size(28.dp))
                Column {
                    Text(
                        text = "PM Surya Ghar Subsidy Applied",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Success
                    )
                    Text(
                        text = "₹${formatInr(report.subsidyInr)} government subsidy deducted",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Cost breakdown card
        ElevatedCard(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Cost Breakdown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                CostRow("Gross Installation Cost", "₹${formatInr(report.installationCostInr)}", Color.Gray)
                CostRow("PM Surya Ghar Subsidy", "−₹${formatInr(report.subsidyInr)}", Success)
                HorizontalDivider()
                CostRow("Net Investment", "₹${formatInr(report.netCostInr)}", Amber500, bold = true)
                CostRow("Annual Savings", "₹${formatInr(report.annualSavingsInr)}/yr", Success, bold = true)
                CostRow("25-Year ROI", "₹${formatInr(report.savings25yrInr)}", MaterialTheme.colorScheme.primary, bold = true)
            }
        }

        // 25-year savings chart
        ElevatedCard(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Cumulative Savings (25 years)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                SavingsLineChart(
                    annualSavingsInr = report.annualSavingsInr,
                    netCostInr = report.netCostInr
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Year 0", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("Year 25", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(10.dp).background(Success.copy(alpha = 0.7f), CircleShape))
                    Text("Cumulative profit", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun EnergyTab(report: SolarReport) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Energy overview row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ElevatedCard(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${report.monthlyGenerationUnits}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Amber500
                    )
                    Text("units/month", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("Monthly Generation", style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                }
            }
            ElevatedCard(modifier = Modifier.weight(1f), shape = RoundedCornerShape(16.dp)) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${report.annualGenerationUnits}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Amber500
                    )
                    Text("units/year", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Text("Annual Generation", style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
                }
            }
        }

        // Coverage donut + details
        ElevatedCard(shape = RoundedCornerShape(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CoverageDonut(
                    coveragePercent = report.usageCoveragePercent,
                    primaryColor = Success
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Covers ${report.usageCoveragePercent}% of your usage",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (report.shadowLossPercent > 0) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.WbCloudy, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                            Text(
                                "${String.format("%.1f", report.shadowLossPercent)}% shadow loss",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFF59E0B)
                            )
                        }
                    }
                    Text(
                        "${String.format("%.1f", report.irradianceKwhM2Day)} kWh/m²/day irradiance",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Monthly generation chart
        ElevatedCard(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Monthly Generation (kWh)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                val breakdown = report.monthlyGenerationBreakdown.ifEmpty {
                    // Generate from annual average if no breakdown
                    val avg = report.monthlyGenerationUnits
                    listOf(82,90,105,115,118,110,98,95,100,108,90,79).map { (avg * it / 100) }
                }
                MonthlyBarChart(values = breakdown, barColor = Amber500)

                // Peak & low month
                val peakIdx = breakdown.indices.maxByOrNull { breakdown[it] } ?: 4
                val lowIdx = breakdown.indices.minByOrNull { breakdown[it] } ?: 11
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Peak: ${MONTHS_FULL[peakIdx]} (${breakdown[peakIdx]} kWh)",
                        style = MaterialTheme.typography.labelSmall, color = Amber500)
                    Text("Lowest: ${MONTHS_FULL[lowIdx]} (${breakdown[lowIdx]} kWh)",
                        style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
        }

        // Environmental impact
        ElevatedCard(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Environmental Impact", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${report.co2KgAnnual}", style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold, color = Success)
                        Text("kg CO₂/year", style = MaterialTheme.typography.labelSmall, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${report.treesEquivalent}", style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold, color = Success)
                        Text("trees equivalent", style = MaterialTheme.typography.labelSmall, color = Color.Gray, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionsTab(report: SolarReport) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Next Steps",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        ActionCard(
            icon = Icons.Filled.PictureAsPdf,
            title = "Download PDF Report",
            subtitle = "Full financial & energy breakdown with subsidy details",
            tint = Error,
            onClick = { /* trigger PDF download */ }
        )
        ActionCard(
            icon = Icons.Filled.Share,
            title = "Share AR Screenshot",
            subtitle = "Share your solar plan with family or contractor",
            tint = Info,
            onClick = { /* share intent */ }
        )
        ActionCard(
            icon = Icons.Filled.Store,
            title = "Find Local Installers",
            subtitle = "Connect with verified solar installers near you",
            tint = Success,
            onClick = { /* navigate to vendors */ }
        )
        ActionCard(
            icon = Icons.Filled.AccountBalance,
            title = "Apply for PM Surya Ghar",
            subtitle = "Claim ₹${formatInr(report.subsidyInr)} subsidy at pmsuryaghar.gov.in",
            tint = Amber500,
            onClick = { /* deeplink */ }
        )
        ActionCard(
            icon = Icons.Filled.CreditCard,
            title = "Solar Loan",
            subtitle = "0% interest solar financing from partner NBFCs",
            tint = MaterialTheme.colorScheme.primary,
            onClick = { /* loan deeplink */ }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick stats footer
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Quick Reference", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Text("• Location: ${report.locationName}", style = MaterialTheme.typography.bodySmall)
                Text("• System: ${report.panelCount} panels · ${String.format("%.1f", report.capacityKw)} kW", style = MaterialTheme.typography.bodySmall)
                Text("• Net Cost after Subsidy: ₹${formatInr(report.netCostInr)}", style = MaterialTheme.typography.bodySmall)
                Text("• Payback Period: ${String.format("%.1f", report.paybackYears)} years", style = MaterialTheme.typography.bodySmall)
                Text("• Annual Generation: ${report.annualGenerationUnits} kWh", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// ── Helper Composables ──

@Composable
private fun MetricChip(
    icon: ImageVector,
    label: String,
    value: String,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier, shape = RoundedCornerShape(12.dp)) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun CostRow(label: String, value: String, valueColor: Color, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            color = valueColor,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    tint: Color,
    onClick: () -> Unit
) {
    ElevatedCard(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(tint.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(
                Icons.AutoMirrored.Filled.TrendingUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun formatInr(amount: Int): String {
    return when {
        amount >= 10000000 -> "${String.format("%.1f", amount / 10000000.0)}Cr"
        amount >= 100000  -> "${String.format("%.1f", amount / 100000.0)}L"
        amount >= 1000    -> "${amount / 1000}K"
        else              -> "$amount"
    }
}

private fun formatInrHero(amount: Int): String {
    return when {
        amount >= 10000000 -> "₹${String.format("%.2f", amount / 10000000.0)} Crore"
        amount >= 100000  -> "₹${String.format("%.1f", amount / 100000.0)} Lakh"
        else              -> "₹$amount"
    }
}
