package com.solarsensear.network

import com.solarsensear.data.models.ReportRequest
import com.solarsensear.data.models.ReportResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("/api/generate-report")
    suspend fun generateReport(@Body request: ReportRequest): ReportResponse

    @GET("/health")
    suspend fun healthCheck(): Map<String, String>

    @POST("/api/vendors/nearby")
    suspend fun getVendorsNearby(@Body request: VendorNearbyRequest): List<VendorApiResponse>
}

data class VendorNearbyRequest(
    val latitude: Double,
    val longitude: Double,
    val radius_km: Int = 15
)

data class VendorApiResponse(
    val name: String,
    val city: String,
    val rating: Double,
    val reviews: Int,
    val price_per_kw_inr: Int,
    val phone: String,
    val latitude: Double,
    val longitude: Double
)
