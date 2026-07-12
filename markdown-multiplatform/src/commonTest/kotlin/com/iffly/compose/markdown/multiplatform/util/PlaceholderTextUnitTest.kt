package com.iffly.compose.markdown.multiplatform.util

import androidx.compose.ui.unit.Density
import kotlin.test.Test
import kotlin.test.assertEquals

class PlaceholderTextUnitTest {
    @Test
    fun convertsMeasuredPixelsWithLinearDensityAndFontScale() {
        val density = Density(density = 2f, fontScale = 2f)

        val width = density.toPlaceholderSp(px = 40)
        val height = density.toPlaceholderSp(px = 20)

        assertEquals(10f, width.value)
        assertEquals(5f, height.value)
        assertEquals(2f, width.value / height.value)
    }
}
