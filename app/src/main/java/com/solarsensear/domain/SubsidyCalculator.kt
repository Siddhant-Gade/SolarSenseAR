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
            // Fix 15: prorate 1–2 kW slab: ₹30K base + ₹18K per kW above 1kW (max ₹60K at 2kW)
            capacityKw <= 2.0 -> (30000 + ((capacityKw - 1.0) * 30000)).toInt().coerceAtMost(60000)
            else -> 78000   // Max cap per government policy (≥2kW gets ₹78K flat)
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
