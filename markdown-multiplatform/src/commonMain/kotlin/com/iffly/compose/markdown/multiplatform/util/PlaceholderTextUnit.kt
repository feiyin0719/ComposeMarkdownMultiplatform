package com.iffly.compose.markdown.multiplatform.util

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit

/**
 * Process-wide proxy for converting measured placeholder pixels to `sp`.
 *
 * Compose's native [Density.toSp] conversion is used by default. Applications can replace
 * [delegate] with a custom conversion. Configure the delegate once during application startup,
 * before Markdown content is composed.
 */
object PlaceholderTextUnitConverter {
    private val defaultDelegate: (Density, Int) -> TextUnit = ::composePlaceholderToSp

    /** Conversion invoked by [toPlaceholderSp]. */
    var delegate: (density: Density, px: Int) -> TextUnit = defaultDelegate

    /** Converts [px] using the current [delegate]. */
    fun convert(
        density: Density,
        px: Int,
    ): TextUnit = delegate(density, px)

    /** Restores Compose's native [Density.toSp] conversion. */
    fun reset() {
        delegate = defaultDelegate
    }
}

/** Converts measured placeholder pixels through [PlaceholderTextUnitConverter]. */
fun Density.toPlaceholderSp(px: Int): TextUnit = PlaceholderTextUnitConverter.convert(this, px)

private fun composePlaceholderToSp(
    density: Density,
    px: Int,
): TextUnit = with(density) { px.toSp() }
