package com.iffly.compose.markdown.multiplatform.task

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
import com.iffly.compose.markdown.multiplatform.render.childNodes
import com.iffly.compose.markdown.multiplatform.util.StringExt.FIGURE_SPACE
import com.iffly.compose.markdown.multiplatform.util.getIndentLevel
import com.iffly.compose.markdown.multiplatform.util.getMarkerText
import com.iffly.compose.markdown.multiplatform.util.isInQuoteBlock
import com.iffly.compose.markdown.multiplatform.util.isLooseList
import com.iffly.compose.markdown.multiplatform.widget.SelectionFormatText
import org.commonmark.ext.task.list.items.TaskListItemMarker
import org.commonmark.node.ListItem

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
        val marker =
            if (taskMarker != null) {
                if (taskMarker.isChecked) "☑" else "☐"
            } else {
                node.getMarkerText()
            }

        val contentChildren =
            if (taskMarker != null) {
                node.childNodes().filter { it !is TaskListItemMarker }
            } else {
                null
            }

        Row(
            modifier =
                modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
        ) {
            val isFirstChild =
                node.parent?.childNodes()?.firstOrNull { it is ListItem } == node
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
        }
    }
}
