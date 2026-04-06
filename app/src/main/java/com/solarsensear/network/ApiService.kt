package com.solarsensear.network

import com.google.gson.annotations.SerializedName
import com.solarsensear.data.models.ReportRequest
import com.solarsensear.data.models.ReportResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Retrofit interface for the SolarSense FastAPI backend.
 * Base URL is set via BuildConfig.BACKEND_URL.
 */
interface ApiService {

    /**
     * Generates a full solar report (AI narrative + financial calculations).
     * POST /generate-report
     */
    @POST("generate-report")
    suspend fun generateReport(@Body request: ReportRequest): ReportResponse

    /**
     * Health check — used to warm up the Render free-tier container.
     * GET /health
     */
    @GET("health")
    suspend fun healthCheck(): Map<String, String>

    /**
     * Fetches verified solar vendors near a given location.
     * POST /vendors-nearby
     */
    @POST("vendors-nearby")
    suspend fun getVendorsNearby(@Body request: VendorNearbyRequest): List<ApiVendor>

    /**
     * Returns the current user's scan history.
     * GET /api/v1/user/scans
     */
    @GET("api/v1/user/scans")
    suspend fun getUserScans(): List<ScanSummary>
}

data class VendorNearbyRequest(
    val latitude: Double,
    val longitude: Double,
    @SerializedName("radius_km")
    val radiusKm: Int = 50
)

data class ApiVendor(
    val id: String = "",
    val name: String = "",
    val city: String = "",
    val state: String = "",
    val rating: Float = 0f,
    val reviews: Int = 0,
    @SerializedName("price_per_kw_inr")
    val pricePerKwInr: Int = 0,
    @SerializedName("contact_phone")
    val phone: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val verified: Boolean = true,
    @SerializedName("years_in_business")
    val yearsInBusiness: Int = 0
)

data class ScanSummary(
    val id: String = "",
    @SerializedName("location_name")
    val locationName: String = "",
    @SerializedName("panel_count")
    val panelCount: Int = 0,
    @SerializedName("system_kw")
    val systemKw: Double = 0.0,
    @SerializedName("net_cost_inr")
    val netCostInr: Int = 0,
    @SerializedName("payback_years")
    val paybackYears: Double = 0.0,
    @SerializedName("created_at")
    val createdAt: String = ""
)