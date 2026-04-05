package com.solarsensear.data.repository

import com.solarsensear.data.models.ReportRequest
import com.solarsensear.data.models.ReportResponse
import com.solarsensear.data.models.SolarReport
import com.solarsensear.domain.SolarCalculator
import com.solarsensear.network.RetrofitClient

class ReportRepository {

    private val api = RetrofitClient.apiService

    /**
     * Generates a solar report via the backend API.
     * Falls back to local calculation if the network call fails.
     */
    suspend fun generateReport(request: ReportRequest): Result<SolarReport> {
        return try {
            val response = api.generateReport(request)
            val report = response.toSolarReport(request)
            // TODO: Phase 3 — Save to Firestore for history
            Result.success(report)
        } catch (e: Exception) {
            // Offline fallback: local calculation
            val localReport = SolarCalculator.calculate(
                panelCount = request.panelCount,
                panelWatt = request.panelWatt,
                irradiance = 5.5, // Default if NASA API unavailable
                monthlyBillInr = request.monthlyBillInr,
                state = request.state,
                locationName = ""
            )
            Result.success(localReport.copy(
                aiNarrative = "Report generated offline. Connect to the internet for AI-powered analysis.",
                latitude = request.latitude,
                longitude = request.longitude
            ))
        }
    }

    /** Pings the backend to wake up Render free-tier container. */
    suspend fun warmUp(): Boolean {
        return try {
            api.healthCheck()
            true
        } catch (e: Exception) {
            false
        }
    }
}

/** Maps API response to domain SolarReport model. */
private fun ReportResponse.toSolarReport(request: ReportRequest): SolarReport {
    return SolarReport(
        latitude = request.latitude,
        longitude = request.longitude,
        state = request.state,
        panelCount = request.panelCount,
        panelWatt = request.panelWatt,
        roofType = request.roofType,
        monthlyBillInr = request.monthlyBillInr,
        capacityKw = capacityKw,
        monthlyGenerationUnits = monthlyGenerationUnits,
        annualGenerationUnits = annualGenerationUnits,
        installationCostInr = installationCostInr,
        subsidyInr = subsidyInr,
        netCostInr = netCostInr,
        paybackYears = paybackYears,
        savings25yrInr = savings25yrInr,
        co2KgAnnual = co2KgAnnual,
        treesEquivalent = treesEquivalent,
        irradianceKwhM2Day = irradianceKwhM2Day,
        aiNarrative = aiNarrative,
        subsidyScheme = subsidyScheme
    )
}
