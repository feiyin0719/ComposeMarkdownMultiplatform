package com.iffly.compose.markdown.multiplatform.table

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iffly.compose.markdown.multiplatform.config.currentActionHandler
import com.iffly.compose.markdown.multiplatform.render.CompositeChildNodeStringBuilder
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.MarkdownInlineText
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import com.iffly.compose.markdown.multiplatform.table.widget.BodyScope
import com.iffly.compose.markdown.multiplatform.table.widget.RowScope
import com.iffly.compose.markdown.multiplatform.table.widget.Table
import com.iffly.compose.markdown.multiplatform.table.widget.TableBorder
import com.iffly.compose.markdown.multiplatform.table.widget.TableBorderMode
import com.iffly.compose.markdown.multiplatform.table.widget.TableScope
import com.iffly.compose.markdown.multiplatform.widget.DisableSelectionWrapper
import kotlinx.collections.immutable.toImmutableList
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableBody
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.node.Node

// -- Node tree traversal helper --

/**
 * Returns the direct children of a commonmark [Node] as a list.
 */
private fun Node.childNodesList(): List<Node> {
    val result = mutableListOf<Node>()
    var child = firstChild
    while (child != null) {
        result.add(child)
        child = child.next
    }
    return result
}

/**
 * Converts a [TableCell.Alignment] to [TextAlign].
 */
private fun TableCell.Alignment?.toTextAlign(): TextAlign =
    when (this) {
        TableCell.Alignment.CENTER -> TextAlign.Center
        TableCell.Alignment.RIGHT -> TextAlign.End
        else -> TextAlign.Start
    }

// -- Renderer interfaces --

/**
 * Functional interface for rendering a table widget section (title or cell) as a Composable.
 *
 * @see TableTitleRenderer
 * @see TableCellRenderer
 */
fun interface TableWidgetRenderer {
    @Suppress("ComposableNaming")
    @Composable
    operator fun invoke(
        node: Node,
        modifier: Modifier,
    )
}

/**
 * Default renderer for the table title bar, which displays a "Copy table" action.
 *
 * @param tableTheme Theme configuration controlling the title bar appearance.
 */
class TableTitleRenderer(
    private val tableTheme: TableTheme = TableTheme(),
) : TableWidgetRenderer {
    @Suppress("ComposableNaming")
    @Composable
    override fun invoke(
        node: Node,
        modifier: Modifier,
    ) {
        TableTitle(tableNode = node, modifier = modifier, tableTheme = tableTheme)
    }
}

/**
 * Default renderer for individual table cell content, displaying markdown text within a selection container.
 */
class TableCellRenderer : TableWidgetRenderer {
    @Suppress("ComposableNaming")
    @Composable
    override fun invoke(
        node: Node,
        modifier: Modifier,
    ) {
        SelectionContainer {
            MarkdownInlineText(
                parent = node,
                modifier = Modifier,
                textAlign = TextAlign.Start,
            )
        }
    }
}

/**
 * Block renderer for GFM table elements using commonmark-kotlin table extension,
 * delegating to [TableTitleRenderer] and [TableCellRenderer]
 * for the title bar and cell content respectively.
 *
 * @param tableTheme Theme configuration for the table visual appearance.
 * @param tableTitleRenderer Custom renderer for the table title bar; defaults to [TableTitleRenderer].
 * @param tableCellRenderer Custom renderer for table cells; defaults to [TableCellRenderer].
 * @see IBlockRenderer
 */
class TableRenderer(
    private val tableTheme: TableTheme = TableTheme(),
    tableTitleRenderer: TableWidgetRenderer? = null,
    tableCellRenderer: TableWidgetRenderer? = null,
) : IBlockRenderer<TableBlock> {
    private val tableTitleRenderer: TableWidgetRenderer =
        tableTitleRenderer ?: TableTitleRenderer(tableTheme)
    private val tableCellRenderer: TableWidgetRenderer =
        tableCellRenderer ?: TableCellRenderer()

    @Composable
    override fun Invoke(
        node: TableBlock,
        modifier: Modifier,
    ) {
        MarkdownTable(
            tableNode = node,
            modifier = modifier,
            tableTitleRenderer = tableTitleRenderer,
            tableCellRenderer = tableCellRenderer,
            tableTheme = tableTheme,
        )
    }
}

/**
 * Inline node string builder for table cells that applies header or body text styles
 * from the [TableTheme] based on the cell's [TableCell.isHeader] property.
 *
 * @param tableTheme Theme providing the text styles for header and body cells.
 * @see CompositeChildNodeStringBuilder
 */
