package com.solarsensear.data.models

data class Vendor(
    val id: String = "",
    val name: String = "",
    val city: String = "",
    val state: String = "",
    val rating: Float = 0f,
    val reviews: Int = 0,
    val pricePerKwInr: Int = 0,
    val phone: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val verified: Boolean = true,
    val yearsInBusiness: Int = 5
)
