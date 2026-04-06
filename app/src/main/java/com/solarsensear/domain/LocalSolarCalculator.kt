package com.solarsensear.domain

/**
 * Complete solar energy + financial calculation engine.
 * Uses PVGIS-aligned irradiance values for Indian cities and
 * PM Surya Ghar subsidy slabs (2024 policy).
 */
object LocalSolarCalculator {

    private const val PANEL_WATT = 550          // Standard panel wattage (W)
    private const val COST_PER_KW = 60000       // Installation cost per kW (INR)
    private const val GRID_RATE_PER_UNIT = 8.0  // Average Indian grid tariff (₹/kWh)
    private const val PERFORMANCE_RATIO = 0.80  // System performance ratio (derating)
    private const val CO2_PER_KWH = 0.0816      // kg CO2 / kWh (India grid factor)
    private const val KWH_PER_TREE = 22.0       // kWh offset equivalent per tree per year

    // Monthly irradiance distribution factors (relative to annual average)
    private val MONTHLY_IRRADIANCE_FACTOR = doubleArrayOf(
        0.82, 0.90, 1.05, 1.15, 1.18, 1.10, // Jan–Jun
        0.98, 0.95, 1.00, 1.08, 0.90, 0.79  // Jul–Dec
    )

    // PVGIS-aligned irradiance table: city → avg daily kWh/m²/day
    private val CITY_IRRADIANCE = mapOf(
        "mumbai"      to 5.52, "delhi"       to 5.61, "bangalore"   to 5.83,
        "bengaluru"   to 5.83, "hyderabad"   to 5.82, "chennai"     to 5.94,
        "kolkata"     to 5.00, "pune"        to 5.55, "ahmedabad"   to 5.99,
        "jaipur"      to 5.97, "nagpur"      to 5.80, "lucknow"     to 5.40,
        "bhopal"      to 5.65, "surat"       to 5.70, "indore"      to 5.69,
        "patna"       to 5.30, "chandigarh"  to 5.20, "coimbatore"  to 5.88,
        "kochi"       to 5.10, "vizag"       to 5.70
    )

    data class SolarCalculationResult(
        val capacityKw: Double,
        val monthlyGenerationUnits: Int,
        val annualGenerationUnits: Int,
        val monthlyBreakdown: List<Int>,
        val installationCostInr: Int,
        val subsidyInr: Int,
        val netCostInr: Int,
        val annualSavingsInr: Int,
        val paybackYears: Double,
        val savings25yrInr: Int,
        val co2KgAnnual: Int,
        val treesEquivalent: Int,
        val shadowLossPercent: Double,
        val usageCoveragePercent: Int,
        val irradianceKwhM2Day: Double,
        val aiNarrative: String,
        val subsidyScheme: String = "PM Surya Ghar Muft Bijli Yojana"
    )

