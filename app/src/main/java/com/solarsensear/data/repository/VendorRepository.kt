package com.solarsensear.data.repository

import com.solarsensear.data.mock.MockData
import com.solarsensear.data.models.Vendor
import com.solarsensear.network.RetrofitClient
import com.solarsensear.network.VendorNearbyRequest
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class VendorRepository {

    private val api = RetrofitClient.apiService

    /**
     * Fetches vendors near a location.
     * Falls back to local mock data filtered by distance if the API is unavailable.
     */
    suspend fun getVendorsNearby(
        latitude: Double,
        longitude: Double,
        radiusKm: Int = 25
    ): Result<List<Vendor>> {
        return try {
            val response = api.getVendorsNearby(
                VendorNearbyRequest(latitude, longitude, radiusKm)
            )
            val vendors = response.map { apiVendor ->
                Vendor(
                    id = "v_${apiVendor.name.hashCode()}",
                    name = apiVendor.name,
                    city = apiVendor.city,
                    rating = apiVendor.rating,
                    reviews = apiVendor.reviews,
                    pricePerKwInr = apiVendor.price_per_kw_inr,
                    phone = apiVendor.phone,
                    latitude = apiVendor.latitude,
                    longitude = apiVendor.longitude
                )
            }
            Result.success(vendors)
        } catch (e: Exception) {
            // Offline fallback: filter mock vendors by distance
            val nearby = MockData.sampleVendors.filter { vendor ->
                haversineKm(latitude, longitude, vendor.latitude, vendor.longitude) <= radiusKm
            }.sortedBy { vendor ->
                haversineKm(latitude, longitude, vendor.latitude, vendor.longitude)
            }.take(3)
            Result.success(nearby)
        }
    }

    /** Haversine formula — distance in km between two GPS points. */
    private fun haversineKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val r = 6371.0 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }
}
