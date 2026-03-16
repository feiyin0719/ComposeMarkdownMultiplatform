package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import com.iffly.compose.markdown.multiplatform.config.currentTheme
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.MarkdownChildren
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode

/**
 * Block renderer for blockquote elements (`> text`).
 *
 * Renders the quoted content with a left border line drawn behind the content,
 * a configurable background color, and appropriate padding. Supports nested
 * blockquotes. Theming is controlled via [MarkdownTheme.blockQuoteTheme].
 *
 * @see IBlockRenderer
 */
class BlockQuoteRenderer : IBlockRenderer {
    @Composable
    override fun Invoke(
        node: ASTNode,
        sourceText: String,
        modifier: Modifier,
    ) {
        val theme = currentTheme()
        val blockQuoteTheme = theme.blockQuoteTheme
        val borderColor = blockQuoteTheme.borderColor
        val spacerHeight = theme.spacerTheme.spacerHeight

        MarkdownChildren(
            parent = node,
            sourceText = sourceText,
            modifier =
                modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(
                        color = blockQuoteTheme.backgroundColor,
                        shape = blockQuoteTheme.shape,
                    ).drawBehind {
                        val borderWidth = blockQuoteTheme.borderWidth.toPx()
                        val x = borderWidth / 2
                        drawLine(
                            color = borderColor,
                            start = Offset(x, 0f),
                            end = Offset(x, size.height),
                            strokeWidth = borderWidth,
                        )
                    }.padding(blockQuoteTheme.padding),
            childModifierFactory = { Modifier },
            onBeforeAll = {
                Spacer(modifier = Modifier.height(spacerHeight))
            },
            onAfterAll = { parent ->
                val lastChild =
                    parent.children.lastOrNull {
                        it.type != MarkdownTokenTypes.EOL &&
                            it.type != MarkdownTokenTypes.WHITE_SPACE &&
                            it.type != MarkdownTokenTypes.BLOCK_QUOTE
                    }
                if (lastChild?.type != MarkdownElementTypes.BLOCK_QUOTE) {
                    Spacer(modifier = Modifier.height(spacerHeight))
                }
            },
        )
    }
}
