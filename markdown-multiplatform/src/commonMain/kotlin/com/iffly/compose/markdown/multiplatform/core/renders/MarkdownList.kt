package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import com.iffly.compose.markdown.multiplatform.config.currentTheme
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.MarkdownChildren
import com.iffly.compose.markdown.multiplatform.render.childNodes
import com.iffly.compose.markdown.multiplatform.util.StringExt.FIGURE_SPACE
import com.iffly.compose.markdown.multiplatform.util.getIndentLevel
import com.iffly.compose.markdown.multiplatform.util.getMarkerText
import com.iffly.compose.markdown.multiplatform.util.isInQuoteBlock
import com.iffly.compose.markdown.multiplatform.util.isLooseList
import com.iffly.compose.markdown.multiplatform.widget.SelectionFormatText
import org.commonmark.node.ListItem
import org.commonmark.node.Node

class ListBlockRenderer : IBlockRenderer<Node> {
    @Composable
    override fun Invoke(
        node: Node,
        modifier: Modifier,
    ) {
        val theme = currentTheme()
        val isLoose = node.isLooseList()
        val spacerHeight =
            if (isLoose) theme.spacerTheme.spacerHeight else theme.listTheme.tightListSpacerHeight
        MarkdownChildren(
            parent = node,
            children = node.childNodes().filterIsInstance<ListItem>(),
            modifier = modifier,
            verticalArrangement = Arrangement.Top,
            spacerHeight = spacerHeight,
        )
    }
}

/**
 * Interface for rendering ListItem markers via [DrawScope].
 * The marker is drawn on canvas instead of being a separate Text composable,
 * so it does not participate in text selection and avoids extra `\n` when copying.
 *
 * The workflow is: [measureMarker] → use [TextLayoutResult] for offset calculation → [DrawScope.drawMarker].
 */
interface ListItemMarkerRenderer {
    /**
     * Measure the marker text and return the [TextLayoutResult].
     * The caller uses [TextLayoutResult.size] to compute the content offset,
     * then passes the same result to [drawMarker] for rendering.
     */
    fun measureMarker(
        textMeasurer: TextMeasurer,
        node: ListItem,
        style: TextStyle,
    ): TextLayoutResult

    /**
     * Draw the marker in the given [DrawScope] using a pre-measured [TextLayoutResult].
     * @param textLayoutResult The result returned by [measureMarker].
     */
    fun DrawScope.drawMarker(textLayoutResult: TextLayoutResult)
}

/**
 * The default implementation of [ListItemMarkerRenderer].
 * Draws the bullet point or ordered number marker using [drawText].
 */
class ListItemMarkerRendererImpl : ListItemMarkerRenderer {
    override fun measureMarker(
        textMeasurer: TextMeasurer,
        node: ListItem,
        style: TextStyle,
    ): TextLayoutResult {
        val marker = node.getMarkerText()
        return textMeasurer.measure(marker, style)
    }

    override fun DrawScope.drawMarker(textLayoutResult: TextLayoutResult) {
        drawText(textLayoutResult, topLeft = Offset(0f, 0f))
    }
}

class ListItemRenderer(
    private val markerRenderer: ListItemMarkerRenderer = ListItemMarkerRendererImpl(),
) : IBlockRenderer<ListItem> {
    @Composable
    override fun Invoke(
        node: ListItem,
        modifier: Modifier,
    ) {
        val theme = currentTheme()
        val listTheme = theme.listTheme
        val indentLevel = node.getIndentLevel()
        val isInQuoteBlock = node.isInQuoteBlock()
        val isLoose = node.parent?.isLooseList() ?: false
        val spacerHeight =
            if (isLoose) theme.spacerTheme.spacerHeight else theme.listTheme.tightListSpacerHeight

        val mergedTextStyle =
            (listTheme.markerTextStyle ?: theme.textStyle)
                .merge(
                    theme.blockQuoteTheme.textStyle.takeIf { isInQuoteBlock },
                )

        val textMeasurer = rememberTextMeasurer()
        val density = LocalDensity.current
        val spacerWidthPx = with(density) { listTheme.markerSpacerWidth.toPx() }

        val markerTextLayoutResult =
            markerRenderer.measureMarker(
                textMeasurer = textMeasurer,
                node = node,
                style = mergedTextStyle,
            )
        val markerOffset = markerTextLayoutResult.size.width + spacerWidthPx.toInt()

        Layout(
            content = {
                val isFirstChild =
                    node.parent?.childNodes()?.firstOrNull { it is ListItem } == node
                if (!isFirstChild && indentLevel > 0) {
                    SelectionFormatText(FIGURE_SPACE.repeat(indentLevel))
                }
                MarkdownChildren(
                    parent = node,
                    modifier = Modifier.wrapContentSize(),
                    verticalArrangement = Arrangement.Top,
                    spacerHeight = spacerHeight,
                    onBeforeChild = { child, parent ->
                        val firstContentChild = parent.childNodes().firstOrNull()
                        if (child != firstContentChild) {
                            SelectionFormatText(FIGURE_SPACE.repeat(indentLevel + 1))
                        }
                    },
                )
            },
            modifier =
                modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .drawBehind {
                        with(markerRenderer) {
                            drawMarker(markerTextLayoutResult)
                        }
                    },
        ) { measurables, constraints ->
            val contentConstraints =
                constraints.copy(
                    maxWidth = (constraints.maxWidth - markerOffset).coerceAtLeast(0),
                    minWidth = 0,
                )
            val placeables = measurables.map { it.measure(contentConstraints) }
            val height = placeables.sumOf { it.height }

            layout(constraints.maxWidth, height) {
                var yOffset = 0
                placeables.forEach { placeable ->
                    placeable.place(markerOffset, yOffset)
                    yOffset += placeable.height
                }
            }
        }
    }
}
