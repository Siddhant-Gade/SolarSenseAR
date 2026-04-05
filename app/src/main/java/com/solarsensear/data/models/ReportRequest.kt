package com.solarsensear.data.models

import com.google.gson.annotations.SerializedName

data class ReportRequest(
    val latitude: Double,
    val longitude: Double,
    @SerializedName("panel_count")
    val panelCount: Int,
    @SerializedName("panel_watt")
    val panelWatt: Int = 400,
    @SerializedName("roof_type")
    val roofType: String = "flat",
    @SerializedName("monthly_bill_inr")
    val monthlyBillInr: Double = 2000.0,
    val state: String = "Maharashtra"
)
