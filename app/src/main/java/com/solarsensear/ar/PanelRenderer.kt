package com.solarsensear.ar

import android.util.Log
import com.solarsensear.domain.PanelOptimizer

/**
 * Handles loading and placing 3D solar panel models in the AR scene.
 *
 * Phase 2: Stub with API contract.
 * Phase 4: Will use SceneView's ModelNode + AnchorNode to render
 * solar_panel.glb models in the real-world AR view.
 *
 * Usage:
 * ```
 * val renderer = PanelRenderer()
 * renderer.placePanelGrid(anchor, panelCount = 12, roofType = "flat")
 * renderer.setPanelCount(16) // Dynamically add panels
 * ```
 */
class PanelRenderer {

    companion object {
        private const val TAG = "PanelRenderer"
        private const val MODEL_ASSET = "models/solar_panel.glb"
        private const val PANEL_SCALE = 1.0f  // Real-world 1:1 scale
    }

    private var currentGrid: PanelOptimizer.PanelGrid? = null
    private var currentPanelCount: Int = 0
    private var currentRoofType: String = "flat"

    /**
     * Places a grid of solar panels at the given anchor point.
     * @param anchor The AR anchor where panels will be placed
     * @param panelCount Number of panels to place
     * @param roofType "flat", "sloped_15", "sloped_30"
     */
    fun placePanelGrid(anchor: Any, panelCount: Int, roofType: String = "flat") {
        currentPanelCount = panelCount
        currentRoofType = roofType

        val grid = PanelOptimizer.calculateGrid(panelCount, roofType)
        currentGrid = grid

        Log.d(TAG, "Placing ${grid.totalPanels} panels in ${grid.rows}x${grid.columns} grid")
        Log.d(TAG, "Grid size: ${grid.gridWidthM}m x ${grid.gridHeightM}m")

        // Phase 4: For each position in grid.panelPositions:
        // val modelNode = ModelNode(
        //     modelInstance = arSceneView.modelLoader.createModelInstance(MODEL_ASSET),
        //     scaleToUnits = PANEL_SCALE,
        //     centerOrigin = Position(y = -0.5f)
        // ).apply {
        //     position = Position(pos.offsetX, 0f, pos.offsetZ)
        //     rotation = Rotation(PanelOptimizer.getTiltAngle(roofType), 0f, 0f)
        // }
        // anchorNode.addChildNode(modelNode)
    }

    /**
     * Places a single panel at an anchor (for tap-to-place interaction).
     */
    fun placeSinglePanel(anchor: Any) {
        Log.d(TAG, "Placing single panel at anchor")
        // Phase 4: Create ModelNode + AnchorNode, add to scene
    }

    /**
     * Updates the panel count dynamically (for +/- buttons in AR overlay).
     * Recalculates and replaces the grid.
     */
    fun setPanelCount(count: Int) {
        currentPanelCount = count
        val grid = PanelOptimizer.calculateGrid(count, currentRoofType)
        currentGrid = grid
        Log.d(TAG, "Updated to $count panels: ${grid.rows}x${grid.columns} grid")
        // Phase 4: Remove old nodes, place new grid
    }

    /** Returns the current panel grid layout for display in UI. */
    fun getCurrentGrid(): PanelOptimizer.PanelGrid? = currentGrid

    /** Removes all placed panels from the AR scene. */
    fun clearAll() {
        currentGrid = null
        currentPanelCount = 0
        Log.d(TAG, "Cleared all panels")
        // Phase 4: Remove all child nodes from anchor
    }
}
