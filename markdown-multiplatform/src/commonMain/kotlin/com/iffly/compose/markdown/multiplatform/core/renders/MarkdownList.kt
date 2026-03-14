package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.multiplatform.config.currentTheme
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.MarkdownChildren
import com.iffly.compose.markdown.multiplatform.util.StringExt.FIGURE_SPACE
import com.iffly.compose.markdown.multiplatform.util.getIndentLevel
import com.iffly.compose.markdown.multiplatform.util.getMarkerText
import com.iffly.compose.markdown.multiplatform.util.isInQuoteBlock
import com.iffly.compose.markdown.multiplatform.util.isLooseList
import com.iffly.compose.markdown.multiplatform.widget.SelectionFormatText
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.MarkdownTokenTypes
import org.intellij.markdown.ast.ASTNode

class ListBlockRenderer : IBlockRenderer {
    @Composable
    override fun Invoke(
        node: ASTNode,
        sourceText: String,
        modifier: Modifier,
    ) {
        val theme = currentTheme()
        val isLoose = node.isLooseList()
        val spacerHeight =
            if (isLoose) theme.spacerTheme.spacerHeight else theme.listTheme.tightListSpacerHeight
        MarkdownChildren(
            parent = node,
            children = node.children.filter { it.type == MarkdownElementTypes.LIST_ITEM },
            sourceText = sourceText,
            modifier = modifier,
            verticalArrangement = Arrangement.Top,
            spacerHeight = spacerHeight,
        )
    }
}

class ListItemRenderer : IBlockRenderer {
    @Composable
    override fun Invoke(
        node: ASTNode,
        sourceText: String,
        modifier: Modifier,
    ) {
        val theme = currentTheme()
        val listTheme = theme.listTheme
        val indentLevel = node.getIndentLevel()
        val marker = node.getMarkerText()
        val isInQuoteBlock = node.isInQuoteBlock()
        val isLoose = node.parent?.isLooseList() ?: false
        val spacerHeight =
            if (isLoose) theme.spacerTheme.spacerHeight else theme.listTheme.tightListSpacerHeight

        val mergedTextStyle =
            (listTheme.markerTextStyle ?: theme.textStyle)
                .merge(
                    theme.blockQuoteTheme.textStyle.takeIf { isInQuoteBlock },
                )

        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
        ) {
            val isFirstChild =
                node.parent
                    ?.children
                    ?.firstOrNull { it.type == MarkdownElementTypes.LIST_ITEM } == node
            if (!isFirstChild && indentLevel > 0) {
                SelectionFormatText(FIGURE_SPACE.repeat(indentLevel))
            }
            Text(
                text = marker,
                style = mergedTextStyle,
                modifier = Modifier.wrapContentHeight(),
            )
            Spacer(modifier = Modifier.width(listTheme.markerSpacerWidth))
            MarkdownChildren(
                parent = node,
                children =
                    node.children.filter {
                        it.type != MarkdownTokenTypes.LIST_BULLET &&
                            it.type != MarkdownTokenTypes.LIST_NUMBER
                    },
                sourceText = sourceText,
                modifier = Modifier.wrapContentSize(),
                verticalArrangement = Arrangement.Top,
                spacerHeight = spacerHeight,
                onBeforeChild = { child, parent ->
                    val firstContentChild =
                        parent.children.firstOrNull {
                            it.type != MarkdownTokenTypes.EOL &&
                                it.type != MarkdownTokenTypes.WHITE_SPACE &&
                                it.type != MarkdownTokenTypes.LIST_BULLET &&
                                it.type != MarkdownTokenTypes.LIST_NUMBER
                        }
                    if (child != firstContentChild) {
                        SelectionFormatText(FIGURE_SPACE.repeat(indentLevel + 1))
                    }
                },
            )
        }
    }
}
