package com.iffly.compose.markdown.multiplatform.util

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit

/**
 * Process-wide proxy for converting measured placeholder pixels to `sp`.
 *
 * Android Compose versions before 1.10 need the platform default's linear compatibility
 * conversion. Other targets use Compose's native [Density.toSp] conversion. Applications can
 * call [useComposeToSp], or replace [delegate] with a custom conversion. Configure the delegate
 * once during application startup, before Markdown content is composed.
 */
object PlaceholderTextUnitConverter {
    private val defaultDelegate: (Density, Int) -> TextUnit = ::platformDefaultPlaceholderToSp

    /** Conversion invoked by [toPlaceholderSp]. */
    var delegate: (density: Density, px: Int) -> TextUnit = defaultDelegate

    /** Converts [px] using the current [delegate]. */
    fun convert(
        density: Density,
        px: Int,
    ): TextUnit = delegate(density, px)

    /** Uses Compose's native [Density.toSp] conversion. Recommended on Android with Compose 1.10 or newer. */
    fun useComposeToSp() {
        delegate = ::composePlaceholderToSp
    }

    /** Restores the default conversion for the current platform. */
    fun reset() {
        delegate = defaultDelegate
    }
}

/** Converts measured placeholder pixels through [PlaceholderTextUnitConverter]. */
fun Density.toPlaceholderSp(px: Int): TextUnit = PlaceholderTextUnitConverter.convert(this, px)

internal expect fun platformDefaultPlaceholderToSp(
    density: Density,
    px: Int,
): TextUnit

private fun composePlaceholderToSp(
    density: Density,
    px: Int,
): TextUnit = with(density) { px.toSp() }
