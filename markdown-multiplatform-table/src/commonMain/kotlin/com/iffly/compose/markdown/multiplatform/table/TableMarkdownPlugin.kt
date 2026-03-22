package com.iffly.compose.markdown.multiplatform.table

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iffly.compose.markdown.multiplatform.config.IMarkdownRenderPlugin
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.IInlineNodeStringBuilder
import org.commonmark.Extension
import org.commonmark.ext.gfm.tables.TableBlock
import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.Node
import kotlin.reflect.KClass

/**
 * Theme configuration for markdown table rendering, controlling colors, text styles, shape, and padding.
 *
 * @param borderColor Color used for the table border lines.
 * @param borderThickness Thickness of the table border lines.
 * @param titleBackgroundColor Background color for the table title bar.
 * @param tableHeaderBackgroundColor Background color for the header row.
 * @param tableCellBackgroundColor Background color for body cells.
 * @param cellTextStyle Optional text style applied to body cell text.
 * @param headerTextStyle Optional text style applied to header cell text.
 * @param copyTextStyle Text style for the "Copy table" action label.
 * @param shape Shape applied to the outer table container.
 * @param cellPadding Padding applied inside each table cell.
 */
data class TableTheme(
    val borderColor: Color = Color.Gray,
    val borderThickness: Dp = 1.dp,
    val titleBackgroundColor: Color = Color.LightGray,
    val tableHeaderBackgroundColor: Color = Color.White,
    val tableCellBackgroundColor: Color = Color.White,
    val cellTextStyle: TextStyle? = null,
    val headerTextStyle: TextStyle? = TextStyle(fontWeight = FontWeight.Bold),
    val copyTextStyle: TextStyle =
        TextStyle(
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.Black,
        ),
    val shape: Shape = RoundedCornerShape(8.dp),
    val cellPadding: PaddingValues = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
)

/**
 * Markdown render plugin that adds GFM (GitHub Flavored Markdown) table support
 * using the commonmark-kotlin table extension.
 *
 * Registers a parser extension for table parsing, a block renderer for [TableBlock],
 * and an inline node string builder for [TableCell].
 *
 * @param tableTheme Theme configuration controlling the visual appearance of tables.
 * @see IMarkdownRenderPlugin
 */
class TableMarkdownPlugin(
    private val tableTheme: TableTheme = TableTheme(),
) : IMarkdownRenderPlugin {
    override fun parserExtensions(): List<Extension> = listOf(TablesExtension.create())

    override fun blockRenderers(): Map<KClass<out Node>, IBlockRenderer<*>> =
        mapOf(
            TableBlock::class to TableRenderer(tableTheme),
        )

    override fun inlineNodeStringBuilders(): Map<KClass<out Node>, IInlineNodeStringBuilder<*>> =
        mapOf(
            TableCell::class to TableCellNodeStringBuilder(tableTheme),
        )
}
