package com.solarsensear.domain

import android.content.Context
import com.solarsensear.data.local.IrradianceDbHelper
import com.solarsensear.data.models.SolarReport

/**
 * Offline-first solar report generator.
 * Uses bundled irradiance_india.json when the backend is unreachable.
 * This ensures the demo always works, even in airplane mode.
 */
object LocalSolarCalculator {

    fun calculate(
        context: Context,
        panelCount: Int,
        panelWatt: Int,
        monthlyBillInr: Double,
        city: String,
        roofType: String = "flat"
    ): SolarReport {
        val irradiance = IrradianceDbHelper.getIrradiance(context, city)
        val state = IrradianceDbHelper.getState(context, city)

        return SolarCalculator.calculate(
            panelCount = panelCount,
            panelWatt = panelWatt,
            irradiance = irradiance,
            monthlyBillInr = monthlyBillInr,
            state = state,
            locationName = city
        ).copy(
            roofType = roofType,
            aiNarrative = generateOfflineNarrative(
                city = city,
                state = state,
                irradiance = irradiance,
                panelCount = panelCount,
                panelWatt = panelWatt,
                monthlyBillInr = monthlyBillInr
            )
        )
    }

    /**
     * Generates a basic AI-style narrative offline.
     * Not as good as Gemini, but ensures the report always has a summary.
     */
    private fun generateOfflineNarrative(
        city: String,
        state: String,
        irradiance: Double,
        panelCount: Int,
        panelWatt: Int,
        monthlyBillInr: Double
    ): String {
        val capacityKw = (panelCount * panelWatt) / 1000.0
        val monthlyGen = (capacityKw * irradiance * 0.75 * 30).toInt()
        val coveragePercent = if (monthlyBillInr > 0) {
            ((monthlyGen * 8.0) / monthlyBillInr * 100).toInt().coerceAtMost(100)
        } else 0

        return buildString {
            append("Based on $city, ${state}'s solar irradiance of $irradiance kWh/m²/day, ")
            append("your $capacityKw kW system with $panelCount panels ")
            append("will generate approximately $monthlyGen units of electricity every month. ")
            if (coveragePercent >= 80) {
                append("This covers nearly your entire ₹${monthlyBillInr.toInt()} monthly bill. ")
            } else {
                append("This covers about $coveragePercent% of your ₹${monthlyBillInr.toInt()} monthly bill. ")
            }
            append("We recommend applying for the PM Surya Ghar Muft Bijli Yojana at pmsuryaghar.gov.in to claim your subsidy.")
        }
    }
}
