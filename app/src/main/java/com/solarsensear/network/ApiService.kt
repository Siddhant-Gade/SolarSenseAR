package com.solarsensear.network

import com.solarsensear.data.models.ReportRequest
import com.solarsensear.data.models.ReportResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

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
}

data class VendorNearbyRequest(
    val latitude: Double,
    val longitude: Double,
    val radiusKm: Int = 25
)

data class ApiVendor(
    val name: String = "",
    val city: String = "",
    val rating: Double = 0.0,
    val reviews: Int = 0,
    val price_per_kw_inr: Int = 0,
    val phone: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0
)