    fun calculate(
        panelCount: Int,
        locationName: String,
        roofType: String = "flat",
        monthlyBillInr: Double = 2000.0,
        shadowLossPercent: Double = 0.0
    ): SolarCalculationResult {
        val irradiance = resolveIrradiance(locationName)
        val capacityKw = (panelCount * PANEL_WATT) / 1000.0

        // Tilt efficiency adjustment
        val tiltFactor = if (roofType == "sloped") 1.05 else 1.0

        // Annual generation (kWh)
        val annualGenKwh = capacityKw * irradiance * 365 * PERFORMANCE_RATIO * tiltFactor *
                (1 - shadowLossPercent / 100.0)

        // Monthly breakdown — normalize factors so they always sum to 12.0
        // (raw factors sum to 11.9, which causes breakdown.sum() != annualGenUnits)
        val factorSum = MONTHLY_IRRADIANCE_FACTOR.sum()
        val monthlyBreakdown = MONTHLY_IRRADIANCE_FACTOR.map { factor ->
            ((annualGenKwh * (factor / factorSum))).toInt()
        }
        val monthlyGenUnits = monthlyBreakdown.average().toInt()
        val annualGenUnits = monthlyBreakdown.sum()

        // Financial
        val installationCost = (capacityKw * COST_PER_KW).toInt()
        val subsidy = SubsidyCalculator.calculate(capacityKw)
        val netCost = installationCost - subsidy
        val annualSavings = (annualGenKwh * GRID_RATE_PER_UNIT).toInt()
        val paybackYears = if (annualSavings > 0) netCost.toDouble() / annualSavings else 0.0
        val savings25yr = (annualSavings * 25) - netCost

        // Environmental
        val co2Annual = (annualGenKwh * CO2_PER_KWH).toInt()
        val treesEquiv = (annualGenKwh / KWH_PER_TREE).toInt()

        // Usage coverage
        val estimatedMonthlyUsage = (monthlyBillInr / GRID_RATE_PER_UNIT).toInt()
        val usageCoverage = if (estimatedMonthlyUsage > 0)
            ((monthlyGenUnits.toDouble() / estimatedMonthlyUsage) * 100).coerceAtMost(100.0).toInt()
        else 85

        val narrative = buildNarrative(
            locationName = locationName,
            irradiance = irradiance,
            capacityKw = capacityKw,
            panelCount = panelCount,
            monthlyGenUnits = monthlyGenUnits,
            netCostInr = netCost,
            subsidyInr = subsidy,
            paybackYears = paybackYears,
            savings25yr = savings25yr
        )

        return SolarCalculationResult(
            capacityKw = String.format("%.2f", capacityKw).toDouble(),
            monthlyGenerationUnits = monthlyGenUnits,
            annualGenerationUnits = annualGenUnits,
            monthlyBreakdown = monthlyBreakdown,
            installationCostInr = installationCost,
            subsidyInr = subsidy,
            netCostInr = netCost,
            annualSavingsInr = annualSavings,
            paybackYears = String.format("%.1f", paybackYears).toDouble(),
            savings25yrInr = savings25yr,
            co2KgAnnual = co2Annual,
            treesEquivalent = treesEquiv,
            shadowLossPercent = shadowLossPercent,
            usageCoveragePercent = usageCoverage,
            irradianceKwhM2Day = irradiance,
            aiNarrative = narrative
        )
    }

    fun resolveIrradiance(locationName: String): Double {
        val key = locationName.lowercase().trim()
        return CITY_IRRADIANCE[key]
            ?: CITY_IRRADIANCE.entries.firstOrNull { key.contains(it.key) }?.value
            ?: 5.5 // National average fallback
    }

    fun optimalPanelCount(roofAreaSqFt: Double, monthlyBillInr: Double): Int {
        // Each panel needs ~35 sq ft. Bill of ₹3000 ≈ 3000/8 = 375 kWh/month
        val maxByArea = (roofAreaSqFt / 35.0).toInt().coerceIn(4, 50)
        val monthlyUsageKwh = monthlyBillInr / GRID_RATE_PER_UNIT
        val kwhPerPanel = (5.5 * 0.55 * 30 * PERFORMANCE_RATIO)  // avg irradiance
        val neededByBill = (monthlyUsageKwh / kwhPerPanel).toInt().coerceIn(4, 50)
        return minOf(maxByArea, neededByBill).coerceAtLeast(4)
    }

    private fun buildNarrative(
        locationName: String,
        irradiance: Double,
        capacityKw: Double,
        panelCount: Int,
        monthlyGenUnits: Int,
        netCostInr: Int,
        subsidyInr: Int,
        paybackYears: Double,
        savings25yr: Int
    ): String {
        val city = locationName.ifBlank { "your location" }
        val subsidy = formatInr(subsidyInr)
        val netCost = formatInr(netCostInr)
        val savings = formatInr(savings25yr)
        return "Based on ${city}'s solar irradiance of ${irradiance} kWh/m²/day, " +
            "your ${String.format("%.1f", capacityKw)} kW system with $panelCount panels will generate " +
            "approximately $monthlyGenUnits units of electricity every month. " +
            "After the PM Surya Ghar subsidy of $subsidy, your net investment is just $netCost — " +
            "which pays for itself in ${String.format("%.1f", paybackYears)} years. " +
            "Over 25 years, you stand to save $savings. " +
            "We strongly recommend applying for the PM Surya Ghar Muft Bijli Yojana at pmsuryaghar.gov.in to claim your subsidy."
    }

    private fun formatInr(amount: Int): String {
        return when {
            amount >= 100000 -> "₹${String.format("%.1f", amount / 100000.0)}L"
            amount >= 1000   -> "₹${amount / 1000}K"
            else             -> "₹$amount"
        }
    }
}
