package com.solarsensear.ar

import android.util.Log
import com.solarsensear.domain.ShadowPathEngine

/**
 * Renders shadow overlays in the AR scene based on sun position.
 * Uses ShadowPathEngine to compute shadow offsets for a given time of day.
 *
 * Phase 2: Stub with API contract.
 * Phase 4: Will render a semi-transparent dark plane/rectangle on the AR scene,
 * offset from the panel anchor based on the sun's azimuth and elevation.
 *
 * Usage:
 * ```
 * val shadow = ShadowOverlay()
 * shadow.updateShadow(lat, lon, hourOfDay = 14)  // 2 PM shadows
 * // Connected to a time slider: slider 6..18 → updateShadow(lat, lon, sliderValue)
 * ```
 */
class ShadowOverlay {

    companion object {
        private const val TAG = "ShadowOverlay"
        private const val SHADOW_ALPHA = 0.35f
    }

    private var isVisible: Boolean = false
    private var currentHour: Int = 12
    private var currentShadowOffset: Pair<Float, Float> = 0f to 0f

    /**
     * Updates the shadow overlay position based on sun position at the given hour.
     * @param lat GPS latitude
     * @param lon GPS longitude
     * @param hourOfDay Hour in 24h format (6=6AM, 18=6PM)
     */
    fun updateShadow(lat: Double, lon: Double, hourOfDay: Int) {
        currentHour = hourOfDay
        val sunPosition = ShadowPathEngine.getSunPosition(lat, lon, hourOfDay)
        currentShadowOffset = ShadowPathEngine.getShadowOffset(sunPosition)

        Log.d(TAG, "Shadow at ${hourOfDay}h: " +
            "Sun az=${sunPosition.azimuthDeg.toInt()}° el=${sunPosition.elevationDeg.toInt()}° " +
            "offset=(${currentShadowOffset.first}, ${currentShadowOffset.second})m")

        // Phase 4: Move the shadow plane node to the computed offset
        // shadowNode.position = Position(
        //     currentShadowOffset.first,
        //     -0.01f,  // Just below ground plane
        //     currentShadowOffset.second
        // )
    }

    /**
     * Shows/hides the shadow overlay.
     * Used to toggle shadow visualization.
     */
    fun setVisible(visible: Boolean) {
        isVisible = visible
        Log.d(TAG, "Shadow visibility: $visible")
        // Phase 4: shadowNode.isVisible = visible
    }

    /** Returns the current shadow offset in meters (dx, dz). */
    fun getCurrentOffset(): Pair<Float, Float> = currentShadowOffset

    /** Returns whether shadows are currently visible. */
    fun isShowing(): Boolean = isVisible

    /** Removes the shadow overlay from the AR scene. */
    fun destroy() {
        isVisible = false
        Log.d(TAG, "Shadow overlay destroyed")
        // Phase 4: Remove shadowNode from scene
    }
}
