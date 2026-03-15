package com.iffly.compose.markdown.multiplatform.table.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@DslMarker
annotation class TableDslMarker

@TableDslMarker
interface TableScope {
    fun header(
        modifier: Modifier = Modifier,
        cellAlignment: Alignment? = null,
        content: RowScope.() -> Unit,
    )

    fun body(content: BodyScope.() -> Unit)
}

@TableDslMarker
interface BodyScope {
    fun row(
        modifier: Modifier = Modifier,
        cellAlignment: Alignment? = null,
        content: RowScope.() -> Unit,
    )
}

@TableDslMarker
interface RowScope {
    fun cell(
        modifier: Modifier = Modifier,
        cellBackground: Modifier = Modifier,
        alignment: Alignment? = null,
        content: @Composable () -> Unit,
    )
}
