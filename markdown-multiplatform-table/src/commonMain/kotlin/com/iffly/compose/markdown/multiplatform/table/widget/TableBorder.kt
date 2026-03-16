package com.iffly.compose.markdown.multiplatform.table.widget

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList

/**
 * Defines which borders of the table are rendered.
 */
enum class TableBorderMode {
    /** No borders are drawn. */
    NONE,
    /** Only horizontal borders between rows are drawn. */
    HORIZONTAL,
    /** Only vertical borders between columns are drawn. */
    VERTICAL,
    /** Both horizontal and vertical borders are drawn. */
    ALL,
}

/**
 * Configuration for table border rendering, specifying the border mode, brush, and width.
 *
 * @param mode Which borders to draw (none, horizontal, vertical, or all).
 * @param brush The [Brush] used to paint the border lines.
 * @param width The thickness of the border lines.
 */
data class TableBorder(
    val mode: TableBorderMode,
    val brush: Brush,
    val width: Dp,
) {
    companion object {
        /**
         * Creates a [TableBorder] with a solid color.
         *
         * @param mode Which borders to draw.
         * @param color The border color.
         * @param width The border line thickness.
         */
        fun solid(
            mode: TableBorderMode = TableBorderMode.NONE,
            color: Color = Color.Gray,
            width: Dp = 1.dp,
        ) = TableBorder(mode, SolidColor(color), width)

        /**
         * Creates a [TableBorder] with a custom [Brush].
         *
         * @param mode Which borders to draw.
         * @param brush The brush used to paint the borders.
         * @param width The border line thickness.
         */
        fun brush(
            mode: TableBorderMode = TableBorderMode.NONE,
            brush: Brush,
            width: Dp = 1.dp,
        ) = TableBorder(mode, brush, width)
    }
}

@Composable
internal fun TableBorderCanvas(
    border: TableBorder,
    columnWidths: ImmutableList<Int>,
    rowHeights: ImmutableList<Int>,
    density: Density,
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        when (border.mode) {
            TableBorderMode.NONE -> {}

            TableBorderMode.HORIZONTAL -> {
                drawHorizontalBorders(border, rowHeights, density, size.width)
            }

            TableBorderMode.VERTICAL -> {
                drawVerticalBorders(border, columnWidths, density, size.height)
            }

            TableBorderMode.ALL -> {
                drawAllBorders(border, columnWidths, rowHeights, density, size.width, size.height)
            }
        }
    }
}

private fun DrawScope.drawHorizontalBorders(
    border: TableBorder,
    rowHeights: List<Int>,
    density: Density,
    totalWidth: Float,
) {
    val strokeWidth = with(density) { border.width.toPx() }
    var yOffset = 0f
    for (i in 0 until rowHeights.size - 1) {
        yOffset += rowHeights[i] + strokeWidth / 2
        drawLine(
            brush = border.brush,
            start = Offset(0f, yOffset),
            end = Offset(totalWidth, yOffset),
            strokeWidth = strokeWidth,
        )
    }
}

private fun DrawScope.drawVerticalBorders(
    border: TableBorder,
    columnWidths: List<Int>,
    density: Density,
    totalHeight: Float,
) {
    val strokeWidth = with(density) { border.width.toPx() }
    var xOffset = 0f
    for (i in 0 until columnWidths.size - 1) {
        xOffset += columnWidths[i] + strokeWidth / 2
        drawLine(
            brush = border.brush,
            start = Offset(xOffset, 0f),
            end = Offset(xOffset, totalHeight),
            strokeWidth = strokeWidth,
        )
    }
}

private fun DrawScope.drawAllBorders(
    border: TableBorder,
    columnWidths: List<Int>,
    rowHeights: List<Int>,
    density: Density,
    totalWidth: Float,
    totalHeight: Float,
) {
    drawHorizontalBorders(border, rowHeights, density, totalWidth)
    drawVerticalBorders(border, columnWidths, density, totalHeight)
}

internal fun calculateBorderOffsetX(
    border: TableBorder,
    density: Density,
): Int =
    when (border.mode) {
        TableBorderMode.NONE, TableBorderMode.HORIZONTAL -> {
            0
        }

        TableBorderMode.VERTICAL, TableBorderMode.ALL -> {
            with(density) { border.width.toPx().toInt() }
        }
    }

internal fun calculateBorderOffsetY(
    border: TableBorder,
    density: Density,
): Int =
    when (border.mode) {
        TableBorderMode.NONE, TableBorderMode.VERTICAL -> {
            0
        }

        TableBorderMode.HORIZONTAL, TableBorderMode.ALL -> {
            with(density) { border.width.toPx().toInt() }
        }
    }
