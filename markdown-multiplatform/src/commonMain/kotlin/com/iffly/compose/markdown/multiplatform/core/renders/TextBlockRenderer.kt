package com.iffly.compose.markdown.multiplatform.core.renders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.iffly.compose.markdown.multiplatform.render.IBlockRenderer
import com.iffly.compose.markdown.multiplatform.render.MarkdownText
import org.commonmark.node.Node

class TextBlockRenderer : IBlockRenderer<Node> {
    @Composable
    override fun Invoke(
        node: Node,
        modifier: Modifier,
    ) {
        MarkdownText(parent = node, modifier = modifier)
    }
}
