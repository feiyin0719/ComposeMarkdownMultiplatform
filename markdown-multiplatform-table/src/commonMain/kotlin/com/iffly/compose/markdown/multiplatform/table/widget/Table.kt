package com.iffly.compose.markdown.multiplatform.table.widget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.layout.SubcomposeMeasureScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.fastForEachIndexed
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlin.math.max
import kotlin.math.min

/**
 * Composable that lays out a table using [SubcomposeLayout], supporting configurable cell padding,
 * alignment, borders, shape clipping, and optional weight-based column widths.
 *
 * @param modifier Modifier applied to the table layout.
 * @param cellPadding Padding applied inside each cell.
 * @param cellAlignment Default alignment for cell content.
 * @param border Border configuration controlling which borders are drawn and their appearance.
 * @param shape Optional shape to clip the table container.
 * @param widthWeights Optional list of relative weights for column widths; when provided,
 *   columns are sized proportionally to the available width.
 * @param content DSL content block defining the table structure via [TableScope].
 * @see TableScope
 */
@Composable
fun Table(
    modifier: Modifier = Modifier,
    cellPadding: PaddingValues = PaddingValues(),
    cellAlignment: Alignment = Alignment.CenterStart,
    border: TableBorder = TableBorder.solid(mode = TableBorderMode.NONE),
    shape: Shape? = null,
    widthWeights: ImmutableList<Float>? = null,
    content: TableScope.() -> Unit,
) {
    val tableBuilder = TableScopeImpl(cellAlignment).apply(content)
    val density = LocalDensity.current
    val appliedModifier = if (shape != null) modifier.clip(shape) else modifier
    SubcomposeLayout(modifier = appliedModifier) { constraints ->
        measureAndLayoutTable(
            this,
            tableBuilder,
            cellPadding,
            density,
            constraints,
            border,
            widthWeights,
        )
    }
}

private fun measureAndLayoutTable(
    measureScope: SubcomposeMeasureScope,
    tableBuilder: TableScopeImpl,
    cellPadding: PaddingValues,
    density: Density,
    constraints: Constraints,
    border: TableBorder,
    widthWeights: List<Float>? = null,
) = with(measureScope) {
    val measureResult =
        measureTable(
            measureScope = this,
            tableBuilder = tableBuilder,
            cellPadding = cellPadding,
            density = density,
            constraints = constraints,
            border = border,
            widthWeights = widthWeights,
        )

    layoutTable(
        measureScope = this,
        measureResult = measureResult,
        border = border,
        density = density,
    )
}

private data class TableMeasureResult(
    val allRows: List<RowScopeImpl>,
    val columnCount: Int,
    val columnWidths: List<Int>,
    val rowHeights: List<Int>,
    val cellPlaceableMap: List<List<Placeable?>>,
    val cellData: List<List<CellImpl?>>,
    val totalWidth: Int,
    val totalHeight: Int,
    val borderWidth: Int = 0,
    val borderHeight: Int = 0,
)

private fun measureTable(
    measureScope: SubcomposeMeasureScope,
    tableBuilder: TableScopeImpl,
    cellPadding: PaddingValues,
    density: Density,
    constraints: Constraints,
    border: TableBorder,
    widthWeights: List<Float>?,
): TableMeasureResult? {
    val allRows = tableBuilder.rows()
    val columnCount = allRows.maxOfOrNull { it.cells.size } ?: 0

    if (columnCount == 0 || allRows.isEmpty()) {
        return null
    }

    val columnWeightWidths =
        getColumnWeightWidths(widthWeights, columnCount, constraints, border, density)

    val columnWidths = MutableList(columnCount) { 0 }
    val rowHeights = MutableList(allRows.size) { 0 }
    val cellPlaceableMap = List(allRows.size) { MutableList<Placeable?>(columnCount) { null } }
    val cellData = List(allRows.size) { MutableList<CellImpl?>(columnCount) { null } }
    val borderWith = calculateBorderOffsetX(border, density)
    val borderHeight = calculateBorderOffsetY(border, density)

    measureAllCells(
        measureScope = measureScope,
        allRows = allRows,
        columnCount = columnCount,
        columnWeightWidths = columnWeightWidths,
        cellPadding = cellPadding,
        constraints = constraints,
        columnWidths = columnWidths,
        rowHeights = rowHeights,
        cellPlaceableMap = cellPlaceableMap,
        cellData = cellData,
    )

    val totalWidth = columnWidths.sum() + borderWith * (columnCount - 1)
    val totalHeight = rowHeights.sum() + borderHeight * (allRows.size - 1)
    val (adjustedTotalWidth, adjustedTotalHeight) =
        adjustedSize(constraints, totalWidth, totalHeight)

    return TableMeasureResult(
        allRows = allRows,
        columnCount = columnCount,
        columnWidths = columnWidths,
        rowHeights = rowHeights,
        cellPlaceableMap = cellPlaceableMap,
        cellData = cellData,
        totalWidth = adjustedTotalWidth,
        totalHeight = adjustedTotalHeight,
        borderWidth = borderWith,
        borderHeight = borderHeight,
    )
}

