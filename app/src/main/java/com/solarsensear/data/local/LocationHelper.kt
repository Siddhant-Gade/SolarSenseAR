package com.solarsensear.data.local

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.solarsensear.network.RetrofitClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

object LocationHelper {

    /** Returns true if both FINE and COARSE location permissions are granted. */
    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    /**
     * Fetches the device's current GPS coordinates, then reverse-geocodes them via Nominatim.
     * Returns the most specific locality name available (city > town > village > county).
     * Throws if location permission is not granted or GPS fails.
     */
    suspend fun fetchCityName(context: Context): String {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()

        val loc = suspendCancellableCoroutine { cont ->
            try {
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { location ->
                        if (location != null) cont.resume(location)
                        else cont.resumeWithException(IllegalStateException("Location is null – GPS may be off"))
                    }
                    .addOnFailureListener { e -> cont.resumeWithException(e) }

                cont.invokeOnCancellation { cts.cancel() }
            } catch (se: SecurityException) {
                cont.resumeWithException(se)
            }
        }

        val response = RetrofitClient.nominatimService.reverseGeocode(
            lat = loc.latitude,
            lon = loc.longitude,
            format = "json",
            addressDetails = 1,
            userAgent = "SolarSenseAR/1.0 (solarsensear@app.com)",
            accept = "application/json"
        )
        return response.address?.locality?.ifBlank { response.displayName.split(",").firstOrNull()?.trim() ?: "Unknown" }
            ?: response.displayName.split(",").firstOrNull()?.trim()
            ?: "Unknown"
    }
}
