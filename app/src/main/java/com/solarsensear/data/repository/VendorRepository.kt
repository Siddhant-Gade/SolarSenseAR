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
     * Fetch vendors from API.
     * If API fails → fallback to local filtered data.
     */
    suspend fun getVendorsNearby(
        latitude: Double,
        longitude: Double,
        radiusKm: Int = 25
    ): Result<List<Vendor>> {

        return try {

            val response = api.getVendorsNearby(
                VendorNearbyRequest(
                    latitude = latitude,
                    longitude = longitude,
                    radiusKm = radiusKm
                )
            )

            val vendors = response.map { apiVendor ->
                Vendor(
                    id = "v_${apiVendor.name.hashCode()}",
                    name = apiVendor.name,
                    city = apiVendor.city,
                    rating = apiVendor.rating.toFloat(),  // Double → Float
                    reviews = apiVendor.reviews,
                    pricePerKwInr = apiVendor.price_per_kw_inr,  // Int → Int (no conversion)
                    phone = apiVendor.phone,
                    latitude = apiVendor.latitude,   // already Double
                    longitude = apiVendor.longitude  // already Double
                )
            }

            Result.success(vendors)

        } catch (e: Exception) {

            // 🔁 Fallback (Offline Mode)
            val nearby = MockData.sampleVendors
                .filter { vendor ->
                    haversineKm(
                        latitude,
                        longitude,
                        vendor.latitude,
                        vendor.longitude
                    ) <= radiusKm.toDouble()   // ✅ FIX HERE
                }
                .sortedBy { vendor ->
                    haversineKm(
                        latitude,
                        longitude,
                        vendor.latitude,
                        vendor.longitude
                    )
                }
                .take(3)

            Result.success(nearby)
        }
    }

    /**
     * Haversine Formula
     * Calculates distance (in KM) between two lat-long points
     */
    private fun haversineKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {

        val r = 6371.0

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }
}