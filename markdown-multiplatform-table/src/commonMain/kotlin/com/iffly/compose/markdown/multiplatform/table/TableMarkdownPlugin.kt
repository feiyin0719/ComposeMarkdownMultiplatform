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
import org.intellij.markdown.IElementType
import org.intellij.markdown.flavours.gfm.GFMElementTypes
import org.intellij.markdown.flavours.gfm.GFMTokenTypes

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

class TableMarkdownPlugin(
    private val tableTheme: TableTheme = TableTheme(),
) : IMarkdownRenderPlugin {
    override fun blockRenderers(): Map<IElementType, IBlockRenderer> =
        mapOf(
            GFMElementTypes.TABLE to TableRenderer(tableTheme),
        )

    override fun inlineNodeStringBuilders(): Map<IElementType, IInlineNodeStringBuilder> =
        mapOf(
            GFMTokenTypes.CELL to TableCellNodeStringBuilder(tableTheme),
        )
}
