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
import com.iffly.compose.markdown.multiplatform.render.MarkdownText
import com.iffly.compose.markdown.multiplatform.style.MarkdownTheme
import com.iffly.compose.markdown.multiplatform.table.widget.BodyScope
import com.iffly.compose.markdown.multiplatform.table.widget.RowScope
import com.iffly.compose.markdown.multiplatform.table.widget.Table
import com.iffly.compose.markdown.multiplatform.table.widget.TableBorder
import com.iffly.compose.markdown.multiplatform.table.widget.TableBorderMode
import com.iffly.compose.markdown.multiplatform.table.widget.TableScope
import com.iffly.compose.markdown.multiplatform.widget.DisableSelectionWrapper
import kotlinx.collections.immutable.toImmutableList
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMTokenTypes

fun interface TableWidgetRenderer {
    @Suppress("ComposableNaming")
    @Composable
    operator fun invoke(
        node: ASTNode,
        sourceText: String,
        modifier: Modifier,
    )
}

class TableTitleRenderer(
    private val tableTheme: TableTheme = TableTheme(),
) : TableWidgetRenderer {
    @Suppress("ComposableNaming")
    @Composable
    override fun invoke(
        node: ASTNode,
        sourceText: String,
        modifier: Modifier,
    ) {
        TableTitle(tableNode = node, modifier = modifier, tableTheme = tableTheme)
    }
}

class TableCellRenderer : TableWidgetRenderer {
    @Suppress("ComposableNaming")
    @Composable
    override fun invoke(
        node: ASTNode,
        sourceText: String,
        modifier: Modifier,
    ) {
        SelectionContainer {
            MarkdownText(
                parent = node,
                modifier = Modifier,
                textAlign = TextAlign.Start,
            )
        }
    }
}

class TableRenderer(
    private val tableTheme: TableTheme = TableTheme(),
    tableTitleRenderer: TableWidgetRenderer? = null,
    tableCellRenderer: TableWidgetRenderer? = null,
) : IBlockRenderer {
    private val tableTitleRenderer: TableWidgetRenderer =
        tableTitleRenderer ?: TableTitleRenderer(tableTheme)
    private val tableCellRenderer: TableWidgetRenderer =
        tableCellRenderer ?: TableCellRenderer()

    @Composable
    override fun Invoke(
        node: ASTNode,
        sourceText: String,
        modifier: Modifier,
    ) {
        MarkdownTable(
            tableNode = node,
            sourceText = sourceText,
            modifier = modifier,
            tableTitleRenderer = tableTitleRenderer,
            tableCellRenderer = tableCellRenderer,
            tableTheme = tableTheme,
        )
    }
}

class TableCellNodeStringBuilder(
    private val tableTheme: TableTheme,
) : CompositeChildNodeStringBuilder() {
    override fun getSpanStyle(
        node: ASTNode,
        markdownTheme: MarkdownTheme,
    ): SpanStyle? {
        val isHeader = node.parent?.type == GFMElementTypes.HEADER
        return if (isHeader) {
            tableTheme.headerTextStyle?.toSpanStyle()
        } else {
            tableTheme.cellTextStyle?.toSpanStyle()
        }
    }

    override fun getParagraphStyle(
        node: ASTNode,
        markdownTheme: MarkdownTheme,
    ): ParagraphStyle? {
        val isHeader = node.parent?.type == GFMElementTypes.HEADER
        return if (isHeader) {
            tableTheme.headerTextStyle?.toParagraphStyle()
        } else {
            tableTheme.cellTextStyle?.toParagraphStyle()
        }
    }
}

