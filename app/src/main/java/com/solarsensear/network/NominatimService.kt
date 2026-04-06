package com.solarsensear.network

import com.solarsensear.data.models.NominatimResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * OpenStreetMap Nominatim API — free reverse geocoding.
 * Base URL: https://nominatim.openstreetmap.org/
 * Required: User-Agent + Accept headers per Nominatim ToS.
 * Rate limit: 1 request/second.
 */
interface NominatimService {

    @GET("reverse")
    suspend fun reverseGeocode(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("format") format: String,
        @Query("addressdetails") addressDetails: Int,
        @Header("User-Agent") userAgent: String,
        @Header("Accept") accept: String
    ): NominatimResponse
}
