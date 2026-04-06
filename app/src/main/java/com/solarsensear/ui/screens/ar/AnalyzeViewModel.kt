package com.solarsensear.ui.screens.ar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solarsensear.data.models.SolarReport
import com.solarsensear.domain.LocalSolarCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class AnalyzeUiState {
    data object Idle : AnalyzeUiState()
    data object Loading : AnalyzeUiState()
    data class Success(val report: SolarReport) : AnalyzeUiState()
    data class Error(val message: String) : AnalyzeUiState()
}

class AnalyzeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<AnalyzeUiState>(AnalyzeUiState.Idle)
    val uiState: StateFlow<AnalyzeUiState> = _uiState

    fun analyze(
        panelCount: Int,
        locationName: String,
        roofType: String,
        monthlyBillInr: Double,
        shadowLossPercent: Double = 5.0
    ) {
        viewModelScope.launch {
            _uiState.value = AnalyzeUiState.Loading
            // Simulate network + AI processing latency
            delay(2200)

            try {
                val result = LocalSolarCalculator.calculate(
                    panelCount = panelCount,
                    locationName = locationName,
                    roofType = roofType,
                    monthlyBillInr = monthlyBillInr,
                    shadowLossPercent = shadowLossPercent
                )

                val report = SolarReport(
                    id = "scan_${System.currentTimeMillis()}",
                    locationName = locationName,
                    panelCount = panelCount,
                    roofType = roofType,
                    monthlyBillInr = monthlyBillInr,
                    capacityKw = result.capacityKw,
                    monthlyGenerationUnits = result.monthlyGenerationUnits,
                    annualGenerationUnits = result.annualGenerationUnits,
                    monthlyGenerationBreakdown = result.monthlyBreakdown,
                    installationCostInr = result.installationCostInr,
                    subsidyInr = result.subsidyInr,
                    netCostInr = result.netCostInr,
                    annualSavingsInr = result.annualSavingsInr,
                    paybackYears = result.paybackYears,
                    savings25yrInr = result.savings25yrInr,
                    co2KgAnnual = result.co2KgAnnual,
                    treesEquivalent = result.treesEquivalent,
                    shadowLossPercent = result.shadowLossPercent,
                    usageCoveragePercent = result.usageCoveragePercent,
                    irradianceKwhM2Day = result.irradianceKwhM2Day,
                    aiNarrative = result.aiNarrative,
                    subsidyScheme = result.subsidyScheme
                )

                _uiState.value = AnalyzeUiState.Success(report)
            } catch (e: Exception) {
                _uiState.value = AnalyzeUiState.Error(
                    "Analysis failed: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun reset() {
        _uiState.value = AnalyzeUiState.Idle
    }
}
