package com.iffly.compose.markdown.multiplatform.util

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit

internal actual fun platformDefaultPlaceholderToSp(
    density: Density,
    px: Int,
): TextUnit = with(density) { px.toSp() }
