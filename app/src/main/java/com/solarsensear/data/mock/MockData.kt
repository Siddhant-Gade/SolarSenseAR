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
            panelWatt = 550,
            capacityKw = 6.6,
            monthlyGenerationUnits = 720,
            annualGenerationUnits = 8640,
            monthlyGenerationBreakdown = listOf(540, 610, 750, 850, 900, 820, 710, 690, 730, 800, 660, 580),
            installationCostInr = 396000,
            subsidyInr = 78000,
            netCostInr = 318000,
            annualSavingsInr = 69120,
            paybackYears = 4.6,
            savings25yrInr = 1410000,
            co2KgAnnual = 705,
            treesEquivalent = 32,
            shadowLossPercent = 5.2,
            usageCoveragePercent = 88,
            irradianceKwhM2Day = 5.8,
            aiNarrative = mockNarrative,
            createdAt = System.currentTimeMillis() - 86400000
        ),
        SolarReport(
            id = "rpt_002",
            locationName = "Pune",
            state = "Maharashtra",
            panelCount = 8,
            panelWatt = 550,
            capacityKw = 4.4,
            monthlyGenerationUnits = 462,
            annualGenerationUnits = 5544,
            monthlyGenerationBreakdown = listOf(350, 400, 490, 550, 570, 520, 460, 445, 470, 510, 420, 360),
            installationCostInr = 264000,
            subsidyInr = 78000,
            netCostInr = 186000,
            annualSavingsInr = 44352,
            paybackYears = 4.2,
            savings25yrInr = 924800,
            co2KgAnnual = 452,
            treesEquivalent = 21,
            shadowLossPercent = 3.5,
            usageCoveragePercent = 72,
            irradianceKwhM2Day = 5.55,
            aiNarrative = mockNarrative,  // Fix 13: was missing — AI card invisible for this report
            createdAt = System.currentTimeMillis() - 172800000
        ),
        SolarReport(
            id = "rpt_003",
            locationName = "Jaipur",
            state = "Rajasthan",
            panelCount = 16,
            panelWatt = 550,
            capacityKw = 8.8,
            monthlyGenerationUnits = 1056,
            annualGenerationUnits = 12672,
            monthlyGenerationBreakdown = listOf(790, 915, 1090, 1220, 1280, 1185, 1035, 1000, 1060, 1160, 950, 840),
            installationCostInr = 528000,
            subsidyInr = 78000,
            netCostInr = 450000,
            annualSavingsInr = 101376,
            paybackYears = 4.4,
            savings25yrInr = 2084400,
            co2KgAnnual = 1034,
            treesEquivalent = 47,
            shadowLossPercent = 6.8,
            usageCoveragePercent = 95,
            irradianceKwhM2Day = 6.0,
            createdAt = System.currentTimeMillis() - 604800000
        )
    )

    val sampleVendors = listOf(
        Vendor(
            id = "v_001",
            name = "SunPower Solutions",
            city = "Nagpur",
            rating = 4.7f,
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
            rating = 4.5f,
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
            rating = 4.8f,
            reviews = 203,
            pricePerKwInr = 62000,
            phone = "+91-9876543212",
            latitude = 21.16,
            longitude = 79.11
        ),
        Vendor(
            id = "v_004",
            name = "Tata Power Solar",
            city = "Mumbai",
            rating = 4.9f,
            reviews = 512,
            pricePerKwInr = 65000,
            phone = "+91-9876543213",
            latitude = 19.07,
            longitude = 72.87
        ),
        Vendor(
            id = "v_005",
            name = "Adani Solar",
            city = "Ahmedabad",
            rating = 4.6f,
            reviews = 389,
            pricePerKwInr = 57000,
            phone = "+91-9876543214",
            latitude = 23.03,
            longitude = 72.58
        )
    )

    val solarTips = listOf(
        "South-facing panels generate up to 20% more energy in India.",
        "Cleaning solar panels monthly increases output by 5–10%.",
        "PM Surya Ghar offers up to ₹78,000 subsidy — apply at pmsuryaghar.gov.in.",
        "A 3 kW system can power most 2BHK homes in India.",
        "Solar panels last 25+ years with minimal maintenance.",
        "Morning dew naturally cleans panels — tilt helps drainage.",
        "India receives 300+ sunny days a year — ideal for solar.",
        "Net metering lets you sell surplus power back to the grid.",
        "A 6.6 kW system offsets over 700 kg of CO₂ annually.",
        "Jaipur, Jodhpur & Nagpur have India's best solar irradiance."
    )
}
