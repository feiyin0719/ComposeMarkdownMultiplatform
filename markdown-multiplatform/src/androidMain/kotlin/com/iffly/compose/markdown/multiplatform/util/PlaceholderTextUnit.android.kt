package com.iffly.compose.markdown.multiplatform.util

import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

internal actual fun platformDefaultPlaceholderToSp(
    density: Density,
    px: Int,
): TextUnit = (px.toFloat() / density.density / density.fontScale).sp
