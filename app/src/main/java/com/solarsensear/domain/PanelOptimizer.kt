package com.solarsensear.domain

/**
 * Handles optimal panel grid layout on a detected roof plane.
 * Given panel dimensions and a detection area, calculates the grid arrangement
 * (rows × columns) that fits within the available space.
 */
object PanelOptimizer {

    // Standard solar panel dimensions in meters
    private const val PANEL_WIDTH_M = 1.0f
    private const val PANEL_HEIGHT_M = 1.7f
    private const val GAP_M = 0.02f // 2cm gap between panels

    data class PanelGrid(
        val rows: Int,
        val columns: Int,
        val totalPanels: Int,
        val gridWidthM: Float,
        val gridHeightM: Float,
        val panelPositions: List<PanelPosition>
    )

    data class PanelPosition(
        val row: Int,
        val col: Int,
        val offsetX: Float,  // Meters from grid origin
        val offsetZ: Float   // Meters from grid origin
    )

    /**
     * Calculates an optimal panel grid layout.
     * @param panelCount Desired number of panels
     * @param roofType "flat", "sloped_15", "sloped_30"
     * @param maxWidthM Available roof width in meters (from plane detection)
     * @param maxHeightM Available roof height in meters
     */
    fun calculateGrid(
        panelCount: Int,
        roofType: String = "flat",
        maxWidthM: Float = 20f,
        maxHeightM: Float = 20f
    ): PanelGrid {
        // Calculate grid dimensions to be as close to square as possible
        val cols = calculateOptimalColumns(panelCount, maxWidthM)
        val rows = (panelCount + cols - 1) / cols // Ceiling division

        val positions = mutableListOf<PanelPosition>()
        var placed = 0

        val tiltCompensation = when (roofType) {
            "sloped_15" -> 0.97f  // Slight compression due to tilt
            "sloped_30" -> 0.87f  // More compression
            else -> 1.0f
        }

        val effectivePanelHeight = PANEL_HEIGHT_M * tiltCompensation

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (placed >= panelCount) break

                val offsetX = c * (PANEL_WIDTH_M + GAP_M)
                val offsetZ = r * (effectivePanelHeight + GAP_M)

                // Check bounds
                if (offsetX + PANEL_WIDTH_M <= maxWidthM &&
                    offsetZ + effectivePanelHeight <= maxHeightM
                ) {
                    positions.add(
                        PanelPosition(
                            row = r,
                            col = c,
                            offsetX = offsetX,
                            offsetZ = offsetZ
                        )
                    )
                    placed++
                }
            }
        }

        val actualRows = positions.maxOfOrNull { it.row }?.plus(1) ?: 0
        val actualCols = positions.maxOfOrNull { it.col }?.plus(1) ?: 0

        return PanelGrid(
            rows = actualRows,
            columns = actualCols,
            totalPanels = positions.size,
            gridWidthM = actualCols * (PANEL_WIDTH_M + GAP_M) - GAP_M,
            gridHeightM = actualRows * (effectivePanelHeight + GAP_M) - GAP_M,
            panelPositions = positions
        )
    }

    /** Determines columns to keep the grid close to square given available width. */
    private fun calculateOptimalColumns(panelCount: Int, maxWidthM: Float): Int {
        val maxCols = (maxWidthM / (PANEL_WIDTH_M + GAP_M)).toInt().coerceAtLeast(1)
        val idealCols = kotlin.math.sqrt(panelCount.toDouble()).toInt().coerceAtLeast(1)
        return idealCols.coerceAtMost(maxCols)
    }

    /**
     * Returns the tilt angle in degrees that should be applied to panel Y rotation
     * based on roof type.
     */
    fun getTiltAngle(roofType: String): Float {
        return when (roofType) {
            "sloped_15" -> 15f
            "sloped_30" -> 30f
            else -> 0f // Flat
        }
    }
}
