package com.iffly.compose.markdown.multiplatform.render

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.commonmark.node.Node

interface IBlockRenderer<T : Node> {
    /**
     * Whether to skip rendering for the given node entirely.
     * When true, the block will not be rendered and no spacer will be added around it.
     * Default is false.
     */
    fun shouldSkipRender(node: T): Boolean = false

    /**
     * Whether this block renderer supports text-mode rendering via MarkdownText.
     * When false, this renderer will not be wrapped as inline content in
     * [RenderRegistry.textModeRegistry] and the block will not appear in text mode.
     * Default is true.
     */
    fun supportTextMode(): Boolean = true

    @Composable
    fun Invoke(
        node: T,
        modifier: Modifier,
    )
}
