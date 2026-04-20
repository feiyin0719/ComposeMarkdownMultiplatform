package com.iffly.compose.markdown.multiplatform.task

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
import com.iffly.compose.markdown.multiplatform.core.renders.ListItemMarkerRenderer
import com.iffly.compose.markdown.multiplatform.core.renders.ListItemMarkerRendererImpl
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.MarkdownChildren
import com.iffly.compose.markdown.multiplatform.render.childNodes
import com.iffly.compose.markdown.multiplatform.util.StringExt.FIGURE_SPACE
import com.iffly.compose.markdown.multiplatform.util.getIndentLevel
import com.iffly.compose.markdown.multiplatform.util.isInQuoteBlock
import com.iffly.compose.markdown.multiplatform.util.isLooseList
import com.iffly.compose.markdown.multiplatform.widget.SelectionFormatText
import org.commonmark.ext.task.list.items.TaskListItemMarker
import org.commonmark.node.ListItem

/**
 * Marker renderer for task list items.
 * Draws a checkbox marker (☑ or ☐) using [drawText].
 */
class TaskListMarkerRenderer(
    private val isChecked: Boolean,
) : ListItemMarkerRenderer {
    private fun getMarkerText(): String = if (isChecked) "☑" else "☐"

    override fun measureMarker(
        textMeasurer: TextMeasurer,
        node: ListItem,
        style: TextStyle,
    ): TextLayoutResult {
        val marker = getMarkerText()
        return textMeasurer.measure(marker, style)
    }

    override fun DrawScope.drawMarker(textLayoutResult: TextLayoutResult) {
        drawText(textLayoutResult, topLeft = Offset(0f, 0f))
    }
}

/**
 * Block renderer for list items that handles both regular and task list items.
 *
 * When a [ListItem] contains a [TaskListItemMarker] child, renders a checkbox
 * marker (☑ or ☐) instead of the default bullet or number marker. For regular
 * list items without a task marker, renders the standard marker text.
 */
class TaskListItemRenderer : IBlockRenderer<ListItem> {
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

        val taskMarker = node.childNodes().filterIsInstance<TaskListItemMarker>().firstOrNull()

        val markerRenderer: ListItemMarkerRenderer =
            if (taskMarker != null) {
                TaskListMarkerRenderer(isChecked = taskMarker.isChecked)
            } else {
                ListItemMarkerRendererImpl()
            }

        val contentChildren =
            if (taskMarker != null) {
                node.childNodes().filter { it !is TaskListItemMarker }
            } else {
                null
            }

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
                    children = contentChildren,
                    modifier = Modifier.wrapContentSize(),
                    verticalArrangement = Arrangement.Top,
                    spacerHeight = spacerHeight,
                    onBeforeChild = { child, parent ->
                        val firstContentChild =
                            (contentChildren ?: parent.childNodes()).firstOrNull()
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
