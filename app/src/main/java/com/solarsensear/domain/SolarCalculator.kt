package com.solarsensear.domain

import com.solarsensear.data.models.SolarReport

object SolarCalculator {

    private const val COST_PER_WATT_INR = 60      // ₹60/W current Indian market rate
    private const val ELECTRICITY_RATE_INR = 8     // ₹8/unit average across India
    private const val CO2_PER_UNIT_KG = 0.82       // India's grid emission factor (CEA 2023)
    private const val KG_CO2_PER_TREE_PER_YEAR = 84.0 // 1 tree absorbs ~84 kg CO₂/year
    private const val PERFORMANCE_RATIO = 0.75     // India standard PR

    fun calculate(
        panelCount: Int,
        panelWatt: Int,
        irradiance: Double,
        monthlyBillInr: Double,
        state: String = "",
        locationName: String = ""
    ): SolarReport {
        val capacityW = panelCount * panelWatt
        val capacityKw = capacityW / 1000.0

        // Energy generation
        val dailyGenKwh = capacityKw * irradiance * PERFORMANCE_RATIO
        val monthlyGen = (dailyGenKwh * 30).toInt()
        val annualGen = monthlyGen * 12

        // Financials
        val installationCost = (capacityW * COST_PER_WATT_INR)
        val subsidy = SubsidyCalculator.calculate(capacityKw)
        val netCost = installationCost - subsidy

        val monthlySavings = minOf(
            (monthlyGen * ELECTRICITY_RATE_INR).toDouble(),
            monthlyBillInr
        )
        val annualSavings = monthlySavings * 12
        val paybackYears = if (annualSavings > 0) {
            (netCost.toDouble() / annualSavings * 10).toInt() / 10.0
        } else {
            99.0
        }
        val savings25yr = (annualSavings * 25 - netCost).toInt()

        // Environmental
        val co2Annual = (annualGen * CO2_PER_UNIT_KG).toInt()
        val treesEquiv = (co2Annual / KG_CO2_PER_TREE_PER_YEAR).toInt()

        return SolarReport(
            panelCount = panelCount,
            panelWatt = panelWatt,
            state = state,
            locationName = locationName,
            monthlyBillInr = monthlyBillInr,
            capacityKw = Math.round(capacityKw * 100) / 100.0,
            monthlyGenerationUnits = monthlyGen,
            annualGenerationUnits = annualGen,
            installationCostInr = installationCost,
            subsidyInr = subsidy,
            netCostInr = netCost,
            paybackYears = paybackYears,
            savings25yrInr = savings25yr,
            co2KgAnnual = co2Annual,
            treesEquivalent = treesEquiv,
            irradianceKwhM2Day = irradiance
        )
    }
}