@Composable
fun MarkdownTable(
    tableNode: ASTNode,
    sourceText: String,
    tableTitleRenderer: TableWidgetRenderer,
    tableCellRenderer: TableWidgetRenderer,
    modifier: Modifier = Modifier,
    tableTheme: TableTheme = TableTheme(),
) {
    val tableData = parseTableCells(tableNode, sourceText)
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
        tableTitleRenderer(tableNode, sourceText, Modifier.fillMaxWidth())
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
                        sourceText = sourceText,
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
                            sourceText = sourceText,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TableTitle(
    tableNode: ASTNode,
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

// -- AST Parsing helpers for intellij-markdown GFM tables --

private data class TableCellData(
    val node: ASTNode,
    val alignment: TextAlign,
)

private data class ParsedTableData(
    val cells: List<List<TableCellData>>,
    val alignments: List<TextAlign>,
)

private fun parseTableCells(
    tableNode: ASTNode,
    sourceText: String,
): ParsedTableData {
    val rows = mutableListOf<List<TableCellData>>()
    var alignments = emptyList<TextAlign>()

    for (child in tableNode.children) {
        when (child.type) {
            GFMElementTypes.HEADER -> {
                if (alignments.isEmpty()) {
                    // Parse alignments from the separator row that follows header
                    alignments = findAlignments(tableNode, sourceText)
                }
                val cellNodes = child.children.filter { it.type == GFMTokenTypes.CELL }
                rows.add(
                    cellNodes.mapIndexed { index, node ->
                        TableCellData(
                            node = node,
                            alignment = alignments.getOrElse(index) { TextAlign.Start },
                        )
                    },
                )
            }

            GFMElementTypes.ROW -> {
                val cellNodes = child.children.filter { it.type == GFMTokenTypes.CELL }
                rows.add(
                    cellNodes.mapIndexed { index, node ->
                        TableCellData(
                            node = node,
                            alignment = alignments.getOrElse(index) { TextAlign.Start },
                        )
                    },
                )
            }
        }
    }

    return ParsedTableData(cells = rows, alignments = alignments)
}

private fun findAlignments(
    tableNode: ASTNode,
    sourceText: String,
): List<TextAlign> {
    val separatorNode =
        tableNode.children.firstOrNull {
            it.type == GFMTokenTypes.TABLE_SEPARATOR
        } ?: return emptyList()

    val text = separatorNode.getTextInNode(sourceText).toString()
    return text
        .split('|')
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .map { segment ->
            val left = segment.startsWith(':')
            val right = segment.endsWith(':')
            when {
                left && right -> TextAlign.Center
                right -> TextAlign.End
                else -> TextAlign.Start
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
    sourceText: String,
) {
    header(modifier = Modifier.background(backgroundColor)) {
        tableCell(
            cells = headerCells,
            modifier = modifier,
            cellContent = cellContent,
            sourceText = sourceText,
        )
    }
}

private fun TableScope.tableBody(
    rows: List<List<TableCellData>>,
    alignments: List<TextAlign>,
    modifier: Modifier,
    backgroundColor: androidx.compose.ui.graphics.Color,
    cellContent: TableWidgetRenderer,
    sourceText: String,
) {
    body {
        tableRow(
            rows = rows,
            modifier = modifier,
            backgroundColor = backgroundColor,
            cellContent = cellContent,
            sourceText = sourceText,
        )
    }
}

private fun BodyScope.tableRow(
    rows: List<List<TableCellData>>,
    modifier: Modifier,
    backgroundColor: androidx.compose.ui.graphics.Color,
    cellContent: TableWidgetRenderer,
    sourceText: String,
) {
    rows.forEach { rowCells ->
        row(Modifier.background(backgroundColor)) {
            tableCell(
                cells = rowCells,
                modifier = modifier,
                cellContent = cellContent,
                sourceText = sourceText,
            )
        }
    }
}

private fun RowScope.tableCell(
    cells: List<TableCellData>,
    modifier: Modifier,
    cellContent: TableWidgetRenderer,
    sourceText: String,
) {
    cells.forEach { cellData ->
        cell(alignment = cellData.alignment.toTableAlignment(), modifier = modifier) {
            cellContent(cellData.node, sourceText, Modifier)
        }
    }
}

private fun TextAlign.toTableAlignment(): Alignment =
    when (this) {
        TextAlign.Center -> Alignment.TopCenter
        TextAlign.End -> Alignment.TopEnd
        else -> Alignment.TopStart
    }