class TableCellNodeStringBuilder(
    private val tableTheme: TableTheme,
) : CompositeChildNodeStringBuilder() {
    override fun getSpanStyle(
        node: Node,
        markdownTheme: MarkdownTheme,
    ): SpanStyle? {
        val isHeader = (node as? TableCell)?.isHeader == true
        return if (isHeader) {
            tableTheme.headerTextStyle?.toSpanStyle()
        } else {
            tableTheme.cellTextStyle?.toSpanStyle()
        }
    }

    override fun getParagraphStyle(
        node: Node,
        markdownTheme: MarkdownTheme,
    ): ParagraphStyle? {
        val isHeader = (node as? TableCell)?.isHeader == true
        return if (isHeader) {
            tableTheme.headerTextStyle?.toParagraphStyle()
        } else {
            tableTheme.cellTextStyle?.toParagraphStyle()
        }
    }
}

// -- Data classes for parsed table structure --

private data class TableCellData(
    val node: TableCell,
    val alignment: TextAlign,
)

private data class ParsedTableData(
    val cells: List<List<TableCellData>>,
    val alignments: List<TextAlign>,
)

// -- AST parsing for commonmark-kotlin table nodes --

/**
 * Extracts [ParsedTableData] from a [TableBlock] by walking its child nodes:
 * [TableHead] -> [TableRow] -> [TableCell] and [TableBody] -> [TableRow] -> [TableCell].
 */
private fun parseTableBlock(tableNode: TableBlock): ParsedTableData {
    val rows = mutableListOf<List<TableCellData>>()
    val alignments = mutableListOf<TextAlign>()

    for (child in tableNode.childNodesList()) {
        when (child) {
            is TableHead -> {
                for (row in child.childNodesList()) {
                    if (row is TableRow) {
                        val cellDataList =
                            row
                                .childNodesList()
                                .filterIsInstance<TableCell>()
                                .map { cell ->
                                    TableCellData(
                                        node = cell,
                                        alignment = cell.alignment.toTextAlign(),
                                    )
                                }
                        rows.add(cellDataList)
                        // Capture alignments from the first header row
                        if (alignments.isEmpty()) {
                            alignments.addAll(cellDataList.map { it.alignment })
                        }
                    }
                }
            }

            is TableBody -> {
                for (row in child.childNodesList()) {
                    if (row is TableRow) {
                        val cellDataList =
                            row
                                .childNodesList()
                                .filterIsInstance<TableCell>()
                                .mapIndexed { index, cell ->
                                    TableCellData(
                                        node = cell,
                                        alignment =
                                            cell.alignment?.toTextAlign()
                                                ?: alignments.getOrElse(index) { TextAlign.Start },
                                    )
                                }
                        rows.add(cellDataList)
                    }
                }
            }
        }
    }

    return ParsedTableData(cells = rows, alignments = alignments)
}

/**
 * Composable that renders a GFM markdown table with a title bar, header row, and body rows.
 *
 * Parses the [TableBlock] AST node to extract cell data and column alignments, then renders
 * using the custom [Table] layout with borders and scrolling support.
 *
 * @param tableNode The commonmark [TableBlock] node representing the GFM table element.
 * @param tableTitleRenderer Renderer for the table title bar.
 * @param tableCellRenderer Renderer for individual cell content.
 * @param modifier Modifier applied to the outer table container.
 * @param tableTheme Theme configuration controlling the table appearance.
 */
