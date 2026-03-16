package com.iffly.compose.markdown.multiplatform.table.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/** DSL marker annotation to restrict the scope of table builder functions. */
@DslMarker
annotation class TableDslMarker

/**
 * Top-level scope for building a table using the table DSL.
 * A table consists of an optional [header] and a [body].
 *
 * @see BodyScope
 * @see RowScope
 */
@TableDslMarker
interface TableScope {
    /**
     * Defines the header row of the table.
     *
     * @param modifier Modifier applied to the header row.
     * @param cellAlignment Optional default alignment for all cells in the header.
     * @param content Builder block for defining header cells via [RowScope].
     */
    fun header(
        modifier: Modifier = Modifier,
        cellAlignment: Alignment? = null,
        content: RowScope.() -> Unit,
    )

    /**
     * Defines the body section of the table, containing one or more rows.
     *
     * @param content Builder block for defining body rows via [BodyScope].
     */
    fun body(content: BodyScope.() -> Unit)
}

/**
 * Scope for defining rows within the table body.
 *
 * @see RowScope
 */
@TableDslMarker
interface BodyScope {
    /**
     * Adds a row to the table body.
     *
     * @param modifier Modifier applied to this row.
     * @param cellAlignment Optional default alignment for cells in this row.
     * @param content Builder block for defining cells via [RowScope].
     */
    fun row(
        modifier: Modifier = Modifier,
        cellAlignment: Alignment? = null,
        content: RowScope.() -> Unit,
    )
}

/**
 * Scope for defining cells within a table row.
 */
@TableDslMarker
interface RowScope {
    /**
     * Adds a cell to the current row.
     *
     * @param modifier Modifier applied to the cell content.
     * @param cellBackground Modifier applied as the cell background.
     * @param alignment Optional alignment for the cell content; overrides row and table defaults.
     * @param content Composable content of the cell.
     */
    fun cell(
        modifier: Modifier = Modifier,
        cellBackground: Modifier = Modifier,
        alignment: Alignment? = null,
        content: @Composable () -> Unit,
    )
}
