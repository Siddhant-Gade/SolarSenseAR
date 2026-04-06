package com.solarsensear.data.models

data class SolarReport(
    val id: String = "",
    val userId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationName: String = "",
    val state: String = "",
    val panelCount: Int = 0,
    val panelWatt: Int = 550,
    val roofType: String = "flat",
    val monthlyBillInr: Double = 2000.0,
    val capacityKw: Double = 0.0,
    val monthlyGenerationUnits: Int = 0,
    val annualGenerationUnits: Int = 0,
    val monthlyGenerationBreakdown: List<Int> = emptyList(), // Jan–Dec kWh
    val installationCostInr: Int = 0,
    val subsidyInr: Int = 0,
    val netCostInr: Int = 0,
    val paybackYears: Double = 0.0,
    val savings25yrInr: Int = 0,
    val annualSavingsInr: Int = 0,
    val co2KgAnnual: Int = 0,
    val treesEquivalent: Int = 0,
    val shadowLossPercent: Double = 0.0,
    val usageCoveragePercent: Int = 0,
    val irradianceKwhM2Day: Double = 5.5,
    val aiNarrative: String = "",
    val subsidyScheme: String = "PM Surya Ghar Muft Bijli Yojana",
    val arSnapshotPath: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
