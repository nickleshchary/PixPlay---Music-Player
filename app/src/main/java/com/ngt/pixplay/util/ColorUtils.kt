package com.ngt.pixplay.util

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

/**
 * Utility for generating deterministic colors
 */
object ColorUtils {

    /**
     * Generates a deterministic color based on a seed ID.
     * Uses HSL to ensure pleasing pastel/vibrant colors.
     */
    /**
     * returns a deterministic color pair (Container, OnContainer) from the Material theme
     * based on the seed ID. Cycles through Primary, Secondary, and Tertiary containers.
     */
    fun getThemeColorForId(id: Long, scheme: androidx.compose.material3.ColorScheme): Pair<Color, Color> {
        val safeSeed = id.hashCode().toLong()
        // Cycle through 0 (Primary), 1 (Secondary), 2 (Tertiary)
        val index = ((safeSeed % 3) + 3) % 3
        
        return when (index) {
            0L -> scheme.primaryContainer to scheme.onPrimaryContainer
            1L -> scheme.secondaryContainer to scheme.onSecondaryContainer
            else -> scheme.tertiaryContainer to scheme.onTertiaryContainer
        }
    }
}
