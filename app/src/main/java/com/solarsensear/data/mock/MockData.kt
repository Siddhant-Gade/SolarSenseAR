package com.solarsensear.data.mock

import com.solarsensear.data.models.SolarReport
import com.solarsensear.data.models.UserProfile
import com.solarsensear.data.models.Vendor

object MockData {

    val currentUser = UserProfile(
        uid = "mock_user_001",
        name = "Siddhant",
        email = "siddhant@example.com",
        isGuest = false
    )

    val mockNarrative = "Based on Nagpur's excellent solar irradiance of 5.8 kWh/m²/day, " +
        "your 4.8 kW system with 12 panels will generate approximately 576 units of " +
        "electricity every month. This covers nearly your entire ₹3,000 monthly bill. " +
        "After the PM Surya Ghar subsidy of ₹78,000, your net investment is just ₹2,10,000 — " +
        "which pays for itself in under 4 years. Over 25 years, you stand to save over ₹16 lakh. " +
        "We strongly recommend applying for the PM Surya Ghar Muft Bijli Yojana at " +
        "pmsuryaghar.gov.in to claim your subsidy."

    val sampleReports = listOf(
        SolarReport(
            id = "rpt_001",
            locationName = "Nagpur",
            state = "Maharashtra",
            panelCount = 12,
            panelWatt = 400,
            capacityKw = 4.8,
            monthlyGenerationUnits = 576,
            annualGenerationUnits = 6912,
            installationCostInr = 288000,
            subsidyInr = 78000,
            netCostInr = 210000,
            paybackYears = 3.9,
            savings25yrInr = 1620000,
            co2KgAnnual = 5529,
            treesEquivalent = 47,
            irradianceKwhM2Day = 5.8,
            aiNarrative = mockNarrative,
            createdAt = System.currentTimeMillis() - 86400000
        ),
        SolarReport(
            id = "rpt_002",
            locationName = "Pune",
            state = "Maharashtra",
            panelCount = 8,
            panelWatt = 400,
            capacityKw = 3.2,
            monthlyGenerationUnits = 360,
            annualGenerationUnits = 4320,
            installationCostInr = 192000,
            subsidyInr = 78000,
            netCostInr = 114000,
            paybackYears = 3.3,
            savings25yrInr = 746000,
            co2KgAnnual = 3542,
            treesEquivalent = 42,
            irradianceKwhM2Day = 5.5,
            createdAt = System.currentTimeMillis() - 172800000
        ),
        SolarReport(
            id = "rpt_003",
            locationName = "Jaipur",
            state = "Rajasthan",
            panelCount = 16,
            panelWatt = 540,
            capacityKw = 8.64,
            monthlyGenerationUnits = 1166,
            annualGenerationUnits = 13996,
            installationCostInr = 518400,
            subsidyInr = 78000,
            netCostInr = 440400,
            paybackYears = 4.7,
            savings25yrInr = 1896000,
            co2KgAnnual = 11477,
            treesEquivalent = 137,
            irradianceKwhM2Day = 6.0,
            createdAt = System.currentTimeMillis() - 604800000
        )
    )

    val sampleVendors = listOf(
        Vendor(
            id = "v_001",
            name = "SunPower Solutions",
            city = "Nagpur",
            rating = 4.7,
            reviews = 142,
            pricePerKwInr = 58000,
            phone = "+91-9876543210",
            latitude = 21.15,
            longitude = 79.09
        ),
        Vendor(
            id = "v_002",
            name = "GreenRay Energy",
            city = "Nagpur",
            rating = 4.5,
            reviews = 89,
            pricePerKwInr = 55000,
            phone = "+91-9876543211",
            latitude = 21.13,
            longitude = 79.07
        ),
        Vendor(
            id = "v_003",
            name = "SolarFirst India",
            city = "Nagpur",
            rating = 4.8,
            reviews = 203,
            pricePerKwInr = 62000,
            phone = "+91-9876543212",
            latitude = 21.16,
            longitude = 79.11
        )
    )

    val solarTips = listOf(
        "South-facing panels generate up to 20% more energy in India.",
        "Cleaning solar panels monthly increases output by 5–10%.",
        "PM Surya Ghar offers up to ₹78,000 subsidy — apply at pmsuryaghar.gov.in.",
        "A 3 kW system can power most 2BHK homes in India.",
        "Solar panels last 25+ years with minimal maintenance.",
        "Morning dew naturally cleans panels — tilt helps drainage.",
        "India receives 300+ sunny days a year — ideal for solar."
    )
}
