package com.solarsensear.ar

import android.content.Context
import android.util.Log

/**
 * Manages the ARCore session lifecycle and plane detection.
 *
 * Phase 2: Stub implementation with API contract.
 * Phase 4: Will integrate ARSceneView, configure plane detection,
 * and manage the AR session lifecycle (resume/pause/destroy).
 *
 * Usage:
 * ```
 * val manager = ARSceneManager(context)
 * manager.initialize()
 * manager.setOnPlaneDetectedListener { anchor ->
 *     panelRenderer.placePanelAt(anchor)
 * }
 * ```
 */
class ARSceneManager(private val context: Context) {

    companion object {
        private const val TAG = "ARSceneManager"
    }

    /** The current state of AR tracking. */
    enum class TrackingState {
        NOT_INITIALIZED,
        INITIALIZING,
        TRACKING,
        PAUSED,
        STOPPED,
        ERROR
    }

    var trackingState: TrackingState = TrackingState.NOT_INITIALIZED
        private set

    private var onPlaneDetectedCallback: ((Any) -> Unit)? = null

    /**
     * Initialize the AR session.
     * Phase 4 will:
     * - Create ARSceneView
     * - Enable plane detection
     * - Configure depth API if available
     */
    fun initialize() {
        Log.d(TAG, "Initializing AR session...")
        trackingState = TrackingState.INITIALIZING
        // Phase 4: arSceneView.apply {
        //     planeRenderer.isEnabled = true
        //     planeRenderer.isVisible = true
        //     configureSession { session, config ->
        //         config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
        //         config.depthMode = Config.DepthMode.AUTOMATIC
        //     }
        // }
        trackingState = TrackingState.TRACKING
        Log.d(TAG, "AR session initialized (stub)")
    }

    /**
     * Register a callback for when ARCore detects a horizontal plane.
     * The callback receives an Anchor at the plane hit point.
     */
    fun setOnPlaneDetectedListener(callback: (Any) -> Unit) {
        onPlaneDetectedCallback = callback
    }

    /** Pause the AR session (call in Activity onPause). */
    fun pause() {
        trackingState = TrackingState.PAUSED
        Log.d(TAG, "AR session paused")
    }

    /** Resume the AR session (call in Activity onResume). */
    fun resume() {
        if (trackingState == TrackingState.PAUSED) {
            trackingState = TrackingState.TRACKING
            Log.d(TAG, "AR session resumed")
        }
    }

    /** Destroy the AR session (call in Activity onDestroy). */
    fun destroy() {
        trackingState = TrackingState.STOPPED
        onPlaneDetectedCallback = null
        Log.d(TAG, "AR session destroyed")
    }

    /**
     * Checks if ARCore is available and up-to-date on this device.
     * Returns true if AR features can be used.
     */
    fun isARCoreAvailable(): Boolean {
        // Phase 4: ArCoreApk.getInstance().checkAvailability(context)
        return true // Stub: assume available
    }
}
