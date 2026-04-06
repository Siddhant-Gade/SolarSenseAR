package com.solarsensear.domain

import java.util.Calendar
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Calculates sun position (azimuth & elevation) for a given GPS location and time.
 * Uses the simplified NOAA solar position algorithm.
 * Used to render shadow overlays in AR that shift with the time-of-day slider.
 */
object ShadowPathEngine {

    data class SunPosition(
        val azimuthDeg: Double,   // Degrees from North (0=N, 90=E, 180=S, 270=W)
        val elevationDeg: Double  // Degrees above horizon (negative = below horizon)
    )

    /**
     * Computes the sun's azimuth and elevation for a given location and hour.
     * @param lat Latitude in degrees
     * @param lon Longitude in degrees
     * @param hourOfDay 0–23 (24h format, local time assumed IST UTC+5:30)
     * @param dayOfYear 1–365
     */
    fun getSunPosition(
        lat: Double,
        lon: Double,
        hourOfDay: Int,
        dayOfYear: Int = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    ): SunPosition {
        val latRad = Math.toRadians(lat)

        // Solar declination (simplified Cooper's equation)
        val declination = 23.45 * sin(Math.toRadians(360.0 / 365.0 * (dayOfYear - 81)))
        val decRad = Math.toRadians(declination)

        // Hour angle: solar noon = 0, each hour = 15°
        // Approximate solar noon at given longitude (IST offset = 5.5 hours from UTC)
        val solarNoonOffset = (lon - 82.5) / 15.0 // 82.5°E is IST reference meridian
        val solarHour = hourOfDay + solarNoonOffset - 12.0
        val hourAngleRad = Math.toRadians(solarHour * 15.0)

        // Solar elevation angle
        val sinElevation = sin(latRad) * sin(decRad) +
                cos(latRad) * cos(decRad) * cos(hourAngleRad)
        val elevationRad = asin(sinElevation.coerceIn(-1.0, 1.0))
        val elevationDeg = Math.toDegrees(elevationRad)

        // Solar azimuth angle (measured from North, clockwise)
        val cosAzimuth = (sin(decRad) - sin(latRad) * sin(elevationRad)) /
                (cos(latRad) * cos(elevationRad))
        var azimuthDeg = Math.toDegrees(acos(cosAzimuth.coerceIn(-1.0, 1.0)))

        // Correct azimuth for afternoon (hour angle > 0 → azimuth > 180°)
        if (solarHour > 0) {
            azimuthDeg = 360.0 - azimuthDeg
        }

        return SunPosition(
            azimuthDeg = azimuthDeg,
            elevationDeg = elevationDeg
        )
    }

    /**
     * Returns a list of sun positions from 6 AM to 6 PM, suitable for the shadow timeline slider.
     */
    fun getFullDayPath(lat: Double, lon: Double, dayOfYear: Int = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)): List<Pair<Int, SunPosition>> {
        return (6..18).map { hour ->
            hour to getSunPosition(lat, lon, hour, dayOfYear)
        }
    }

    /**
     * Computes shadow offset (dx, dz) from sun position.
     * Used to position the shadow rectangle relative to panel anchor in AR space.
     * Returns meters offset (approximate for panel-scale shadows).
     */
    fun getShadowOffset(sunPosition: SunPosition, panelHeightM: Double = 0.5): Pair<Float, Float> {
        if (sunPosition.elevationDeg <= 0) {
            // Sun below horizon — no meaningful shadow
            return 0f to 0f
        }

        val elevRad = Math.toRadians(sunPosition.elevationDeg)
        val azRad = Math.toRadians(sunPosition.azimuthDeg)

        // Shadow length proportional to cot(elevation) * panel height
        val shadowLength = panelHeightM / kotlin.math.tan(elevRad)

        // Shadow falls opposite to sun direction
        val dx = (-shadowLength * sin(azRad)).toFloat()
        val dz = (-shadowLength * cos(azRad)).toFloat()

        return dx to dz
    }
}
