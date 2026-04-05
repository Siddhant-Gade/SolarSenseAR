package com.solarsensear.domain

object SubsidyCalculator {

    const val SCHEME_NAME = "PM Surya Ghar Muft Bijli Yojana"

    data class SubsidyBreakdown(
        val upTo1kw: Int = 30000,
        val oneTo2kw: Int = 60000,
        val above2kw: Int = 78000
    )

    fun calculate(capacityKw: Double): Int {
        return when {
            capacityKw <= 1.0 -> 30000
            capacityKw <= 2.0 -> 60000
            else -> 78000   // Max cap per government policy
        }
    }

    fun getBreakdown(): SubsidyBreakdown = SubsidyBreakdown()

    fun getSlabLabel(capacityKw: Double): String {
        return when {
            capacityKw <= 1.0 -> "Up to 1 kW — ₹30,000"
            capacityKw <= 2.0 -> "1 kW to 2 kW — ₹60,000"
            else -> "Above 2 kW — ₹78,000 (max)"
        }
    }
}