private fun adjustedSize(
    constraints: Constraints,
    totalWidth: Int,
    totalHeight: Int,
): Pair<Int, Int> {
    val adjustedTotalWidth =
        if (constraints.hasBoundedWidth) {
            min(constraints.maxWidth, totalWidth)
        } else {
            totalWidth
        }
    val adjustedTotalHeight =
        if (constraints.hasBoundedHeight) {
            min(constraints.maxHeight, totalHeight)
        } else {
            totalHeight
        }
    return Pair(adjustedTotalWidth, adjustedTotalHeight)
}

private fun getColumnWeightWidths(
    widthWeights: List<Float>?,
    columnCount: Int,
    constraints: Constraints,
    border: TableBorder,
    density: Density,
): List<Int> =
    if (!widthWeights.isNullOrEmpty()) {
        calculateWeightBasedColumnWidths(widthWeights, columnCount, constraints, border, density)
    } else {
        List(columnCount) { -1 }
    }

private fun measureAllCells(
    measureScope: SubcomposeMeasureScope,
    allRows: List<RowScopeImpl>,
    columnCount: Int,
    columnWeightWidths: List<Int>,
    cellPadding: PaddingValues,
    constraints: Constraints,
    columnWidths: MutableList<Int>,
    rowHeights: MutableList<Int>,
    cellPlaceableMap: List<MutableList<Placeable?>>,
    cellData: List<MutableList<CellImpl?>>,
) = allRows.fastForEachIndexed { rowIndex, row ->
    row.cells.fastForEachIndexed { columnIndex, cell ->
        if (columnIndex < columnCount) {
            measureSingleCell(
                measureScope = measureScope,
                rowIndex = rowIndex,
                columnIndex = columnIndex,
                cell = cell,
                columnWeightWidths = columnWeightWidths,
                cellPadding = cellPadding,
                constraints = constraints,
                columnWidths = columnWidths,
                rowHeights = rowHeights,
                cellPlaceableMap = cellPlaceableMap,
                cellData = cellData,
            )
        }
    }
}

private fun measureSingleCell(
    measureScope: SubcomposeMeasureScope,
    rowIndex: Int,
    columnIndex: Int,
    cell: CellImpl,
    columnWeightWidths: List<Int>,
    cellPadding: PaddingValues,
    constraints: Constraints,
    columnWidths: MutableList<Int>,
    rowHeights: MutableList<Int>,
    cellPlaceableMap: List<MutableList<Placeable?>>,
    cellData: List<MutableList<CellImpl?>>,
) = with(measureScope) {
    val cellConstraints =
        if (columnWeightWidths[columnIndex] != -1) {
            Constraints.fixedWidth(columnWeightWidths[columnIndex])
        } else {
            constraints
        }

    val placeable =
        subcompose("cell_${rowIndex}_$columnIndex") {
            CellBox(cellPadding, cell.modifier, contentAlignment = cell.alignment, cell.content)
        }[0].measure(cellConstraints)

    cellPlaceableMap[rowIndex][columnIndex] = placeable
    cellData[rowIndex][columnIndex] = cell
    rowHeights[rowIndex] = max(rowHeights[rowIndex], placeable.height)
    columnWidths[columnIndex] = max(columnWidths[columnIndex], placeable.width)
}

private fun layoutTable(
    measureScope: SubcomposeMeasureScope,
    measureResult: TableMeasureResult?,
    border: TableBorder,
    density: Density,
) = with(measureScope) {
    if (measureResult == null) {
        return@with layout(0, 0) {}
    }

    val borderWith = measureResult.borderWidth
    val borderHeight = measureResult.borderHeight
    layout(measureResult.totalWidth, measureResult.totalHeight) {
        var yOffset = 0

        measureResult.allRows.fastForEachIndexed { rowIndex, row ->
            if (yOffset >= measureResult.totalHeight) {
                return@fastForEachIndexed
            }
            val rowHeight = measureResult.rowHeights[rowIndex]
            placeRowBackground(
                measureScope = measureScope,
                rowIndex = rowIndex,
                row = row,
                totalWidth = measureResult.totalWidth,
                rowHeight = rowHeight,
                yOffset = yOffset,
                density = density,
            )

            placeCellsInRow(
                measureScope = measureScope,
                measureResult = measureResult,
                rowIndex = rowIndex,
                yOffset = yOffset,
                borderWith = borderWith,
                density = density,
            )

            yOffset += rowHeight + borderHeight
        }

        placeTableBorders(
            measureScope = measureScope,
            border = border,
            measureResult = measureResult,
            density = density,
        )
    }
}

