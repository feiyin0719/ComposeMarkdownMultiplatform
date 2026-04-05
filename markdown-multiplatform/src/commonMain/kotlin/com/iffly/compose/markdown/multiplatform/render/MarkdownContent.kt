package com.iffly.compose.markdown.multiplatform.render

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.iffly.compose.markdown.multiplatform.config.currentRenderRegistry
import com.iffly.compose.markdown.multiplatform.config.currentTheme
import org.commonmark.node.Node

/** Functional interface for custom rendering of the top-level markdown content composable. */
fun interface MarkdownContentRenderer {
    @Composable
    operator fun invoke(
        node: Node,
        modifier: Modifier,
    )
}

@Composable
fun MarkdownContent(
    node: Node,
    modifier: Modifier = Modifier,
) {
    val renderRegistry = currentRenderRegistry()
    renderRegistry.markdownContentRenderer?.invoke(
        node = node,
        modifier = modifier,
    ) ?: DefaultMarkdownContent(
        node = node,
        modifier = modifier,
    )
}

@Composable
private fun DefaultMarkdownContent(
    node: Node,
    modifier: Modifier = Modifier,
) {
    val renderRegistry = currentRenderRegistry()
    val renderer = renderRegistry.getBlockRenderer(node)
    if (renderer != null) {
        if (!renderer.shouldSkipRender(node)) {
            renderer.Invoke(node, modifier)
        }
    } else {
        MarkdownInlineText(node, modifier = modifier)
    }
}

@Composable
fun MarkdownChildren(
    parent: Node,
    modifier: Modifier = Modifier,
    children: List<Node>? = null,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    spacerHeight: Dp = currentTheme().spacerTheme.spacerHeight,
    showSpacer: Boolean = currentTheme().spacerTheme.showSpacer,
    childModifierFactory: (child: Node) -> Modifier = {
        Modifier
            .wrapContentHeight()
            .fillMaxWidth()
    },
    onBeforeChild: (@Composable (child: Node, parent: Node) -> Unit)? = null,
    onAfterChild: (@Composable (child: Node, parent: Node) -> Unit)? = null,
    onBeforeAll: (@Composable (parent: Node) -> Unit)? = null,
    onAfterAll: (@Composable (parent: Node) -> Unit)? = null,
) {
    val renderRegistry = currentRenderRegistry()
    val childList = children ?: parent.childNodes()
    Column(modifier = modifier, verticalArrangement = verticalArrangement) {
        onBeforeAll?.invoke(parent)
        childList.forEachIndexed { index, child ->
            val skip = renderRegistry.shouldSkipRender(child)
            if (!skip) {
                key(child) {
                    onBeforeChild?.invoke(child, parent)
                    MarkdownContent(
                        node = child,
                        modifier = childModifierFactory(child),
                    )
                    onAfterChild?.invoke(child, parent)
                    if (index != childList.lastIndex &&
                        showSpacer &&
                        renderRegistry.getBlockRenderer(child) != null
                    ) {
                        Spacer(Modifier.height(spacerHeight))
                    }
                }
            }
        }
        onAfterAll?.invoke(parent)
    }
}
