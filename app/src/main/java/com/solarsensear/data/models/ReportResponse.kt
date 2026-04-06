package com.solarsensear.data.models

import com.google.gson.annotations.SerializedName

data class ReportResponse(
    @SerializedName("capacity_kw")
    val capacityKw: Double,
    @SerializedName("monthly_generation_units")
    val monthlyGenerationUnits: Int,
    @SerializedName("annual_generation_units")
    val annualGenerationUnits: Int,
    @SerializedName("annual_savings_inr")
    val annualSavingsInr: Int,   // Fix 14: was missing — backend could never populate this
    @SerializedName("installation_cost_inr")
    val installationCostInr: Int,
    @SerializedName("subsidy_inr")
    val subsidyInr: Int,
    @SerializedName("net_cost_inr")
    val netCostInr: Int,
    @SerializedName("payback_years")
    val paybackYears: Double,
    @SerializedName("savings_25yr_inr")
    val savings25yrInr: Int,
    @SerializedName("co2_kg_annual")
    val co2KgAnnual: Int,
    @SerializedName("trees_equivalent")
    val treesEquivalent: Int,
    @SerializedName("irradiance_kwh_m2_day")
    val irradianceKwhM2Day: Double,
    @SerializedName("ai_narrative")
    val aiNarrative: String,
    @SerializedName("subsidy_scheme")
    val subsidyScheme: String,
    @SerializedName("subsidy_breakdown")
    val subsidyBreakdown: SubsidyBreakdown
)

data class SubsidyBreakdown(
    @SerializedName("up_to_1kw")
    val upTo1kw: Int,
    @SerializedName("1_to_2kw")
    val oneTo2kw: Int,
    @SerializedName("above_2kw")
    val above2kw: Int
)