private fun Placeable.PlacementScope.placeRowBackground(
    measureScope: SubcomposeMeasureScope,
    rowIndex: Int,
    row: RowScopeImpl,
    totalWidth: Int,
    rowHeight: Int,
    yOffset: Int,
    density: Density,
) {
    if (row.modifier != Modifier) {
        val rowBackground =
            measureScope
                .subcompose("bg_$rowIndex") {
                    Box(
                        modifier =
                            row.modifier.size(
                                width = totalWidth.toDp(density),
                                height = rowHeight.toDp(density),
                            ),
                    )
                }[0]
                .measure(Constraints.fixed(totalWidth, rowHeight))
        rowBackground.place(0, yOffset)
    }
}

private fun Placeable.PlacementScope.placeCellsInRow(
    measureScope: SubcomposeMeasureScope,
    measureResult: TableMeasureResult,
    rowIndex: Int,
    yOffset: Int,
    borderWith: Int,
    density: Density,
) {
    var xOffset = 0
    for (columnIndex in 0 until measureResult.columnCount) {
        if (xOffset >= measureResult.totalWidth) {
            break
        }
        val placeable = measureResult.cellPlaceableMap[rowIndex][columnIndex]
        val cell = measureResult.cellData[rowIndex][columnIndex]

        if (placeable != null && cell != null) {
            val columnWidth = measureResult.columnWidths[columnIndex]
            val rowHeight = measureResult.rowHeights[rowIndex]

            placeCellBackground(
                measureScope = measureScope,
                rowIndex = rowIndex,
                columnIndex = columnIndex,
                cell = cell,
                cellWidth = columnWidth,
                cellHeight = rowHeight,
                xOffset = xOffset,
                yOffset = yOffset,
                density = density,
            )

            val (cellX, cellY) =
                calculateCellPosition(
                    alignment = cell.alignment,
                    cellWidth = placeable.width,
                    cellHeight = placeable.height,
                    availableWidth = columnWidth,
                    availableHeight = rowHeight,
                    baseX = xOffset,
                    baseY = yOffset,
                )

            placeable.place(cellX, cellY)
        }

        xOffset += measureResult.columnWidths[columnIndex] + borderWith
    }
}

private const val BORDER_COMPOSE = "border"

private fun Placeable.PlacementScope.placeTableBorders(
    measureScope: SubcomposeMeasureScope,
    border: TableBorder,
    measureResult: TableMeasureResult,
    density: Density,
) {
    if (border.mode != TableBorderMode.NONE) {
        val borderCanvas =
            measureScope
                .subcompose(BORDER_COMPOSE) {
                    TableBorderCanvas(
                        border = border,
                        columnWidths = measureResult.columnWidths.toImmutableList(),
                        rowHeights = measureResult.rowHeights.toImmutableList(),
                        density = density,
                    )
                }[0]
                .measure(Constraints.fixed(measureResult.totalWidth, measureResult.totalHeight))
        borderCanvas.place(0, 0)
    }
}

private fun Placeable.PlacementScope.placeCellBackground(
    measureScope: SubcomposeMeasureScope,
    rowIndex: Int,
    columnIndex: Int,
    cell: CellImpl,
    cellWidth: Int,
    cellHeight: Int,
    xOffset: Int,
    yOffset: Int,
    density: Density,
) {
    if (cell.cellBackground != null && cell.cellBackground != Modifier) {
        val cellBackground =
            measureScope
                .subcompose("cell_bg_${rowIndex}_$columnIndex") {
                    Box(
                        modifier =
                            cell.cellBackground.size(
                                width = cellWidth.toDp(density),
                                height = cellHeight.toDp(density),
                            ),
                    )
                }[0]
                .measure(Constraints.fixed(cellWidth, cellHeight))
        cellBackground.place(xOffset, yOffset)
    }
}

@Composable
private fun CellBox(
    cellPadding: PaddingValues,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.CenterStart,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier.padding(cellPadding),
        contentAlignment = contentAlignment,
    ) {
        content()
    }
}