@Composable
fun MarkdownTable(
    tableNode: TableBlock,
    tableTitleRenderer: TableWidgetRenderer,
    tableCellRenderer: TableWidgetRenderer,
    modifier: Modifier = Modifier,
    tableTheme: TableTheme = TableTheme(),
) {
    val tableData = parseTableBlock(tableNode)
    val columnsCount = tableData.cells.firstOrNull()?.size ?: 0
    if (columnsCount == 0 || tableData.cells.isEmpty()) return
    val borderColor = tableTheme.borderColor
    val widthWeights = if (columnsCount <= 2) List(columnsCount) { 1f } else null

    Column(
        modifier =
            modifier
                .wrapContentSize()
                .clip(tableTheme.shape)
                .border(
                    tableTheme.borderThickness,
                    borderColor,
                    tableTheme.shape,
                ),
    ) {
        tableTitleRenderer(tableNode, Modifier.fillMaxWidth())
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(tableTheme.borderThickness)
                .background(borderColor),
        )
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val cellMinWidth = this.minWidth / 3 - 1.dp
            val cellModifier = Modifier.cellModifier(columnsCount, cellMinWidth)
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .boxModifier(columnsCount, rememberScrollState()),
            ) {
                Table(
                    modifier = Modifier.tableModifier(columnsCount),
                    widthWeights = widthWeights?.toImmutableList(),
                    cellPadding = tableTheme.cellPadding,
                    border =
                        TableBorder.solid(
                            mode = TableBorderMode.ALL,
                            color = borderColor,
                            width = tableTheme.borderThickness,
                        ),
                    cellAlignment = Alignment.TopStart,
                ) {
                    tableHeader(
                        headerCells = tableData.cells.first(),
                        alignments = tableData.alignments,
                        modifier = cellModifier,
                        backgroundColor = tableTheme.tableHeaderBackgroundColor,
                        cellContent = tableCellRenderer,
                    )
                    val bodyCells =
                        if (tableData.cells.size > 1) {
                            tableData.cells.subList(1, tableData.cells.size)
                        } else {
                            null
                        }
                    bodyCells?.let {
                        tableBody(
                            rows = it,
                            alignments = tableData.alignments,
                            modifier = cellModifier,
                            backgroundColor = tableTheme.tableCellBackgroundColor,
                            cellContent = tableCellRenderer,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TableTitle(
    tableNode: Node,
    modifier: Modifier = Modifier,
    tableTheme: TableTheme = TableTheme(),
) {
    val actionHandler = currentActionHandler()
    DisableSelectionWrapper(disabled = true) {
        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .background(tableTheme.titleBackgroundColor)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Copy table",
                style = tableTheme.copyTextStyle,
                modifier =
                    Modifier.clickable {
                        actionHandler?.handleCopyClick(tableNode)
                    },
            )
        }
    }
}

// -- Modifier helpers --

private fun Modifier.cellModifier(
    columnsCount: Int,
    minWidth: Dp,
): Modifier =
    if (columnsCount <= 2) {
        fillMaxSize()
    } else {
        val maxWidth = if (minWidth > 167.dp) minWidth else 167.dp
        fillMaxHeight()
            .wrapContentWidth()
            .widthIn(max = maxWidth, min = minWidth)
    }

private fun Modifier.boxModifier(
    columnsCount: Int,
    state: ScrollState,
): Modifier =
    if (columnsCount <= 2) {
        this
    } else {
        this.horizontalScroll(state)
    }

private fun Modifier.tableModifier(columnsCount: Int): Modifier =
    if (columnsCount <= 2) {
        this.fillMaxWidth()
    } else {
        this
    }

// -- Table DSL helpers --

private fun TableScope.tableHeader(
    headerCells: List<TableCellData>,
    alignments: List<TextAlign>,
    modifier: Modifier,
    backgroundColor: androidx.compose.ui.graphics.Color,
    cellContent: TableWidgetRenderer,
) {
    header(modifier = Modifier.background(backgroundColor)) {
        tableCell(
            cells = headerCells,
            modifier = modifier,
            cellContent = cellContent,
        )
    }
}

private fun TableScope.tableBody(
    rows: List<List<TableCellData>>,
    alignments: List<TextAlign>,
    modifier: Modifier,
    backgroundColor: androidx.compose.ui.graphics.Color,
    cellContent: TableWidgetRenderer,
) {
    body {
        tableRow(
            rows = rows,
            modifier = modifier,
            backgroundColor = backgroundColor,
            cellContent = cellContent,
        )
    }
}

private fun BodyScope.tableRow(
    rows: List<List<TableCellData>>,
    modifier: Modifier,
    backgroundColor: androidx.compose.ui.graphics.Color,
    cellContent: TableWidgetRenderer,
) {
    rows.forEach { rowCells ->
        row(Modifier.background(backgroundColor)) {
            tableCell(
                cells = rowCells,
                modifier = modifier,
                cellContent = cellContent,
            )
        }
    }
}

private fun RowScope.tableCell(
    cells: List<TableCellData>,
    modifier: Modifier,
    cellContent: TableWidgetRenderer,
) {
    cells.forEach { cellData ->
        cell(alignment = cellData.alignment.toTableAlignment(), modifier = modifier) {
            cellContent(cellData.node, Modifier)
        }
    }
}

private fun TextAlign.toTableAlignment(): Alignment =
    when (this) {
        TextAlign.Center -> Alignment.TopCenter
        TextAlign.End -> Alignment.TopEnd
        else -> Alignment.TopStart
    }