private fun calculateCellPosition(
    alignment: Alignment,
    cellWidth: Int,
    cellHeight: Int,
    availableWidth: Int,
    availableHeight: Int,
    baseX: Int,
    baseY: Int,
): Pair<Int, Int> {
    val x: Int =
        when (alignment) {
            Alignment.TopStart, Alignment.CenterStart, Alignment.BottomStart -> {
                baseX
            }

            Alignment.TopCenter, Alignment.Center, Alignment.BottomCenter -> {
                baseX + (availableWidth - cellWidth) / 2
            }

            Alignment.TopEnd, Alignment.CenterEnd, Alignment.BottomEnd -> {
                baseX + availableWidth - cellWidth
            }

            else -> {
                baseX
            }
        }
    val y: Int =
        when (alignment) {
            Alignment.TopStart, Alignment.TopCenter, Alignment.TopEnd -> {
                baseY
            }

            Alignment.CenterStart, Alignment.Center, Alignment.CenterEnd -> {
                baseY + (availableHeight - cellHeight) / 2
            }

            Alignment.BottomStart, Alignment.BottomCenter, Alignment.BottomEnd -> {
                baseY + availableHeight - cellHeight
            }

            else -> {
                baseY
            }
        }
    return Pair(x, y)
}

private fun calculateWeightBasedColumnWidths(
    widthWeights: List<Float>,
    columnCount: Int,
    constraints: Constraints,
    border: TableBorder,
    density: Density,
): List<Int> {
    val processedWeights =
        when {
            widthWeights.size > columnCount -> {
                widthWeights.take(columnCount)
            }

            widthWeights.size < columnCount -> {
                widthWeights + List(columnCount - widthWeights.size) { 1.0f }
            }

            else -> {
                widthWeights
            }
        }

    val totalWeight = processedWeights.sum()
    val availableWidth =
        constraints.maxWidth - calculateBorderOffsetX(border, density) * (columnCount - 1)

    val columnWidths = MutableList(columnCount) { 0 }
    if (totalWeight == 0f) {
        val equalWidth = availableWidth / columnCount
        columnWidths.fill(equalWidth)
    } else {
        for (i in 0 until columnCount) {
            columnWidths[i] = (availableWidth * (processedWeights[i] / totalWeight)).toInt()
        }
    }
    return columnWidths
}

private fun Int.toDp(density: Density): Dp =
    with(density) {
        this@toDp.toDp()
    }

internal class TableScopeImpl(
    private val cellAlignment: Alignment = Alignment.CenterStart,
) : TableScope {
    var header: RowScopeImpl? = null
        private set
    var body: BodyScopeImpl? = null
        private set

    override fun header(
        modifier: Modifier,
        cellAlignment: Alignment?,
        content: RowScope.() -> Unit,
    ) {
        header =
            RowScopeImpl(
                modifier = modifier,
                rowDefaultAlignment = cellAlignment,
                tableDefaultAlignment = this@TableScopeImpl.cellAlignment,
            ).apply(content)
    }

    override fun body(content: BodyScope.() -> Unit) {
        body = BodyScopeImpl(cellAlignment).apply(content)
    }
}

internal fun TableScopeImpl.rows(): List<RowScopeImpl> =
    buildList {
        header?.let { add(it) }
        body?.let { addAll(it.rows) }
    }

internal class BodyScopeImpl(
    private val tableDefaultAlignment: Alignment,
) : BodyScope {
    val rows = mutableListOf<RowScopeImpl>()

    override fun row(
        modifier: Modifier,
        cellAlignment: Alignment?,
        content: RowScope.() -> Unit,
    ) {
        rows.add(
            RowScopeImpl(
                modifier = modifier,
                rowDefaultAlignment = cellAlignment,
                tableDefaultAlignment = tableDefaultAlignment,
            ).apply(content),
        )
    }
}

internal class RowScopeImpl(
    val modifier: Modifier = Modifier,
    private val rowDefaultAlignment: Alignment? = null,
    private val tableDefaultAlignment: Alignment = Alignment.CenterStart,
) : RowScope {
    val cells = mutableListOf<CellImpl>()

    override fun cell(
        modifier: Modifier,
        cellBackground: Modifier,
        alignment: Alignment?,
        content: @Composable () -> Unit,
    ) {
        val finalAlignment = alignment ?: rowDefaultAlignment ?: tableDefaultAlignment
        cells.add(CellImpl(modifier, cellBackground, finalAlignment, content))
    }
}

internal class CellImpl(
    val modifier: Modifier = Modifier,
    val cellBackground: Modifier? = null,
    val alignment: Alignment = Alignment.CenterStart,
    val content: @Composable () -> Unit,
)
